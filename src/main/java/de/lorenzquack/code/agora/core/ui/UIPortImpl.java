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

import java.security.SecureRandom;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.lorenzquack.code.agora.core.api.JSONConfig;
import de.lorenzquack.code.agora.core.api.UIPort;
import de.lorenzquack.code.agora.core.api.exceptions.AuthenticationException;
import de.lorenzquack.code.agora.core.api.exceptions.AuthorizationException;
import de.lorenzquack.code.agora.core.api.exceptions.JSONConfigException;

import static de.lorenzquack.code.agora.core.utils.Utils.bytesToLong;


public class UIPortImpl implements UIPort {
    private static final Logger LOGGER = LoggerFactory.getLogger(UIPortImpl.class);
    private static final UIPortDelegate UNAUTHORIZED_DELEGATE = new UIPortUnauthorizedDelegate();
    private static final UIPortDelegate AUTHORIZED_DELEGATE = new UIPortAuthorizedDelegate();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int BCRYPT_ROUNDS_EXPO = 10;
    private final Set<Object> _authorizedTokens = Collections.newSetFromMap(new ConcurrentHashMap<Object, Boolean>());
    private String _storedHashedPassword;
    private String _username;
    private JSONConfig _config;

    public UIPortImpl() {
    }

    @Override
    public void initialize() {

    }

    @Override
    public void configure(JSONConfig config) {
        _config = config;
        loadUsernameAndPasswordFromConfig(_config);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void cleanup() {

    }

    private void loadUsernameAndPasswordFromConfig(JSONConfig config) {
        try {
            _username = config.get("username").asString();
            _storedHashedPassword = config.get("password").asString();
        } catch (JSONConfigException e) {
            // pass
        }
    }

    @Override
    public Object login(String username, String password) throws AuthenticationException {
        LOGGER.trace("login attempt for user '{}'", username);
        // SECURITY NOTE: To prevent side channel timing attacks we always evaluate both
        //                the user and the password. Additionally, when evaluating the
        //                overall success we don't use booleans to prevent java from
        //                short circuiting the expression. Also, the error message should
        //                not reveal which part of the authentication failed.
        int correctUser = 0;
        int correctPassword = 0;
        if (_username != null && _username.equals(username)) {
            correctUser = 1;
        }
        if (_storedHashedPassword != null) {
            try {
                if (BCrypt.checkpw(password, _storedHashedPassword)) {
                    correctPassword = 1;
                }
            } catch (IllegalArgumentException e) {
                // pass
                //LOGGER.debug("BCrypt.checkpw failed:", e);
            }
        } else {
            // no password required
            correctPassword = 1;
        }
        boolean authenticationSuccessful = ((correctUser & correctPassword) > 0);
        if (!authenticationSuccessful) {
            throw new AuthenticationException("Could not authenticate user '" + username + "'");
        }
        Object token = generateToken();
        _authorizedTokens.add(token);
        return token;
    }

    @Override
    public void logout(Object token) {
        _authorizedTokens.remove(token);
    }

    @Override
    public void setPassword(Object token, String oldPassword, String newPassword) {
        if (_authorizedTokens.contains(token)) {
            if (oldPassword == null) {
                oldPassword = "";
            }
            if (newPassword == null) {
                newPassword = "";
            }
            if ((_storedHashedPassword == null) || BCrypt.checkpw(oldPassword, _storedHashedPassword)) {
                String hashedNewPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(BCRYPT_ROUNDS_EXPO));
                _storedHashedPassword = hashedNewPassword;
                saveUsernameAndPasswordToConfig(_config);
            }
        } else {
            throw new AuthorizationException();
        }
    }

    @Override
    public String getVersionString(Object token) {
        return getDelegate(token).getVersionString();
    }

    private UIPortDelegate getDelegate(Object token) {
        if (_authorizedTokens.contains(token)) {
            return AUTHORIZED_DELEGATE;
        } else {
            return UNAUTHORIZED_DELEGATE;
        }
    }

    private void saveUsernameAndPasswordToConfig(JSONConfig config) {
        config.putString("username", _username);
        config.putString("password", _storedHashedPassword);
    }

    private static Object generateToken() {
        byte bytes[] = new byte[8];
        RANDOM.nextBytes(bytes);
        return new Long(bytesToLong(bytes));
    }
}
