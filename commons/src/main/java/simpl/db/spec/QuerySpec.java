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

import simpl.db.api.Join;
import simpl.db.api.Query;
import simpl.db.api.QueryDef;

/**
 * An internal cache implementation for found {@link Query} annotations.
 */
public class QuerySpec extends SimplSpec<Query, QueryDef> {
    public Join join = null;

    /**
     * @param name     as of {@link Class#getSimpleName()}
     * @param query    as present at class
     * @param queryDef as the class itself
     */
    public QuerySpec(String name, Query query, Class<? extends QueryDef> queryDef) {
        super(name, query, queryDef);
    }
}
