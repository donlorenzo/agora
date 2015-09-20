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

/**
 * LifeCycle events are always called in this order with the following guarantees:
 * 1) initialize
 * No guarantees. Object should be self reliant at this stage.
 * 2) configure
 * Object may try to discover other object but should make no assumptions about their state.
 * 3) start
 * Other objects should be fully initialized at this point.
 * 4) stop
 * All other objects should be still be valid (i.e., should not NullPointer) but might have already stopped.
 * 5) cleanup
 * No guarantees about other objects. Object should again be self reliant.
 */
public interface LifeCycle {
    void initialize();
    void configure(JSONConfig config);
    void start();
    void stop();
    void cleanup();
}
