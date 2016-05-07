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
import android.database.sqlite.SQLiteDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import simpl.db.SimplDb.Insert;
import simpl.db.SimplDb.Insert.Callback;
import simpl.db.table.TableDef;
import simpl.db.table.v2.ColumnTest;
import simpl.db.table.v2.DatabaseTest;
import simpl.db.table.v2.TableTest;
import simpl.db.table.v2.TypeTest;

import static android.support.test.InstrumentationRegistry.getContext;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static simpl.db.SimplDb.getName;
import static simpl.db.table.v2.ColumnTest.CHECK;
import static simpl.db.table.v2.ColumnTest.CHECK$;
import static simpl.db.table.v2.ColumnTest.COLUMN;
import static simpl.db.table.v2.ColumnTest.DEFAULT;
import static simpl.db.table.v2.ColumnTest.NOT_NULL;
import static simpl.db.table.v2.ColumnTest.UNIQUE;
import static simpl.db.table.v2.ColumnTest.UNIQUE$;
import static simpl.db.table.v2.TableTest.DATA;
import static simpl.db.table.v2.TableTest.INFO;
import static simpl.db.table.v2.TableTest.KEEP;
import static simpl.db.table.v2.TableTest.VALUE;
import static simpl.db.table.v2.TypeTest.BLOB;
import static simpl.db.table.v2.TypeTest.INTEGER;
import static simpl.db.table.v2.TypeTest.NUMERIC;
import static simpl.db.table.v2.TypeTest.REAL;
import static simpl.db.table.v2.TypeTest.TEXT;

public class SimplDbTest {
    static final String COLUMN_TABLE = getName(ColumnTest.class);
    static final String TYPE_TABLE = getName(TypeTest.class);

    final Insert mColumnInsert = new Insert(ColumnTest.class, new ContentValues());
    final Insert mTypedInsert = new Insert(TypeTest.class, new ContentValues());

    SimplDb mSimplDb;
    Cursor mCursor;

    @BeforeClass
    public static void deleteAllDatabases() {
        Context context = getContext();
        for (String database : context.databaseList())
            context.deleteDatabase(database);
    }

    @Before
    public void createDatabase() {
        mSimplDb = new DatabaseTest(getContext());
        mColumnInsert.contentValues.clear();
        mTypedInsert.contentValues.clear();
    }

    @After
    public void deleteDatabase() {
        mSimplDb.delete();
    }

    @Test
    public void columns() throws Exception {
        mColumnInsert.contentValues.put(NOT_NULL, NOT_NULL);
        mColumnInsert.contentValues.put(CHECK$, 1);
        mColumnInsert.contentValues.put(UNIQUE$, 2);
        insertAndQuery(mColumnInsert, COLUMN_TABLE);

        try {
            assertTrue(mCursor.moveToFirst());
            assertNull(mCursor.getString(mCursor.getColumnIndex(COLUMN)));
            assertEquals(DEFAULT, mCursor.getString(mCursor.getColumnIndex(DEFAULT)));
            assertEquals(NOT_NULL, mCursor.getString(mCursor.getColumnIndex(NOT_NULL)));
            assertEquals(2, mCursor.getInt(mCursor.getColumnIndex(UNIQUE)));
            assertEquals(1, mCursor.getInt(mCursor.getColumnIndex(CHECK)));
        } finally {
            mCursor.close();
        }
    }

    @Test
    public void columnNotNull() throws Exception {
        mColumnInsert.contentValues.putNull(NOT_NULL);
        insertAndQuery(mColumnInsert, COLUMN_TABLE);

        try {
            assertFalse(mCursor.moveToFirst());
        } finally {
            mCursor.close();
        }
    }

    @Test
    public void columnCheck() throws Exception {
        mColumnInsert.contentValues.put(NOT_NULL, "");
        mColumnInsert.contentValues.put(CHECK$, 3);
        insertAndQuery(mColumnInsert, COLUMN_TABLE);

        try {
            assertFalse(mCursor.moveToFirst());
        } finally {
            mCursor.close();
        }
    }

    @Test
    public void columnUnique() throws Exception {
        mColumnInsert.contentValues.put(NOT_NULL, "");
        mColumnInsert.contentValues.put(CHECK$, 1);
        mColumnInsert.contentValues.put(UNIQUE$, 2);
        insertAndQuery(mColumnInsert, COLUMN_TABLE);

        mCursor.close();
        mColumnInsert.contentValues.put(CHECK$, 2);
        insertAndQuery(mColumnInsert, COLUMN_TABLE);

        try {
            assertEquals(1, mCursor.getCount());
            assertTrue(mCursor.moveToFirst());
            assertEquals(2, mCursor.getInt(mCursor.getColumnIndex(CHECK)));
        } finally {
            mCursor.close();
        }
    }

    @Test
    public void types() throws Exception {
        final byte[] bytes = {'h', 'a', 'l', 'l', 'o'};
        mTypedInsert.contentValues.put(BLOB, bytes);
        mTypedInsert.contentValues.put(INTEGER, 1);
        mTypedInsert.contentValues.put(NUMERIC, true);
        mTypedInsert.contentValues.put(REAL, 3.14);
        mTypedInsert.contentValues.put(TEXT, TEXT);
        insertAndQuery(mTypedInsert, TYPE_TABLE);

        try {
            assertTrue(mCursor.moveToFirst());
            assertArrayEquals(bytes, mCursor.getBlob(mCursor.getColumnIndex(BLOB)));
            assertEquals(1, mCursor.getInt(mCursor.getColumnIndex(INTEGER)));
            assertEquals(1, mCursor.getInt(mCursor.getColumnIndex(NUMERIC)));
            assertEquals(3.14, mCursor.getFloat(mCursor.getColumnIndex(REAL)), 0.01);
            assertEquals(TEXT, mCursor.getString(mCursor.getColumnIndex(TEXT)));
        } finally {
            mCursor.close();
        }
    }

    @Test
    public void getTables() throws Exception {
        HashSet<String> expected = new HashSet<>();
        for (Class<? extends TableDef> table : mSimplDb.mTableDefs)
            expected.add(getName(table));

        SQLiteDatabase db = mSimplDb.getReadableDatabase();
        HashSet<String> tables = new HashSet<>(SimplDb.getTables(db));
        assertTrue(tables.containsAll(expected));
    }

    @Test
    public void getColumns() throws Exception {
        SQLiteDatabase db = mSimplDb.getReadableDatabase();
        HashSet<String> columns = new HashSet<>(SimplDb.getColumns(db, getName(TableTest.class)));
        assertEquals(new HashSet<>(Arrays.asList(DATA, INFO, KEEP, VALUE)), columns);
    }

    private void insertAndQuery(Insert insert, final String table) throws InterruptedException {
        mCursor = null;
        mSimplDb.insert(insert, new Callback() {
            @Override
            public void onInsertFinished(long rowId, Insert insert, SimplDb db) {
                mCursor = db.getReadableDatabase().query(table, null, null, null, null, null, null);
            }
        });
        while (mCursor == null)
            Thread.sleep(100);
    }
}
