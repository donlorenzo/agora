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
package de.lorenzquack.code.agora.core.logging;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.FilterReply;


public class NameAndLevelFilter extends AbstractMatcherFilter<ILoggingEvent> {
    private Pattern _loggerNamePattern;
    private Level _level;

    public NameAndLevelFilter() {
        this(".*", Level.DEBUG);
    }

    public NameAndLevelFilter(String loggerName, Level level) {
        super();
        setOnMatch(FilterReply.ACCEPT);
        setOnMismatch(FilterReply.NEUTRAL);
        _loggerNamePattern = Pattern.compile(loggerName);
        _level = level;
    }

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (!event.getLevel().isGreaterOrEqual(_level)) {
            return getOnMismatch();
        } else {
            Matcher loggerNameMatcher = _loggerNamePattern.matcher(event.getLoggerName());
            if (!loggerNameMatcher.matches()) {
                return getOnMismatch();
            }
        }
        return getOnMatch();
    }

    public void setLoggerName(String loggerName) {
        _loggerNamePattern = Pattern.compile(loggerName);
    }

    public void setLevel(Level level) {
        _level = level;
    }
}
