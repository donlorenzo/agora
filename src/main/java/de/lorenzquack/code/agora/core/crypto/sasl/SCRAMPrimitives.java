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

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.google.common.primitives.Bytes;
import com.ibm.icu.text.StringPrep;
import com.ibm.icu.text.StringPrepParseException;


/**
 *
 */
public class SCRAMPrimitives {

    static final int NONCE_SIZE = 24;
    static final int SALT_SIZE = 24;

    private static final String HMAC_SHA_1_ALGORITHM = "HmacSHA1";

    public static byte[] normalize(String str) {
        StringPrep stringPrep = StringPrep.getInstance(StringPrep.RFC4013_SASLPREP);
        try {
            String preparedString = stringPrep.prepare(str, StringPrep.DEFAULT);
            return preparedString.getBytes("UTF-8");
        } catch (StringPrepParseException | UnsupportedEncodingException e) {
            throw new RuntimeException("Could not prepare string for SCRAM use.");
        }
    }

    public static byte[] HMAC(byte[] key, byte[] str) {
        Mac hmac = getHMAC();
        Key hmacKey = new SecretKeySpec(key, HMAC_SHA_1_ALGORITHM);
        try {
            hmac.init(hmacKey);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Invalid Key. This is probably due to a programming error.");
        }
        return hmac.doFinal(str);
    }

    public static byte[] H(byte[] str) {
        return getSha1().digest(str);
    }

    public static byte[] XOR(byte[] a, byte[] b) {
        int length = a.length;
        byte[] result = new byte[length];
        for (int i = 0; i < length; ++i) {
            result[i] = (byte) (0xFF & ((int) a[i] ^ (int) b[i]));
        }
        return result;
    }

    public static byte[] Hi(byte[] str, byte[] salt, int i) {
        byte[] Ui = HMAC(str, concat(salt, intToBigEndian(i)));
        byte[] result = Ui;
        for (int j = 1; j < i; ++j) {
            byte[] Ui_ = HMAC(str, Ui);
            result = XOR(Ui, Ui_);
            Ui = Ui_;
        }
        return result;
    }

    public static byte[] generateNonce(int bytes) {
        throw new RuntimeException("Not implemented");
    }

    public static List<byte[]> split(byte[] bytes, char splitChar) {
        List<byte[]> parts = new ArrayList<>();
        byte splitByte = (byte) splitChar;
        int pos = 0;
        for (int i = 0; i < bytes.length; ++i) {
            if (bytes[i] == splitByte) {
                parts.add(Arrays.copyOfRange(bytes, pos, i));
                // skip to after the splitChar
                pos = i + 1;
            }
        }
        parts.add(Arrays.copyOfRange(bytes, pos, bytes.length));
        return parts;
    }

    public static byte[] concat(byte[] a, byte[] b) {
        return Bytes.concat(a, b);
    }

    /**
     * My best effort at a string compare that is not susceptible to timing attacks
     * @param untrusted string from an untrusted source
     * @param trusted reference string from a trusted source
     * @return true if the strings are equal. false otherwise.
     */
    public static boolean secureStringCompare(String untrusted, String trusted) {
        return secureBytesCompare(normalize(untrusted), normalize(trusted));
    }

    /**
     * My best effort at a byte[] compare that is not susceptible to timing attacks
     * @param untrusted array of bytes from an untrusted source
     * @param trusted reference array of bytes from a trusted source
     * @return true if the byte arrays are equal. false otherwise.
     */
    public static boolean secureBytesCompare(byte[] untrusted, byte[] trusted) {
        int trustedLength = trusted.length;
        int untrustedLength = untrusted.length;
        // first check for same length
        boolean equal = (trustedLength == untrustedLength);
        // do iteration over untrusted to not reveal length of trusted through timing
        for (int i = 0; i < untrustedLength; ++i) {
            byte aChar = untrusted[i];
            byte bChar;
            if (i < trustedLength) {
                bChar = trusted[i];
            } else {
                // always make access; equal is already false so it is okay to use untrusted instead of trusted
                bChar = untrusted[i];
            }
            if (aChar != bChar) {
                equal &= false; // set false
            } else {
                equal |= false; // always perform operation. This is however a no-op.
            }
        }
        return equal;
    }

    private static byte[] intToBigEndian(int i) {
        byte[] encoded = new byte[4];
        for (int j = 0; j < 4; ++j) {
            encoded[j] = (byte) (0xFF & (i >> (8*(3-i))));
        }
        return encoded;
    }

    private static MessageDigest getSha1() {
        try {
            return MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("JVM does not provide SHA cryptographic primitive.");
        }

    }

    private static Mac getHMAC() {
        try {
            return Mac.getInstance(HMAC_SHA_1_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("JVM does not provide HmacSHA1 cryptographic primitive.");
        }
    }
}
