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
import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        LOGGER.debug("Hello World!");
        CommandlineOptions options = new CommandlineOptions();
        JCommander parser = new JCommander(options, args);
        for (String command : options._commands) {
            if ("help".equals(command)) {
                parser.usage();
            } else {
                LOGGER.info("Ignoring unsupported argument {}", command);
            }
        }
        AgoraCore core = AgoraCore.create();
        core.initialize();
        try {
            ObjectMapper jsonMapper = new ObjectMapper();
            core.configure(jsonMapper.writeValueAsString(options));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        core.start();
        LOGGER.debug("Main#main() is exiting");
    }

    static class CommandlineOptions {
        @Parameter(description = "agora command to execute")
        public List<String> _commands = new ArrayList<>();
        @Parameter(names = { "--config-dir", "-c" })
        public String configurationDirectory = "~/.config/agora";
    }
}
