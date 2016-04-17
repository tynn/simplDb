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

package simpl.db;

import java.lang.annotation.Annotation;

import simpl.db.query.Query;
import simpl.db.table.Column;
import simpl.db.table.Table;

/**
 * {@code SimplError} indicates a contract violation or missing annotations.
 */
public class SimplError extends RuntimeException {
    private static final long serialVersionUID = -2175585598077487715L;

    /**
     * Constructs a new {@code SimplError} with the given cause.
     *
     * @param tr causing this error
     */
    public SimplError(Throwable tr) {
        super(tr);
    }

    /**
     * Constructs a new {@code SimplError} with the given message.
     *
     * @param msg describing this error
     */
    public SimplError(String msg) {
        super(msg);
    }

    /**
     * Constructs a new {@code SimplError} indicating a missing annotation.
     *
     * @param cls the annotation is missing at
     * @param ann missing at {@code cls}
     * @see Database
     * @see Table
     * @see Column
     * @see Query
     */
    public SimplError(Class<? extends SimplDef> cls, Class<? extends Annotation> ann) {
        super("Annotation " + ann.getSimpleName() + " not present on Class " + cls.getSimpleName());
    }
}
