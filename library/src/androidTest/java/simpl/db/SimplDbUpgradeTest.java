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
import android.content.Context;
import android.database.Cursor;

import org.junit.Before;
import org.junit.Test;

import simpl.db.SimplDb.Insert;
import simpl.db.table.v1.TableTest;
import simpl.db.table.v2.ColumnTest;

import static android.support.test.InstrumentationRegistry.getContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static simpl.db.SimplDb.getName;
import static simpl.db.SimplDb.getTables;
import static simpl.db.table.v1.TableTest.DATA;
import static simpl.db.table.v1.TableTest.DROP;
import static simpl.db.table.v1.TableTest.DROP$;
import static simpl.db.table.v1.TableTest.KEEP;

public class SimplDbUpgradeTest {
    private final static String TABLE = getName(TableTest.class);

    SimplDb mSimpleDb;
    Cursor mCursor;

    @Before
    public void deleteAllDatabases() {
        SimplDbTest.deleteAllDatabases();
    }

    @Test
    public void keepAndDropColumns() throws Exception {
        ContentValues values = new ContentValues();
        values.put(DATA, DATA);
        values.put(DROP$, false);
        values.put(KEEP, true);

        createDb(1);
        try {
            insertAndQuery(mSimpleDb, values);
            try {
                assertTrue(mCursor.moveToFirst());
                assertEquals(DATA, mCursor.getString(mCursor.getColumnIndex(DATA)));
                assertEquals(0, mCursor.getInt(mCursor.getColumnIndex(DROP)));
                assertEquals(1, mCursor.getInt(mCursor.getColumnIndex(KEEP)));
            } finally {
                mCursor.close();
            }
        } finally {
            mSimpleDb.close();
        }

        createDb(2);
        try {
            mCursor = queryTable(mSimpleDb);
            try {
                assertEquals(-1, mCursor.getColumnIndex(DROP));
                assertTrue(mCursor.moveToFirst());
                assertEquals(DATA, mCursor.getString(mCursor.getColumnIndex(DATA)));
                assertEquals(1, mCursor.getInt(mCursor.getColumnIndex(KEEP)));
            } finally {
                mCursor.close();
            }
        } finally {
            mSimpleDb.close();
        }
    }

    @Test
    public void dropTable() throws Exception {
        ContentValues values = new ContentValues();
        values.put(DATA, DATA);

        createDb(1);
        try {
            insertAndQuery(mSimpleDb, values);
            try {
                assertTrue(mCursor.moveToFirst());
                assertEquals(DATA, mCursor.getString(mCursor.getColumnIndex(DATA)));
            } finally {
                mCursor.close();
            }
        } finally {
            mSimpleDb.close();
        }

        final String table = getName(ColumnTest.class);
        createDb(2);
        try {
            assertTrue(getTables(mSimpleDb.getReadableDatabase()).contains(table));
        } finally {
            mSimpleDb.close();
        }
        createDb(3);
        try {
            assertFalse(getTables(mSimpleDb.getReadableDatabase()).contains(table));
        } finally {
            mSimpleDb.close();
        }

        createDb(3);
        try {
            mCursor = queryTable(mSimpleDb);
            try {
                assertTrue(mCursor.moveToFirst());
                assertEquals(DATA, mCursor.getString(mCursor.getColumnIndex(DATA)));
            } finally {
                mCursor.close();
            }
        } finally {
            mSimpleDb.close();
        }
    }

    private void insertAndQuery(SimplDb db, ContentValues values) throws Exception {
        mCursor = null;
        db.insert(TableTest.class, values, new Insert.Callback() {
            @Override
            public void onInsertionFinished(long rowId, Insert insert, SimplDb db) {
                mCursor = queryTable(db);
            }
        });
        while (mCursor == null)
            Thread.sleep(100);
    }

    static Cursor queryTable(SimplDb db) {
        return db.getReadableDatabase().query(TABLE, null, null, null, null, null, null);
    }

    private void createDb(int version) {
        Context context = getContext();
        switch (version) {
            case 3:
                mSimpleDb = new simpl.db.table.v3.DatabaseTest(context);
                break;
            case 2:
                mSimpleDb = new simpl.db.table.v2.DatabaseTest(context);
                break;
            case 1:
            default:
                mSimpleDb = new simpl.db.table.v1.DatabaseTest(context);
        }
    }
}
