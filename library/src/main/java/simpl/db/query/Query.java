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
 * Annotation to define a query of a database.
 * <p>
 * Implementations of {@link QueryDef} may be annotated with {@code @Query}.
 * </p>
 */
@Documented
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
public @interface Query {
    /**
     * @return the table to query
     */
    Class<? extends TableDef> table();

    /**
     * @return the columns to return
     */
    String[] columns();

    /**
     * @return selection filter
     */
    String selection() default "";

    /**
     * @return arguments for selection filter using {@code ?s}
     */
    String[] selectionArgs() default {};

    /**
     * @return group by filter
     */
    String groupBy() default "";

    /**
     * @return order by filter
     */
    String orderBy() default "";

    /**
     * @return having filter
     */
    String having() default "";

    /**
     * @return limit of rows to return
     */
    int limit() default 0;
}
