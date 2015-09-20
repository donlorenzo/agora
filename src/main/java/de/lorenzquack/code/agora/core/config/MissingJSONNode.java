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

import java.util.List;

import de.lorenzquack.code.agora.core.api.JSONConfig;
import de.lorenzquack.code.agora.core.api.exceptions.JSONConfigException;


public class MissingJSONNode implements JSONConfig {
    private final String _missingPath;

    MissingJSONNode(String node) {
        _missingPath = node;
    }

    @Override
    public JSONConfig get(String child) {
        return new MissingJSONNode(_missingPath + "/" + child);
    }

    @Override
    public JSONConfig get(int index) {
        return new MissingJSONNode(_missingPath + "/" + String.valueOf(index));
    }

    @Override
    public JSONConfig getPath(String path) {
        if (path.startsWith("/")) {
            throw new JSONConfigException("operation on missing node: '" + path + "'");
        }
        return new MissingJSONNode(_missingPath + "/" + path);
    }

    @Override
    public String asJSON() throws JSONConfigException {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public String asString() {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public boolean asBoolean() {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public int asInt() {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public long asLong() {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public double asDouble() {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public boolean isArray() {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public boolean isObject() {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public boolean isContainer() {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public boolean isValue() {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public boolean isNull() {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public boolean isBoolean() {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public boolean isNumeric() {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public boolean isTextual() {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public JSONConfig putString(String key, String value) {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public JSONConfig putBoolean(String key, boolean value) {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public JSONConfig putInteger(String key, int value) {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public JSONConfig putLong(String key, long value) {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public JSONConfig putDouble(String key, double value) {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public JSONConfig putObject(String key) {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public JSONConfig putArray(String key) {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public JSONConfig insertString(int index, String value) {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public JSONConfig insertBoolean(int index, boolean value) {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public JSONConfig insertInteger(int index, int value) {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public JSONConfig insertLong(int index, long value) {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public JSONConfig insertDouble(int index, double value) {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public JSONConfig insertObject(int index) {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public JSONConfig insertArray(int index) {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public JSONConfig appendString(String value) {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public JSONConfig appendBoolean(boolean value) {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public JSONConfig appendInteger(int value) {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public JSONConfig appendLong(long value) {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public JSONConfig appendDouble(double value) {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public JSONConfig appendObject() {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public JSONConfig appendArray() {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public int size() {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public List<String> getKeys() {
        throw new JSONConfigException("operation on missing node: '" + _missingPath + "'");
    }

    @Override
    public boolean exists() {
        return false;
    }
}
