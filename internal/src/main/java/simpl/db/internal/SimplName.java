/*
 * Copyright 2016 Christian Schmitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package simpl.db.internal;

/**
 * Utility class for column names.
 */
public final class SimplName {
    private SimplName() {
    }

    /**
     * Quotes an SQLite keywords.
     *
     * @param value to quote with {@code "}
     * @return the quoted string
     */
    public static String quote(String value) {
        StringBuilder sb = new StringBuilder(value.length() + 2);
        if (value.charAt(0) != '"')
            sb.append('"');
        sb.append(value);
        if (value.charAt(value.length() - 1) != '"')
            sb.append('"');
        return sb.toString();
    }

    /**
     * Creates an internal name for any given string.
     * This is a snake case representation of the string itself.
     *
     * @param name string
     * @return the internal name for {@code name}
     */
    public static String from(String name) {
        StringBuilder sb = new StringBuilder(name.length() + 10);
        boolean capsToUnderscore = false;
        for (char c : name.toCharArray())
            if (Character.isUpperCase(c)) {
                if (capsToUnderscore) {
                    capsToUnderscore = false;
                    sb.append('_');
                }
                sb.append(Character.toLowerCase(c));
            } else {
                capsToUnderscore = Character.isLowerCase(c) || Character.isDigit(c);
                sb.append(capsToUnderscore ? c : '_');
            }
        return sb.toString();
    }
}
