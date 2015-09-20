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

import de.lorenzquack.code.agora.core.api.UIPort;
import de.lorenzquack.code.agora.core.api.exceptions.AuthorizationException;


final class UIPortUnauthorizedDelegate implements UIPortDelegate {
    @Override
    public void setUIPort(UIPort uiPort) {
        // no-op
    }

    @Override
    public String getVersionString() {
        throw new AuthorizationException();
    }

    @Override
    public void setPassword(String oldPassword, String newPassword) {
        throw new AuthorizationException();
    }
}
