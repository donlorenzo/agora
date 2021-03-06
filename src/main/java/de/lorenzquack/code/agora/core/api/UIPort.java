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
package de.lorenzquack.code.agora.core.api;

public interface UIPort extends LifeCycle {
    /**
     * Login as a UI user.
     *
     * @param username
     * @param password
     * @return Opaque token with which the user must identify herself in subsequent calls
     */
    Object login(String username, String password);

    /**
     * Logs out the user by invalidating the token
     *
     * @param token
     */
    void logout(Object token);

    void setPassword(Object token, String oldPassword, String newPassword);

    String getVersionString(Object token);
}
