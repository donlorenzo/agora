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
import javax.security.sasl.SaslServer;

import static de.lorenzquack.code.agora.core.crypto.sasl.SCRAMPrimitives.*;


public class SCRAMSHA1Server implements SaslServer {
    private final byte[] _serverNonce;
    private SCRAMServerState _state;
    private String _channelBindingName;
    private String _clientFirstMessageBare;
    private String _clientNonce;
    private String _username;
    private String _serverFirstMessage;
    private SCRAMAuthenticationInformation _authenticationInformation;

    private SCRAMSHA1Server() {
        _serverNonce = generateNonce(NONCE_SIZE);
        _state = SCRAMServerState.AWAIT_CLIENT_FIRST;
    }

    @Override
    public String getMechanismName() {
        return "SCRAM-SHA-1";
    }

    @Override
    public byte[] evaluateResponse(byte[] bytes) throws SaslException {
        byte[] response = new byte[0];
        switch (_state) {
            case ERROR:
                throwError("In ERROR state due to previous error");
            case AWAIT_CLIENT_FIRST:
                response = handleClientFirstMessage(bytes);
                _state = SCRAMServerState.AWAIT_CLIENT_FIRST;
                break;
            case AWAIT_CLIENT_FINAL:
                response = handleClientFinalMessage(bytes);
                _state = SCRAMServerState.COMPLETED;
                break;
            case COMPLETED:
                throwError("Spurious client message after completed handshake");
            case DISPOSED:
                throwError("Spurious client message after disposal");
            default:
                throwError("Sasl failed in unexpected state: " + _state);
        }
        return response;
    }

    @Override
    public boolean isComplete() {
        return _state == SCRAMServerState.COMPLETED;
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
        _state = SCRAMServerState.DISPOSED;
        _channelBindingName = null;
        _clientFirstMessageBare = null;
        _clientNonce = null;
        _username = null;
        _serverFirstMessage = null;
        _authenticationInformation = null;
    }

    private byte[] handleClientFirstMessage(byte[] clientFirstMessage) throws SaslException {
        SCRAMParser.ClientFirstMessageInfo clientFirstMessageInfo = SCRAMParser.parseClientFirstMessage(clientFirstMessage);
        _channelBindingName = clientFirstMessageInfo.getChannelBindingName();
        _clientNonce = clientFirstMessageInfo.getClientNonce();
        _clientFirstMessageBare = clientFirstMessageInfo.getClientFirstMessageBare();
        _username = clientFirstMessageInfo.getUsername();
        _authenticationInformation = getAuthenticationInformation(_username);

        String nonce = _clientNonce + _serverNonce;
        String salt = _authenticationInformation.getSalt();
        int iterationCount = _authenticationInformation.getIterationCount();
        String serverFirstMessage = nonce + "," + salt + "," + String.valueOf(iterationCount);
        byte[] serverFirstMessageAsBytes = new byte[0];
        try {
            serverFirstMessageAsBytes = serverFirstMessage.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throwError("Could not encode server-first-message");
        }
        return serverFirstMessageAsBytes;
    }

    private byte[] handleClientFinalMessage(byte[] clientFinalMessage) throws SaslException {
        SCRAMParser.ClientFinalMessageInfo clientFinalMessageInfo = SCRAMParser.parseClientFinalMessage(clientFinalMessage);

        String nonce = clientFinalMessageInfo.getNonce();
        String proof = clientFinalMessageInfo.getProof();
        String clientFinalMessageWithoutProof = clientFinalMessageInfo.getClientFinalMessageWithoutProof();

        byte[] proofAsBytes = normalize(proof);
        byte[] storedKey = normalize(_authenticationInformation.getStoredKey());
        byte[] authMessage = normalize(_clientFirstMessageBare + "," + _serverFirstMessage +
                                       "," + clientFinalMessageWithoutProof);
        byte[] clientSignature = HMAC(storedKey, authMessage);
        byte[] clientKey = XOR(clientSignature, proofAsBytes);
        byte[] storedKeyComputedFromProof = H(clientKey);
        boolean verified = true;
        if (!secureStringCompare(nonce, _clientNonce + _serverNonce)) {
            verified = false;
        }
        if (!secureBytesCompare(storedKeyComputedFromProof, storedKey)) {
            verified = false;
        }
        if (!verified) {
            throwError("Authorization failed");
        }

        byte[] serverKey = normalize(_authenticationInformation.getServerKey());
        byte[] serverSignature = HMAC(serverKey, authMessage);
        return concat(new byte[]{'v','='}, serverSignature);
    }

    private void throwError(String errorMessage) throws SaslException {
        _state = SCRAMServerState.ERROR;
        throw new SaslException(errorMessage);
    }

    private SCRAMAuthenticationInformation getAuthenticationInformation(String username) {
        return new SCRAMAuthenticationInformation();
    }

    enum SCRAMServerState {
        ERROR,
        AWAIT_CLIENT_FIRST,
        AWAIT_CLIENT_FINAL,
        COMPLETED,
        DISPOSED,
    }
}
