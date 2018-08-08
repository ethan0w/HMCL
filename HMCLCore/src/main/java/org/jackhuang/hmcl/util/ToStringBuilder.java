/*
 * Hello Minecraft! Launcher.
 * Copyright (C) 2018  huangyuhui <huanghongxun2008@126.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see {http://www.gnu.org/licenses/}.
 */
package org.jackhuang.hmcl.util;

public class ToStringBuilder {

    private final StringBuilder stringBuilder;
    private boolean first = true;

    public ToStringBuilder(Object object) {
        stringBuilder = new StringBuilder(object.getClass().getSimpleName()).append(" [");
    }

    public ToStringBuilder append(String name, Object content) {
        if (!first)
            stringBuilder.append(", ");
        first = false;
        stringBuilder.append(name).append('=').append(content);
        return this;
    }

    @Override
    public String toString() {
        return stringBuilder.toString() + "]";
    }
}
