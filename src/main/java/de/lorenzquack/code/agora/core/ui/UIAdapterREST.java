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
package de.lorenzquack.code.agora.core.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lorenzquack.code.agora.core.api.JSONConfig;
import de.lorenzquack.code.agora.core.api.UIAdaptor;
import de.lorenzquack.code.agora.core.api.UIPort;
import de.lorenzquack.code.agora.core.api.exceptions.AuthenticationException;


public class UIAdapterREST implements UIAdaptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(UIAdapterREST.class);

    private UIPort _core;
    private Server _server;
    private JSONConfig _config;

    public UIAdapterREST() {
    }

    @Override
    public void initialize() {

    }

    @Override
    public void configure(JSONConfig config) {
        _config = config;
        int port = config.get("port").asInt();
        _server = new Server(port);
        ServletContextHandler servletHandler = new ServletContextHandler();// ServletHandler();
        _server.setHandler(servletHandler);
        servletHandler.addServlet(new ServletHolder(new RESTHandler(_core)), "/api/*");
        servletHandler.addServlet(new ServletHolder(new StaticHandler()), "/*");
        //servletHandler.addServletWithMapping(RESTHandler.class, "/api/*");
        //servletHandler.addServletWithMapping(StaticHandler.class, "/*");
    }

    @Override
    public void start() {
        try {
            _server.start();
        } catch (Exception e) {
            LOGGER.error("REST server crashed", e);
        }
    }

    @Override
    public void stop() {
        try {
            _server.stop();
        } catch (Exception e) {
            LOGGER.error("REST server failed to stop", e);
        }
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void setUICore(UIPort core) {
        _core = core;
    }

    public static class RESTHandler extends HttpServlet {
        private static final Logger LOGGER = LoggerFactory.getLogger(RESTHandler.class);
        private final UIPort _core;

        public RESTHandler(UIPort core) {
            _core = core;
        }

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            LOGGER.debug("GET request: " + request.getRequestURI());
            super.doGet(request, response);
        }


        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            if ("/login".equals(request.getPathInfo())) {
                String username = request.getParameter("username");
                try {
                    Object token = _core.login(username, request.getParameter("password"));
                    sendResponse(response, 200, "{\"authToken\": \"" + token.toString() + "\"}");
                } catch (AuthenticationException e) {
                    sendResponse(response, 401, "{\"errorMessage\": \"Failed to authenticate user '" + username + "'.\"}");
                }
            }
        }

        private void sendResponse(HttpServletResponse response, int status, String jsonData) {
            response.setStatus(status);
            response.setContentType("application/json");
            response.setContentLength(jsonData.length());
            try {
                response.getWriter().write(jsonData);
            } catch (IOException e) {
                LOGGER.info("Error while sending response", e);
            }
        }
    }


    public static class StaticHandler extends HttpServlet {
        private static final Logger LOGGER = LoggerFactory.getLogger(StaticHandler.class);

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            LOGGER.debug("GET {}", request);
            sendResource(response, request.getRequestURI());
        }

        private void sendResource(HttpServletResponse response, String requestURI) throws IOException {
            final int BUFFER_SIZE = 1024;
            byte[] buffer = new byte[BUFFER_SIZE];
            PrintWriter writer = response.getWriter();
            InputStream resourceStream = UIAdapterREST.class.getResourceAsStream("/WebUI" + requestURI);

            int byteCount = -1;
            while ((byteCount = resourceStream.read(buffer)) != -1) {
                String data = new String(buffer, 0, byteCount, "UTF-8");
                writer.write(data);
            }
            if (requestURI.endsWith(".html")) {
                response.setContentType("text/html");
            } else if (requestURI.endsWith(".js")) {
                response.setContentType("application/javascript");
            } else if (requestURI.endsWith(".css")) {
                response.setContentType("text/css");
            } else {
                response.setContentType("text/plain");
            }
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }
}
