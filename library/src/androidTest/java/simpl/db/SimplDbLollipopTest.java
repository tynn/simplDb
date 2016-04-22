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

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.filters.SdkSuppress;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import simpl.db.table.v21.DatabaseTest;
import simpl.db.table.v21.TableTest;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.support.test.InstrumentationRegistry.getContext;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static simpl.db.SimplDb.getName;

@SdkSuppress(minSdkVersion = LOLLIPOP)
public class SimplDbLollipopTest {
    SimplDb mSimplDb;

    @BeforeClass
    public static void deleteAllDatabases() {
        SimplDbTest.deleteAllDatabases();
    }

    @Before
    public void createDatabase() {
        mSimplDb = new DatabaseTest(getContext());
    }

    @After
    public void deleteDatabase() {
        mSimplDb.delete();
    }

    @Test
    public void rowid() throws Exception {
        final String[] rows = {"rowid"};
        SQLException error = null;
        SQLiteDatabase db = mSimplDb.getReadableDatabase();

        try {
            db.query(getName(TableTest.MI.class), rows, null, null, null, null, null).close();
        } catch (SQLException e) {
            error = e;
        } finally {
            assertNull(error);
        }

        try {
            db.query(getName(TableTest.OI.class), rows, null, null, null, null, null).close();
        } catch (SQLException e) {
            error = e;
        } finally {
            assertNotNull(error);
        }
    }
}
