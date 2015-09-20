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
package de.lorenzquack.code.agora.core.api;

import java.util.List;

import de.lorenzquack.code.agora.core.api.exceptions.JSONConfigException;


public interface JSONConfig {
    JSONConfig get(String child);
    JSONConfig get(int index);
    JSONConfig getPath(String path);

    String asJSON() throws JSONConfigException;
    String asString() throws JSONConfigException;
    boolean asBoolean() throws JSONConfigException;
    int asInt() throws JSONConfigException;
    long asLong() throws JSONConfigException;
    double asDouble() throws JSONConfigException;

    boolean isArray() throws JSONConfigException;
    boolean isObject() throws JSONConfigException;
    boolean isContainer() throws JSONConfigException;
    boolean isValue() throws JSONConfigException;
    boolean isNull() throws JSONConfigException;
    boolean isBoolean() throws JSONConfigException;
    boolean isNumeric() throws JSONConfigException;
    boolean isTextual() throws JSONConfigException;

    JSONConfig putString(String key, String value) throws JSONConfigException;
    JSONConfig putBoolean(String key, boolean value) throws JSONConfigException;
    JSONConfig putInteger(String key, int value) throws JSONConfigException;
    JSONConfig putLong(String key, long value) throws JSONConfigException;
    JSONConfig putDouble(String key, double value) throws JSONConfigException;
    JSONConfig putObject(String key) throws JSONConfigException;
    JSONConfig putArray(String key) throws JSONConfigException;

    JSONConfig insertString(int index, String value) throws JSONConfigException;
    JSONConfig insertBoolean(int index, boolean value) throws JSONConfigException;
    JSONConfig insertInteger(int index, int value) throws JSONConfigException;
    JSONConfig insertLong(int index, long value) throws JSONConfigException;
    JSONConfig insertDouble(int index, double value) throws JSONConfigException;
    JSONConfig insertObject(int index) throws JSONConfigException;
    JSONConfig insertArray(int index) throws JSONConfigException;

    JSONConfig appendString(String value) throws JSONConfigException;
    JSONConfig appendBoolean(boolean value) throws JSONConfigException;
    JSONConfig appendInteger(int value) throws JSONConfigException;
    JSONConfig appendLong(long value) throws JSONConfigException;
    JSONConfig appendDouble(double value) throws JSONConfigException;
    JSONConfig appendObject() throws JSONConfigException;
    JSONConfig appendArray() throws JSONConfigException;

    int size() throws JSONConfigException;
    List<String> getKeys() throws JSONConfigException;

    boolean exists();
}
