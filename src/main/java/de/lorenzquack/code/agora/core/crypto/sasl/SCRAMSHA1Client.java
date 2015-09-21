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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Bytes;

import static de.lorenzquack.code.agora.core.crypto.sasl.SCRAMPrimitives.*;


public class SCRAMSHA1Client implements SaslClient {
    private static final int NONCE_SIZE = 24;
    private static final int SALT_SIZE = 24;

    private final byte[] _username;
    private final byte[] _password;
    private final byte[] _clientNonce;
    private SCRAMState _state = SCRAMState.START;
    private byte[] _expectedServerSignature;
    private byte[] _clientFirstMessageBare;

    private SCRAMSHA1Client(String username, String password) {
        _username = normalize(username);
        _password = normalize(password);
        _clientNonce = generateNonce(NONCE_SIZE);
    }

    @Override
    public String getMechanismName() {
        return "SCRAM-SHA-1";
    }

    @Override
    public boolean hasInitialResponse() {
        return true;
    }

    @Override
    public byte[] evaluateChallenge(byte[] bytes) throws SaslException {
        byte[] response = new byte[0];
        switch (_state) {
            case ERROR:
                throwError("In ERROR state due to previous error");
            case START:
                response = handleInitialResponse(bytes);
                _state = SCRAMState.AWAIT_SERVER_FIRST;
                break;
            case AWAIT_SERVER_FIRST:
                response = handleServerFirstMessage(bytes);
                _state = SCRAMState.AWAIT_SERVER_FINAL;
                break;
            case AWAIT_SERVER_FINAL:
                handleServerFinalMessage(bytes);
                _state = SCRAMState.COMPLETED;
                break;
            case COMPLETED:
                throwError("Spurious server message after completed handshake");
            case DISPOSED:
                throwError("Spurious server message after disposal");
            default:
                throwError("Sasl failed in unexpected state: " + _state);
        }
        return response;
    }

    @Override
    public boolean isComplete() {
        return _state == SCRAMState.COMPLETED;
    }

    @Override
    public byte[] unwrap(byte[] bytes, int i, int i1) throws SaslException {
        throw new IllegalStateException("SASL mechanism " + getMechanismName() + " does not support unwrapping");
    }

    @Override
    public byte[] wrap(byte[] bytes, int i, int i1) throws SaslException {
        throw new IllegalStateException("SASL mechanism " + getMechanismName() + " does not support wrapping");
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
        Arrays.fill(_password, (byte) 0);
        Arrays.fill(_username, (byte) 0);
        Arrays.fill(_clientNonce, (byte) 0);
        Arrays.fill(_expectedServerSignature, (byte) 0);
        Arrays.fill(_clientFirstMessageBare, (byte) 0);
        _state = SCRAMState.DISPOSED;
    }

    private void throwError(String message) throws SaslException {
        _state = SCRAMState.ERROR;
        throw new SaslException(message);
    }

    private byte[] handleInitialResponse(byte[] bytes) throws SaslException {
        if (bytes.length != 0) {
            throwError("No message expected in START state");
        }
        ByteArrayBuilder clientFirstMessageBuilder = new ByteArrayBuilder();
        // TODO: change n to p and implement channel binding
        clientFirstMessageBuilder.append("n,,");
        ByteArrayBuilder clientFirstMessageBareBuilder = new ByteArrayBuilder();
        clientFirstMessageBareBuilder.append("n=");
        clientFirstMessageBareBuilder.append(_username);
        clientFirstMessageBareBuilder.append(",r=");
        clientFirstMessageBareBuilder.append(_clientNonce);
        _clientFirstMessageBare = clientFirstMessageBareBuilder.toBytes();
        clientFirstMessageBuilder.append(_clientFirstMessageBare);
        return clientFirstMessageBuilder.toBytes();
    }

