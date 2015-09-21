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
package de.lorenzquack.code.agora.core.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lorenzquack.code.agora.core.api.ConfigurationStoreAdaptor;
import de.lorenzquack.code.agora.core.api.JSONConfig;


public class ConfigurationStoreAdaptorJSONFile implements ConfigurationStoreAdaptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationStoreAdaptorJSONFile.class);
    private Path _configurationDirectory;
    private ConcurrentHashMap<String, ConfigFile> _configFileMap = new ConcurrentHashMap<>();

    @Override
    public void setConfigurationDirectory(Path configurationDirectory) {
        _configurationDirectory = configurationDirectory;
    }

    @Override
    public synchronized JSONConfig openStore(String storeName) throws IOException {
        ConfigFile configFile = new ConfigFile(_configurationDirectory, storeName);
        if (!configFile.asFile().exists()) {
            throw new IOException("store '" + storeName + "' does not exist");
        }
        if (_configFileMap.containsKey(storeName)) {
            configFile = _configFileMap.get(storeName);
        } else {
            _configFileMap.put(storeName, configFile);
        }

        return configFile.getRoot();
    }

    @Override
    public JSONConfig createStore(String storeName, String initialContent) throws IOException {
        ConfigFile configFile = new ConfigFile(_configurationDirectory, storeName);
        if (_configFileMap.putIfAbsent(storeName, configFile) != null) {
            throw new IOException("Store '" + storeName + "' already exists.");
        }
        configFile.create(initialContent);
        return configFile.getRoot();
    }

    @Override
    public JSONConfig createStore(String storeName) throws IOException {
        return createStore(storeName, "{}");
    }

    @Override
    public synchronized void save(String store) throws IOException {
        ConfigFile configFile1 = _configFileMap.get(store);
        if (configFile1 != null) {
            configFile1.store();
        }
    }

    @Override
    public synchronized void saveAll() throws IOException {
        List<String> failedStores = new ArrayList<>();
        for (ConfigFile configFile : _configFileMap.values()) {
            try {
                configFile.store();
            } catch (IOException e) {
                failedStores.add(configFile.getName());
            }
        }
        if (!failedStores.isEmpty()) {
            throw new IOException("Failed to save at least one configuration store. List of failed stores: " + failedStores.toString());
        }
    }

    private class ConfigFile {
        private final Path _parentPath;
        private final String _name;
        private volatile boolean _loaded;
        private JsonNode _jsonRoot;

        ConfigFile(Path parentPath, String name) {
            _parentPath = parentPath;
            _name = name;
        }

        public JSONConfig getRoot() throws IOException {
            ensureIsLoaded();
            return new JSONConfigImpl(_jsonRoot);
        }

        private void ensureIsLoaded() throws IOException {
            if (!_loaded) {
                load();
            }
        }

        private synchronized void load() throws IOException {
            if (!_loaded) {
                ObjectMapper jsonMapper = new ObjectMapper();
                _jsonRoot = jsonMapper.readTree(asFile());
                _loaded = true;
            }
        }

        private File asFile() {
            String pathAsString = getPath().toString();
            pathAsString = pathAsString.replaceFirst("^~", System.getProperty("user.home"));
            return new File(pathAsString);
        }

        private Path getPath() {
            return _parentPath.resolve(getName() + ".json");
        }

        private String getName() {
            return _name;
        }

        public void create(String initialContent) throws IOException {
            ObjectMapper mapper = new ObjectMapper();
            _jsonRoot = mapper.readTree(initialContent);
            _loaded = true;
        }

        public void store() throws IOException {
            if (_loaded) {
                try (OutputStream outputStream = new FileOutputStream(asFile())) {
                    ObjectMapper jsonMapper = new ObjectMapper();
                    JsonFactory jsonFactory = new JsonFactory();
                    JsonGenerator jsonGenerator = jsonFactory.createGenerator(outputStream);
                    jsonMapper.writerWithDefaultPrettyPrinter().writeValue(jsonGenerator, _jsonRoot);
                }
            }
        }

        private Path getParentPath() {
            return _parentPath;
        }
    }
}
