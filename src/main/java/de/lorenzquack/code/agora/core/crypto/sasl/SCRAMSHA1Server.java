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

import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;


public class SCRAMSHA1Server implements SaslServer {
    @Override
    public String getMechanismName() {
        return "SCRAM-SHA-1";
    }

    @Override
    public byte[] evaluateResponse(byte[] bytes) throws SaslException {
        return new byte[0];
    }

    @Override
    public boolean isComplete() {
        return false;
    }

    @Override
    public String getAuthorizationID() {
        return null;
    }

    @Override
    public byte[] unwrap(byte[] bytes, int i, int i1) throws SaslException {
        throw new IllegalStateException("SASL mechanism " + getMechanismName() + " does not support unwrapping");
    }

    @Override
    public byte[] wrap(byte[] bytes, int i, int i1) throws SaslException {
        throw new IllegalStateException("SASL mechanism " + getMechanismName() + " does not support unwrapping");
    }

    @Override
    public Object getNegotiatedProperty(String s) {
        if (!isComplete()) {
            throw new IllegalStateException("SASL mechanism " + getMechanismName() + " has not completed yet");
        }
        return null;
    }

    @Override
    public void dispose() throws SaslException {

    }
}
