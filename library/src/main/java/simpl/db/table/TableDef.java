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

package simpl.db.table;

import android.provider.BaseColumns;

import simpl.db.SimplDb;
import simpl.db.SimplDef;
import simpl.db.query.QueryDef;

/**
 * {@code TableDef} is the base class for all simplDb tables.
 * <p>
 * Implementations must also be annotated with {@link Table}.
 * </p>
 */
public interface TableDef extends SimplDef {
    /**
     * Defines the default _id column for a table.
     */
    interface WithID {
        /**
         * Defines an autoincrement primary key integer column for the table.
         * @see BaseColumns#_ID
         */
        @Column(type = ColumnType.INTEGER)
        @PrimaryKey(autoincrement = true, sortorder = Sortorder.ASC)
        String _ID = BaseColumns._ID;
    }

    /**
     * Defines a default current {@code _timestamp} column for a table.
     */
    interface WithCurrentTimestamp {
        @Column(type = ColumnType.TEXT)
        @Default(value = Default.CURRENT_TIMESTAMP)
        String _TIMESTAMP = "_timestamp";

        String _TIMESTAMP_LOCALTIME = "datetime(_timestamp, 'localtime')";
    }

    /**
     * {@code Observer} for changes to a table of the databases.
     */
    interface Observer {
        /**
         * @param queryDef to execute
         * @param db       which changed
         */
        void onTableChanged(Class<? extends QueryDef> queryDef, SimplDb db);
    }
}