    private byte[] handleServerFirstMessage(byte[] serverFirstMessage) throws SaslException {
        List<byte[]> parts = split(serverFirstMessage, ',');
        if (parts.size() != 3) {
            throw new SaslException("Sasl failed in AWAIT_SERVER_FIRST phase");
        }
        byte[] serverNonce = parseServerNonce(parts.get(0));
        byte[] salt = parseServerSalt(parts.get(1));
        int iterationCount = parseServerIterationCount(parts.get(2));
        ByteArrayBuilder clientFinalMessageBuilder = new ByteArrayBuilder();
        // TODO channel binding
        clientFinalMessageBuilder.append("c=");
        clientFinalMessageBuilder.append(",r=");
        clientFinalMessageBuilder.append(SCRAMPrimitives.concat(_clientNonce, serverNonce));
        byte[] clientFinalWithoutProof = clientFinalMessageBuilder.toBytes();
        clientFinalMessageBuilder.append(",p=");
        byte[] saltedPassword = Hi(_password, salt, iterationCount);
        byte[] clientKey = HMAC(saltedPassword, normalize("Client Key"));
        byte[] storedKey = H(clientKey);

        byte[] commaAsBytes = { ',' };
        byte[] authMessage = Bytes.concat(_clientFirstMessageBare, commaAsBytes, serverFirstMessage, commaAsBytes,
                                          clientFinalWithoutProof);
        byte[] clientSignature = HMAC(storedKey, authMessage);
        byte[] clientProof = XOR(clientKey, clientSignature);
        byte[] serverKey = HMAC(saltedPassword, normalize("Server Key"));
        _expectedServerSignature = HMAC(serverKey, authMessage);
        clientFinalMessageBuilder.append(clientProof);

        return clientFinalMessageBuilder.toBytes();
    }

    private void handleServerFinalMessage(byte[] serverFinalMessage) throws SaslException {
        if (serverFinalMessage.length < 3 || (serverFinalMessage[1] != 'e' && serverFinalMessage[1] != 'v') || serverFinalMessage[2] != '=') {
            throwError("Unexpected challenge from server");
        }
        String message = Arrays.toString(Arrays.copyOfRange(serverFinalMessage, 2, serverFinalMessage.length));
        if (serverFinalMessage[1] == 'e') {
            throwError("Server error: " + message);
        } else if (serverFinalMessage[1] == 'v') {
            byte[] verifier = BaseEncoding.base64().decode(message);
            if (!Arrays.equals(_expectedServerSignature, verifier)) {
                throwError("Server failed to authenticate");
            }
        }
    }

    private byte[] parseServerNonce(byte[] bytes) throws SaslException {
        int length = bytes.length;
        if (length < 2 + 2 * NONCE_SIZE) {
            throw new SaslException("Sasl failed in AWAIT_SERVER_FIRST phase");
        }
        if (bytes[0] != 'r' || bytes[1] != '=') {
            throw new SaslException("Sasl failed in AWAIT_SERVER_FIRST phase");
        }
        for (int i = 0; i < NONCE_SIZE; ++i) {
            if (bytes[i + 2] != _clientNonce[i]) {
                throw new SaslException("Sasl failed in AWAIT_SERVER_FIRST phase");
            }
        }
        byte[] serverNonce = new byte[NONCE_SIZE];
        for (int i = 0; i < NONCE_SIZE; ++i) {
            serverNonce[i] = bytes[i + 2 + NONCE_SIZE];
        }
        return serverNonce;
    }

    private byte[] parseServerSalt(byte[] bytes) throws SaslException {
        int length = bytes.length;
        if (length < 2 + SALT_SIZE) {
            throw new SaslException("Sasl failed in AWAIT_SERVER_FIRST phase");
        }
        if (bytes[0] != 's' || bytes[1] != '=') {
            throw new SaslException("Sasl failed in AWAIT_SERVER_FIRST phase");
        }
        byte[] salt = new byte[SALT_SIZE];
        for (int i = 0; i < SALT_SIZE; ++i) {
            salt[i] = bytes[i + 2];
        }
        return salt;
    }

    private int parseServerIterationCount(byte[] bytes) throws SaslException {
        int length = bytes.length;
        if (length < 3) {
            throw new SaslException("Sasl failed in AWAIT_SERVER_FIRST phase");
        }
        if (bytes[0] != 'i' || bytes[1] != '=') {
            throw new SaslException("Sasl failed in AWAIT_SERVER_FIRST phase");
        }
        return Integer.parseInt(Arrays.toString(Arrays.copyOfRange(bytes, 2, length)));
    }


    private enum SCRAMState {
        ERROR,
        START,
        AWAIT_SERVER_FIRST,
        AWAIT_SERVER_FINAL,
        AWAIT_CLIENT_FIRST,
        AWAIT_CLIENT_FINAL,
        COMPLETED,
        DISPOSED,
    }


    private class ByteArrayBuilder {
        private List<byte[]> _arrays = new ArrayList<>();
        private int _size;

        void append(byte[] bytes) {
            _arrays.add(bytes);
            _size += bytes.length;
        }

        void append(String str) {
            _arrays.add(normalize(str));
            _size += str.length();
        }

        byte[] toBytes() {
            return Bytes.concat((byte[][]) _arrays.toArray());
/*
            byte[] retValue = new byte[_size];
            int position = 0;
            for (byte[] bytes : _arrays) {
                for (int i = 0; i < bytes.length; ++i, ++position) {
                    retValue[position] = bytes[i];
                }
            }
            return retValue;
            */
        }
    }
}
