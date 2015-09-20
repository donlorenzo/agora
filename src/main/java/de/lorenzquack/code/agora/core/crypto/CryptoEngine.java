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
package de.lorenzquack.code.agora.core.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;


public class CryptoEngine {
    private final SecureRandom _random;
    //private final Cipher _cipher;

    public CryptoEngine() {
        try {
            _random = SecureRandom.getInstance("SHA1PRNG");
            //_cipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Could not setup CryptoEngine", e);
        }
        /*
        catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        */
    }

    public byte[] getNewBlockKey(int keySize) {
        byte[] key = new byte[keySize];
        _random.nextBytes(key);
        return key;
    }

    public byte[] blockEncrypt(byte[] data, byte[] key) {
        throw new NotImplementedException();
    }
}
