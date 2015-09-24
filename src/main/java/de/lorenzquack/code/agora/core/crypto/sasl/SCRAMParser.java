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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.sasl.SaslException;

public class SCRAMParser {
    // RegEx patterns to match the parts of the client-first-message taken from rfc5802, rfc3629, and rfc5234
    private static final String ALPHA = "[\\x41-\\x5A]|[\\x61-\\x7A]";
    private static final String DIGIT = "\\d";
    private static final String PRINTABLE = "(?:[\\x21-\\x2B]|[\\x2D-\\x7E])";
    private static final String UTF8_TAIL = "[\\x80-\\xBF]";
    private static final String UTF8_2 = "[\\xC2-\\xDF]" + UTF8_TAIL;
    private static final String UTF8_3 = "\\xE0[\\xA0-\\xBF]" + UTF8_TAIL + "|[\\xE1-\\xEC]" + UTF8_TAIL + "{2}|" +
                                         "\\xED[\\x80-\\x9F]" + UTF8_TAIL + "|[\\xEE-\\xEF]" + UTF8_TAIL + "{2}";
    private static final String UTF8_4 = "\\xF0[\\x90-\\xBF]" + UTF8_TAIL + "{2}|[\\xF1-\\xF3]" + UTF8_TAIL + "{3}|" +
                                         "\\xF4[\\x80-\\x8F]" + UTF8_TAIL + "{2}";
    private static final String BASE64_CHAR = "(?:" + ALPHA + "|" + DIGIT + "|/|\\+)";
    private static final String BASE64_2 = BASE64_CHAR + "{2}==";
    private static final String BASE64_3 = BASE64_CHAR + "{3}=";
    private static final String BASE64_4 = BASE64_CHAR + "{4}";
    private static final String BASE64 = "(?:" + BASE64_4 + ")*" + "(?:" + BASE64_3 + "|" + BASE64_2 + ")?";
    private static final String VALUE_SAFE_CHAR = "[\\x01-\\x2B]|[\\x2D-\\x3C]|[\\x3E-\\x7F]|" + UTF8_2 + "|" + UTF8_3 + "|" + UTF8_4;
    private static final String VALUE_CHAR = "(?:" + VALUE_SAFE_CHAR + "|=)";
    private static final String SASLNAME = "(?:" + VALUE_SAFE_CHAR + "|=2C|=3D)+";
    private static final String AUTHZID = "(?:a=(?<authzid>" + SASLNAME + "))?";
    private static final String CB_NAME = "p=(?<cbName>" + ALPHA + "|" + DIGIT + "|\\.|-)";
    private static final String GS2_CBIND_FLAG = "(?<gs2CBindFlag>(?:" + CB_NAME + ")|n|y)";
    private static final String GS2_HEADER = GS2_CBIND_FLAG + "," + AUTHZID + ",";
    private static final String RESERVED_M_EXT = "(?:m=" + VALUE_CHAR + "+)?";
    private static final String EXTENSION = ""; // TODO
    private static final String NONCE = "r=(?<nonce>" + PRINTABLE + "+)";
    private static final String USERNAME = "n=(?<username>" + SASLNAME + ")";
    private static final String CLIENT_FIRST_MESSAGE_BARE = "(?<clientFirstMessageBare>" + RESERVED_M_EXT + USERNAME + "," + NONCE + EXTENSION + ")";
    private static final String CLIENT_FIRST_MESSAGE = GS2_HEADER + CLIENT_FIRST_MESSAGE_BARE;

    private static final String CHANNEL_BINDING = "c=(?<channelBinding>" + BASE64 + ")";
    private static final String CLIENT_FINAL_MESSAGE_WITHOUT_PROOF = "(?<clientFinalMessageWithoutProof>" + CHANNEL_BINDING + "," + NONCE + EXTENSION + ")";
    private static final String PROOF = "p=(?<proof>" + BASE64 + ")";
    private static final String CLIENT_FINAL_MESSAGE = CLIENT_FINAL_MESSAGE_WITHOUT_PROOF + "," + PROOF;

    private static final Pattern CLIENT_FIRST_MESSAGE_PATTERN = Pattern.compile(CLIENT_FIRST_MESSAGE);
    private static final Pattern CLIENT_FINAL_MESSAGE_PATTERN = Pattern.compile(CLIENT_FINAL_MESSAGE);

    public static ClientFirstMessageInfo parseClientFirstMessage(byte[] clientFirstMessage) throws SaslException {
        String clientFirstMessageAsString;
        try {
            clientFirstMessageAsString = new String(clientFirstMessage, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new SaslException("Malformed client-first-message");
        }
        Matcher matcher = CLIENT_FIRST_MESSAGE_PATTERN.matcher(clientFirstMessageAsString);
        String channelBindingName = matcher.group("cbName");
        String clientFirstMessageBare = matcher.group("clientFirstMessageBare");
        String username = matcher.group("username");
        String clientNonce = matcher.group("nonce");
        if (channelBindingName == null || clientFirstMessageBare == null || username == null || clientNonce == null) {
            throw new SaslException("Malformed client-first-message");
        }
        return new ClientFirstMessageInfo(channelBindingName, clientFirstMessageBare, username, clientNonce);
    }

    public static ClientFinalMessageInfo parseClientFinalMessage(byte[] clientFinalMessage) throws SaslException {
        String clientFinalMessageAsString;
        try {
            clientFinalMessageAsString = new String(clientFinalMessage, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new SaslException("Malformed client-final-message");
        }
        Matcher matcher = CLIENT_FINAL_MESSAGE_PATTERN.matcher(clientFinalMessageAsString);
        String nonce = matcher.group("nonce");
        String proof = matcher.group("proof");
        String clientFinalMessageWithoutProof = matcher.group("clientFinalMessageWithoutProof");
        if (nonce == null || proof == null || clientFinalMessageWithoutProof == null) {
            throw new SaslException("Malformed client-final-message");
        }
        return new ClientFinalMessageInfo(nonce, proof, clientFinalMessageWithoutProof);
    }


    public static final class ClientFirstMessageInfo {
        private final String _channelBindingName;
        private final String _clientFirstMessageBare;
        private final String _username;
        private final String _clientNonce;

        private ClientFirstMessageInfo(String channelBindingName, String clientFirstMessageBare, String username, String clientNonce) {
            _channelBindingName = channelBindingName;
            _clientFirstMessageBare = clientFirstMessageBare;
            _username = username;
            _clientNonce = clientNonce;
        }

        public String getChannelBindingName() {
            return _channelBindingName;
        }

        public String getClientFirstMessageBare() {
            return _clientFirstMessageBare;
        }

        public String getUsername() {
            return _username;
        }

        public String getClientNonce() {
            return _clientNonce;
        }
    }

    public static final class ClientFinalMessageInfo {
        private final String _nonce;
        private final String _proof;
        private final String _clientFinalMessageWithoutProof;

        private ClientFinalMessageInfo(String nonce, String proof, String clientFinalMessageWithoutProof) {
            _nonce = nonce;
            _proof = proof;
            _clientFinalMessageWithoutProof = clientFinalMessageWithoutProof;
        }

        public String getNonce() {
            return _nonce;
        }

        public String getProof() {
            return _proof;
        }

        public String getClientFinalMessageWithoutProof() {
            return _clientFinalMessageWithoutProof;
        }
    }
}
