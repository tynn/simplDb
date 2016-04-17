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

package simpl.db.query;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import simpl.db.table.TableDef;

/**
 * Annotation to define a join clause of a query.
 * <p>
 * An implementation of {@link QueryDef} annotated with {@link @Query}
 * may also be annotated with {@code @Join}.
 * </p>
 */
@Documented
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
public @interface Join {
    /**
     * @return the table to join
     */
    Class<? extends TableDef> table();

    /**
     * @return the columns of the table to use
     */
    String[] columns();

    /**
     * @return join clause
     */
    String on();

    /**
     * @return join type
     */
    JoinType type() default JoinType.DEFAULT;

    /**
     * @return if it is a natural join
     */
    boolean natural() default false;
}
