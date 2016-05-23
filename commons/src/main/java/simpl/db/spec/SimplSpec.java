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

package simpl.db.spec;

import java.lang.annotation.Annotation;

import simpl.db.api.SimplDef;

/**
 * An internal cache abstraction for found annotations.
 *
 * @param <A> annotation type
 * @param <D> simplDef implementation
 */
public abstract class SimplSpec<A extends Annotation, D extends SimplDef> {
    public final String name;
    public final A annotation;
    public final Class<? extends D> simplDef;

    /**
     * @param name       as of {@link Class#getSimpleName()}
     * @param annotation as present at class
     * @param simplDef   as the class itself
     */
    protected SimplSpec(String name, A annotation, Class<? extends D> simplDef) {
        this.name = name;
        this.annotation = annotation;
        this.simplDef = simplDef;
    }

    /**
     * Creates an internal name for any given string.
     * This is a snake case representation of the string itself.
     *
     * @param name string
     * @return the internal name for {@code name}
     */
    public static String getName(String name) {
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
