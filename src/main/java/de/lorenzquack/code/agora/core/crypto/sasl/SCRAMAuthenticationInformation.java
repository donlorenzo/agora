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
package de.lorenzquack.code.agora.core.crypto.sasl;

public final class SCRAMAuthenticationInformation {
    private final int _iterationCount;
    private final String _salt;
    private final String _storedKey;
    private final String _serverKey;

    public SCRAMAuthenticationInformation(int iterationCount, String salt, String storedKey, String serverKey) {
        _iterationCount = iterationCount;
        _salt = salt;
        _storedKey = storedKey;
        _serverKey = serverKey;
    }

    public final int getIterationCount() {
        return _iterationCount;
    }

    public final String getSalt() {
        return _salt;
    }

    public final String getStoredKey() {
        return _storedKey;
    }

    public final String getServerKey() {
        return _serverKey;
    }
}
