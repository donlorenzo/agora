/*
 * Copyright 2015 by Lorenz Quack
 *
 * This file is part of agora.
 *
 *     agora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     agora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with agora.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.lorenzquack.code.agora.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lorenzquack.code.agora.core.api.JSONConfig;
import de.lorenzquack.code.agora.core.api.LifeCycle;
import de.lorenzquack.code.agora.core.api.NetworkAdaptor;
import de.lorenzquack.code.agora.core.api.NetworkPort;
import de.lorenzquack.code.agora.core.api.PluginPort;
import de.lorenzquack.code.agora.core.api.UIAdaptor;
import de.lorenzquack.code.agora.core.api.UIPort;
import de.lorenzquack.code.agora.core.config.ConfigurationStoreAdaptor;
import de.lorenzquack.code.agora.core.config.ConfigurationStoreAdaptorJSONFile;
import de.lorenzquack.code.agora.core.network.NetworkAdaptorTCP;
import de.lorenzquack.code.agora.core.network.NetworkPortImpl;
import de.lorenzquack.code.agora.core.plugins.PluginPortImpl;
import de.lorenzquack.code.agora.core.ui.UIAdapterREST;
import de.lorenzquack.code.agora.core.ui.UIPortImpl;

import static de.lorenzquack.code.agora.core.utils.Utils.streamToString;


public class AgoraCore {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgoraCore.class);

    private static final ExecutorService MAIN_LOOP_EXECUTOR = Executors.newSingleThreadExecutor();
    private final List<LifeCycle> _managedObjects = new ArrayList<>();

    private final NetworkPort _networkPort;
    private final PluginPort _pluginsPort;
    private final UIPort _uiPort;

    private final ConfigurationStoreAdaptor _configurationStoreAdaptor;
    private final NetworkAdaptor _networkAdaptor;
    private final UIAdaptor _uiAdaptorREST;

    private volatile boolean _quit = false;
    private Path _configurationDirectory;
    private JSONConfig _config;

    private AgoraCore() {
        installShutdownHook();
        _networkPort = new NetworkPortImpl();
        _pluginsPort = new PluginPortImpl();
        _uiPort = new UIPortImpl();
        _configurationStoreAdaptor = new ConfigurationStoreAdaptorJSONFile();
        _networkAdaptor = new NetworkAdaptorTCP();
        _uiAdaptorREST = new UIAdapterREST();
        _managedObjects.add(_networkPort);
        _managedObjects.add(_pluginsPort);
        _managedObjects.add(_uiPort);
        _managedObjects.add(_networkAdaptor);
        _managedObjects.add(_uiAdaptorREST);
    }

    private void installShutdownHook() {
        final AgoraCore core = this;
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                LOGGER.trace("shutdown hook");
                core.shutdown();
            }
        });
    }

    void shutdown() {
        LOGGER.info("shutting down Agora...");
        _quit = true;
    }

    static AgoraCore create() {
        return new AgoraCore();
    }

    void initialize() {
        for (LifeCycle object : _managedObjects) {
            object.initialize();
        }
        _uiAdaptorREST.setUICore(_uiPort);
    }

    void configure(String config) throws IOException {
        ObjectMapper jsonMapper = new ObjectMapper();
        JsonNode jsonRootNode;
        try {
            jsonRootNode = jsonMapper.readTree(config);
        } catch (IOException e) {
            throw new RuntimeException(this.getClass().getSimpleName() + "#configure() failed", e);
        }
        _configurationDirectory = Paths.get(jsonRootNode.get("configurationDirectory").asText());
        _configurationStoreAdaptor.setConfigurationDirectory(_configurationDirectory);
        try {
            _config = _configurationStoreAdaptor.openStore("core");
        } catch (IOException e) {
            InputStream defaultCoreConfigStream = this.getClass().getResourceAsStream("/core.json");
            String defaultCoreConfigString = streamToString(defaultCoreConfigStream);
            _config = _configurationStoreAdaptor.createStore("core", defaultCoreConfigString);
        }
        _networkPort.configure(_config.get("network"));
        _pluginsPort.configure(_config.get("plugins"));
        _uiPort.configure(_config.get("ui"));
        _uiAdaptorREST.configure(_config.getPath("adaptors/ui/rest"));
        _networkAdaptor.configure(_config.get("adaptors/network/tcp"));
    }

    void start() {
        for (LifeCycle object : _managedObjects) {
            object.start();
        }
        final AgoraCore core = this;

        Future<Void> future = MAIN_LOOP_EXECUTOR.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                try {
                    mainLoop();
                } catch (Throwable t) {
                    LOGGER.error("Agora terminated unexpectedly", t);
                    throw t;
                } finally {
                    core.stop();
                }
                LOGGER.info("Agora is closed!");
                return null;
            }
        });
        try {
            future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void mainLoop() {
        while (!_quit) {
            LOGGER.debug("MainThread still spinning");

            Object authToken = _uiPort.login("admin", "admin");
            LOGGER.debug(_uiPort.getVersionString(authToken));
            _uiPort.logout(authToken);

            try {
                Thread.currentThread().sleep(500);
            } catch (InterruptedException e) {
                break;
            }
        }
        LOGGER.debug("byebye");
        System.out.println("foo");
    }

    private void stop() {
        LOGGER.info("Agora stop()");
        for (LifeCycle object : _managedObjects) {
            object.stop();
        }

        _quit = true;
        MAIN_LOOP_EXECUTOR.shutdown();
        try {
            MAIN_LOOP_EXECUTOR.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            MAIN_LOOP_EXECUTOR.shutdownNow();
        }

        for (LifeCycle object : _managedObjects) {
            object.cleanup();
        }
        try {
            _configurationStoreAdaptor.saveAll();
        } catch (IOException e) {
            LOGGER.error("error saving all configuration stores during shutdown", e);
        }
    }
}
