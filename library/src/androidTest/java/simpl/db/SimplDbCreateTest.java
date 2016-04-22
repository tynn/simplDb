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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;

import simpl.db.table.v1.DatabaseTest;
import simpl.db.table.v1.TableTest;

import static android.database.DatabaseUtils.queryNumEntries;
import static android.support.test.InstrumentationRegistry.getContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static simpl.db.SimplDb.getName;

public class SimplDbCreateTest {

    @BeforeClass
    public static void deleteAllDatabases() {
        SimplDbTest.deleteAllDatabases();
    }

    @Test
    public void createAndDeleteDatabase() {
        Context context = getContext();
        SimplDb simplDb = new DatabaseTest(context);
        assertEquals(0, context.databaseList().length);
        SQLiteDatabase db = simplDb.getReadableDatabase();
        try {
            assertTrue(Arrays.asList(context.databaseList()).contains(simplDb.name));
            assertEquals(0, queryNumEntries(db, getName(TableTest.class)));
        } finally {
            db.close();
            simplDb.delete();
            assertEquals(0, context.databaseList().length);
        }
    }
}
