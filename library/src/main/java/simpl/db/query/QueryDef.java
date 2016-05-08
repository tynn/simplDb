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

import android.database.Cursor;

import simpl.db.SimplDb;
import simpl.db.SimplDef;
import simpl.db.SimplQuery;
import simpl.db.table.TableDef;

/**
 * {@code QueryDef} is the base class for all simplDb queries.
 * <p>
 * Implementations must also be annotated with {@link Query} or implement {@link TableDef}.
 * If the {@code Query} annotation is not used, the query returns all columns and rows of the table.
 * </p>
 *
 * @see SimplQuery#get(Class)
 */
public interface QueryDef extends SimplDef {

    /**
     * {@code Callback} for notification of a finished asynchronous query.
     */
    interface Callback {
        /**
         * @param cursor   returned for {@code queryDef} with applied {@code filter}
         * @param queryDef executed
         * @param filter   applied
         * @param db       queried
         */
        void onQueryFinished(Cursor cursor, Class<? extends QueryDef> queryDef, SimplQuery.Filter filter, SimplDb db);
    }
}
