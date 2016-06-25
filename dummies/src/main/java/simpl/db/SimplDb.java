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

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import simpl.db.api.SimplDef;
import simpl.db.api.TableDef;

public abstract class SimplDb implements SimplDef {

    public final SQLiteDatabase getWritableDatabase() {
        return null;
    }
    public final void delete() {
    }

    public static class Insert {
        public final Class<? extends TableDef> tableDef;
        public final ContentValues contentValues = new ContentValues();

        public Insert(Class<? extends TableDef> tableDef, ContentValues contentValues) {
            this.tableDef = tableDef;
        }
    }

    public static class Update extends Insert {
        public final String whereClause;
        public final String[] whereArgs;

        public Update(Class<? extends TableDef> tableDef, ContentValues contentValues, String whereClause, String... whereArgs) {
            super(tableDef, contentValues);
            this.whereClause = whereClause;
            this.whereArgs = whereArgs;
        }
    }
}
