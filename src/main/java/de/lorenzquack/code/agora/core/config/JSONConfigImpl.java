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
package de.lorenzquack.code.agora.core.config;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;

import de.lorenzquack.code.agora.core.api.JSONConfig;
import de.lorenzquack.code.agora.core.api.exceptions.JSONConfigException;


public class JSONConfigImpl implements JSONConfig {
    private final JsonNode _root;

    public JSONConfigImpl(JsonNode root) {
        _root = root;
    }

    public JSONConfigImpl(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            _root = mapper.readTree(json);
        } catch (IOException e) {
            throw new JSONConfigException("Error parsing JSON", e);
        }
    }

    @Override
    public JSONConfig get(String child) {
        assertIsObject();
        JsonNode childNode = _root.get(child);
        if (childNode == null) {
            return new MissingJSONNode(child);
        } else {
            return new JSONConfigImpl(childNode);
        }
    }

    @Override
    public JSONConfig get(int index) {
        assertIsArray();
        if (index < 0 || index >= _root.size()) {
            return new MissingJSONNode(String.valueOf(index));
        }
        return new JSONConfigImpl(_root.get(index));
    }

    @Override
    public JSONConfig getPath(String path) {
        String[] parts = path.split("/");
        JSONConfig currentConfig = this;
        for (String part : parts) {
            if (!part.isEmpty()) {
                try {
                    currentConfig = currentConfig.get(part);
                } catch (JSONConfigException e) {
                    currentConfig = currentConfig.get(Integer.valueOf(part));
                }
            }
        }
        return currentConfig;
    }

    @Override
    public String asJSON() {
        return _root.toString();
    }

    @Override
    public String asString() throws JSONConfigException {
        if (!isTextual()) {
            throw new JSONConfigException("JSONConfig is not a String");
        }
        return _root.asText();
    }

    @Override
    public boolean asBoolean() {
        if (!isBoolean()) {
            throw new JSONConfigException("JSONConfig is not a boolean.");
        }
        return _root.asBoolean();
    }

    @Override
    public int asInt() {
        if (!_root.canConvertToInt()) {
            throw new JSONConfigException("JSONConfig is not numeric or too large for int. Cannot convert to int.");
        }
        return _root.asInt();
    }

    @Override
    public long asLong() {
        if (!_root.canConvertToLong()) {
            throw new JSONConfigException("JSONConfig is not numeric or too large for long. Cannot convert to long.");
        }
        return _root.asLong();
    }

    @Override
    public double asDouble() {
        if (!isNumeric()) {
            throw new JSONConfigException("JSONConfig is not numeric. Cannot convert to double.");
        }
        return _root.asDouble();
    }

    @Override
    public boolean isArray() {
        return _root.isArray();
    }

    @Override
    public boolean isObject() {
        return _root.isObject();
    }

    @Override
    public boolean isContainer() {
        return isArray() || isObject();
    }

    @Override
    public boolean isValue() {
        return _root.isValueNode();
    }

    @Override
    public boolean isNull() {
        return _root.isNull();
    }

    @Override
    public boolean isBoolean() {
        return _root.isBoolean();
    }

    @Override
    public boolean isNumeric() {
        return _root.isNumber();
    }

    @Override
    public boolean isTextual() {
        return _root.isTextual();
    }

    @Override
    public JSONConfig putString(String key, String value) {
        assertIsObject();
        ((ObjectNode) _root).put(key, value);
        return this;
    }

    @Override
    public JSONConfig putBoolean(String key, boolean value) {
        assertIsObject();
        ((ObjectNode) _root).put(key, value);
        return this;
    }

    @Override
    public JSONConfig putInteger(String key, int value) {
        assertIsObject();
        ((ObjectNode) _root).put(key, value);
        return this;
    }

    @Override
    public JSONConfig putLong(String key, long value) {
        assertIsObject();
        ((ObjectNode) _root).put(key, value);
        return this;
    }

    @Override
    public JSONConfig putDouble(String key, double value) {
        assertIsObject();
        ((ObjectNode) _root).put(key, value);
        return this;
    }

    @Override
    public JSONConfig putObject(String key) {
        assertIsObject();
        JsonNode newNode = ((ObjectNode) _root).putObject(key);
        return new JSONConfigImpl(newNode);
    }

    @Override
    public JSONConfig putArray(String key) {
        assertIsObject();
        JsonNode newNode = ((ObjectNode) _root).putArray(key);
        return new JSONConfigImpl(newNode);
    }

    @Override
    public JSONConfig insertString(int index, String value) {
        assertIsArray();
        ((ArrayNode) _root).insert(index, value);
        return this;
    }

    @Override
    public JSONConfig insertBoolean(int index, boolean value) {
        assertIsArray();
        ((ArrayNode) _root).insert(index, value);
        return this;
    }

    @Override
    public JSONConfig insertInteger(int index, int value) {
        assertIsArray();
        ((ArrayNode) _root).insert(index, value);
        return this;
    }

    @Override
    public JSONConfig insertLong(int index, long value) {
        assertIsArray();
        ((ArrayNode) _root).insert(index, value);
        return this;
    }

    @Override
    public JSONConfig insertDouble(int index, double value) {
        assertIsArray();
        ((ArrayNode) _root).insert(index, value);
        return this;
    }

    @Override
    public JSONConfig insertObject(int index) {
        assertIsArray();
        JsonNode newNode = ((ArrayNode) _root).insertObject(index);
        return new JSONConfigImpl(newNode);
    }

    @Override
    public JSONConfig insertArray(int index) {
        assertIsArray();
        JsonNode newNode = ((ArrayNode) _root).insertArray(index);
        return new JSONConfigImpl(newNode);
    }

    @Override
    public JSONConfig appendString(String value) {
        assertIsArray();
        ((ArrayNode) _root).add(value);
        return this;
    }

    @Override
    public JSONConfig appendBoolean(boolean value) {
        assertIsArray();
        ((ArrayNode) _root).add(value);
        return this;
    }

    @Override
    public JSONConfig appendInteger(int value) {
        assertIsArray();
        ((ArrayNode) _root).add(value);
        return this;
    }

    @Override
    public JSONConfig appendLong(long value) {
        assertIsArray();
        ((ArrayNode) _root).add(value);
        return this;
    }

    @Override
    public JSONConfig appendDouble(double value) {
        assertIsArray();
        ((ArrayNode) _root).add(value);
        return this;
    }

    @Override
    public JSONConfig appendObject() {
        assertIsArray();
        JsonNode newNode = ((ArrayNode) _root).addObject();
        return new JSONConfigImpl(newNode);
    }

    @Override
    public JSONConfig appendArray() {
        assertIsArray();
        JsonNode newNode = ((ArrayNode) _root).addArray();
        return new JSONConfigImpl(newNode);
    }

    @Override
    public int size() {
        assertIsContainer();
        return _root.size();
    }

    @Override
    public List<String> getKeys() {
        assertIsObject();
        return Lists.newArrayList(_root.fieldNames());
    }

    @Override
    public boolean exists() {
        return true;
    }

    private void assertIsContainer() {
        if (!isContainer()) {
            throw new JSONConfigException("JSONConfig is not a JSON container.");
        }
    }

    private void assertIsArray() {
        if (!isObject()) {
            throw new JSONConfigException("JSONConfig is not a JSON array.");
        }
    }

    private void assertIsObject() {
        if (!isObject()) {
            throw new JSONConfigException("JSONConfig is not a JSON object.");
        }
    }
}
