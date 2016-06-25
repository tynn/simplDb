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

import android.database.Cursor;

import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import simpl.db.SimplDb.Insert;
import simpl.db.SimplDb.Insert.Callback;
import simpl.db.api.TableDef;
import simpl.db.db.TestDatabase;
import simpl.db.db.v2.ColumnTest;
import simpl.db.db.v2.ConstraintTest;
import simpl.db.db.v2.ForeignKeyTest;
import simpl.db.db.v2.TableTest;
import simpl.db.db.v2.TypeTest;
import simpl.db.test.rules.InsertTestRule;
import simpl.db.test.rules.SimplDbTestRule;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static simpl.db.SimplDb.getName;
import static simpl.db.db.v2.ColumnTest.CHECK;
import static simpl.db.db.v2.ColumnTest.CHECK$;
import static simpl.db.db.v2.ColumnTest.COLLATE;
import static simpl.db.db.v2.ColumnTest.COLLATE$;
import static simpl.db.db.v2.ColumnTest.COLUMN;
import static simpl.db.db.v2.ColumnTest.DEFAULT;
import static simpl.db.db.v2.ColumnTest.NOT_NULL;
import static simpl.db.db.v2.ColumnTest.UNIQUE;
import static simpl.db.db.v2.ColumnTest.UNIQUE$;
import static simpl.db.db.v2.ConstraintTest.KEY1;
import static simpl.db.db.v2.ConstraintTest.KEY2;
import static simpl.db.db.v2.ConstraintTest.KEY3;
import static simpl.db.db.v2.ConstraintTest.KEY4;
import static simpl.db.db.v2.ForeignKeyTest.FOREIGN_KEY1;
import static simpl.db.db.v2.ForeignKeyTest.FOREIGN_KEY2;
import static simpl.db.db.v2.ForeignKeyTest.FOREIGN_KEY3;
import static simpl.db.db.v2.TableTest.DATA;
import static simpl.db.db.v2.TableTest.INFO;
import static simpl.db.db.v2.TableTest.KEEP;
import static simpl.db.db.v2.TableTest.KEY;
import static simpl.db.db.v2.TableTest.VALUE;
import static simpl.db.db.v2.TypeTest.BLOB;
import static simpl.db.db.v2.TypeTest.INTEGER;
import static simpl.db.db.v2.TypeTest.NUMERIC;
import static simpl.db.db.v2.TypeTest.REAL;
import static simpl.db.db.v2.TypeTest.TEXT;

public class SimplDbTest {
    static final String COLUMN_TABLE = getName(ColumnTest.class);
    static final String CONSTRAINT_TABLE = getName(ConstraintTest.class);
    static final String FOREIGN_KEY_TABLE = getName(ForeignKeyTest.class);
    static final String TABLE_TABLE = getName(TableTest.class);
    static final String TYPE_TABLE = getName(TypeTest.class);

    Cursor mCursor;

    @Rule
    public InsertTestRule mColumnInsert = new InsertTestRule(ColumnTest.class);

    @Rule
    public InsertTestRule mConstraintInsert = new InsertTestRule(ConstraintTest.class);

    @Rule
    public InsertTestRule mForeignKeyInsert = new InsertTestRule(ForeignKeyTest.class);

    @Rule
    public InsertTestRule mTableInsert = new InsertTestRule(TableTest.class);

    @Rule
    public InsertTestRule mTypedInsert = new InsertTestRule(TypeTest.class);

    @Rule
    public SimplDbTestRule mSimplDb = TestDatabase.v(2);

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
            assertEquals(0, mCursor.getCount());
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
            assertEquals(0, mCursor.getCount());
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
    public void columnCollate() throws Exception {
        mColumnInsert.contentValues.put(NOT_NULL, "");
        mColumnInsert.contentValues.put(CHECK$, 1);
        mColumnInsert.contentValues.put(COLLATE$, "TODO");
        insertAndQuery(mColumnInsert, COLUMN_TABLE);

        try {
            assertTrue(mCursor.moveToFirst());
            assertEquals("TODO", mCursor.getString(mCursor.getColumnIndex(COLLATE)));
        } finally {
            mCursor.close();
        }

        query(COLUMN_TABLE, COLLATE$ + "=?", "todo");

        try {
            assertTrue(mCursor.moveToFirst());
            assertEquals("TODO", mCursor.getString(mCursor.getColumnIndex(COLLATE)));
        } finally {
            mCursor.close();
        }
    }

    @Test
    public void columnForeignKey() throws Exception {
        testForeignKey(FOREIGN_KEY1);
    }

    @Test
    public void constraintPrimaryKey() throws Exception {
        mConstraintInsert.contentValues.put(KEY1, 1);
        mConstraintInsert.contentValues.put(KEY2, 2);
        mConstraintInsert.contentValues.put(KEY4, 3);
        insertAndQuery(mConstraintInsert, CONSTRAINT_TABLE);

        mCursor.close();
        mConstraintInsert.contentValues.put(KEY4, 4);
        insertAndQuery(mConstraintInsert, CONSTRAINT_TABLE);

        try {
            assertEquals(1, mCursor.getCount());
            assertTrue(mCursor.moveToFirst());
            assertEquals(1, mCursor.getInt(mCursor.getColumnIndex(KEY1)));
            assertEquals(2, mCursor.getInt(mCursor.getColumnIndex(KEY2)));
            assertEquals(3, mCursor.getInt(mCursor.getColumnIndex(KEY4)));
        } finally {
            mCursor.close();
        }
    }

    @Test
    public void constraintUnique() throws Exception {
        mConstraintInsert.contentValues.put(KEY1, 1);
        mConstraintInsert.contentValues.put(KEY2, 2);
        mConstraintInsert.contentValues.put(KEY3, 3);
        insertAndQuery(mConstraintInsert, CONSTRAINT_TABLE);

        mCursor.close();
        mConstraintInsert.contentValues.put(KEY1, 2);
        mConstraintInsert.contentValues.put(KEY2, 3);
        insertAndQuery(mConstraintInsert, CONSTRAINT_TABLE);

        try {
            assertEquals(1, mCursor.getCount());
            assertTrue(mCursor.moveToFirst());
            assertEquals(1, mCursor.getInt(mCursor.getColumnIndex(KEY1)));
            assertEquals(2, mCursor.getInt(mCursor.getColumnIndex(KEY2)));
            assertEquals(3, mCursor.getInt(mCursor.getColumnIndex(KEY3)));
        } finally {
            mCursor.close();
        }
    }

    @Test
    public void constraintCheck() throws Exception {
        mConstraintInsert.contentValues.put(KEY1, 1);
        mConstraintInsert.contentValues.put(KEY2, 2);
        mConstraintInsert.contentValues.put(KEY4, 7);
        insertAndQuery(mConstraintInsert, CONSTRAINT_TABLE);

        try {
            assertEquals(0, mCursor.getCount());
        } finally {
            mCursor.close();
        }

        mConstraintInsert.contentValues.put(KEY4, 42);
        insertAndQuery(mConstraintInsert, CONSTRAINT_TABLE);

        try {
            assertTrue(mCursor.moveToFirst());
            assertEquals(42, mCursor.getInt(mCursor.getColumnIndex(KEY4)));
        } finally {
            mCursor.close();
        }
    }

    @Test
    public void constraintForeignKey() throws Exception {
        testForeignKey(FOREIGN_KEY3);
    }

    @Test
    public void tablePrimaryKey() throws Exception {
        mTableInsert.contentValues.put(KEY, 2);
        mTableInsert.contentValues.put(INFO, "first");
        insertAndQuery(mTableInsert, TABLE_TABLE);

        mCursor.close();
        mTableInsert.contentValues.put(INFO, "second");
        insertAndQuery(mTableInsert, TABLE_TABLE);

        try {
            assertEquals(1, mCursor.getCount());
            assertTrue(mCursor.moveToFirst());
            assertEquals(2, mCursor.getInt(mCursor.getColumnIndex(KEY)));
            assertEquals("first", mCursor.getString(mCursor.getColumnIndex(INFO)));
        } finally {
            mCursor.close();
        }
    }

    @Test
    public void tableUnique() throws Exception {
        mTableInsert.contentValues.put(VALUE, 2);
        mTableInsert.contentValues.put(INFO, "first");
        insertAndQuery(mTableInsert, TABLE_TABLE);

        mCursor.close();
        mTableInsert.contentValues.put(INFO, "second");
        insertAndQuery(mTableInsert, TABLE_TABLE);

        try {
            assertEquals(1, mCursor.getCount());
            assertTrue(mCursor.moveToFirst());
            assertEquals(2, mCursor.getInt(mCursor.getColumnIndex(VALUE)));
            assertEquals("first", mCursor.getString(mCursor.getColumnIndex(INFO)));
        } finally {
            mCursor.close();
        }
    }

    @Test
    public void tableCheck() throws Exception {
        mTableInsert.contentValues.put(INFO, "test");
        insertAndQuery(mTableInsert, TABLE_TABLE);

        try {
            assertEquals(0, mCursor.getCount());
        } finally {
            mCursor.close();
        }

        mTableInsert.contentValues.put(INFO, "TEST");
        insertAndQuery(mTableInsert, TABLE_TABLE);

        try {
            assertTrue(mCursor.moveToFirst());
            assertEquals("TEST", mCursor.getString(mCursor.getColumnIndex(INFO)));
        } finally {
            mCursor.close();
        }
    }

    @Test
    public void tableForeignKey() throws Exception {
        testForeignKey(FOREIGN_KEY2);
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
        for (Class<? extends TableDef> table : mSimplDb.get().mTableDefs)
            expected.add(getName(table));

        HashSet<String> tables = new HashSet<>(SimplDb.getTables(mSimplDb.db()));
        assertTrue(tables.containsAll(expected));
    }


    private void testForeignKey(String foreignKey) throws Exception {
        mForeignKeyInsert.contentValues.put(foreignKey, 1);
        insertAndQuery(mForeignKeyInsert, FOREIGN_KEY_TABLE);

        try {
            assertEquals(0, mCursor.getCount());
        } finally {
            mCursor.close();
        }

        mTypedInsert.contentValues.put(INTEGER, 1);
        insertAndQuery(mTypedInsert, TYPE_TABLE);

        try {
            assertEquals(1, mCursor.getCount());
        } finally {
            mCursor.close();
        }

        insertAndQuery(mForeignKeyInsert, FOREIGN_KEY_TABLE);

        try {
            assertTrue(mCursor.moveToFirst());
            assertEquals(1, mCursor.getInt(mCursor.getColumnIndex(foreignKey)));
        } finally {
            mCursor.close();
        }

        assertTrue(mSimplDb.db().delete(TYPE_TABLE, "1", null) > 0);

        query(COLUMN_TABLE, null);

        try {
            assertEquals(0, mCursor.getCount());
        } finally {
            mCursor.close();
        }
    }

    @Test
    public void getColumns() throws Exception {
        HashSet<String> columns = new HashSet<>(SimplDb.getColumns(mSimplDb.db(), getName(TableTest.class)));
        assertEquals(new HashSet<>(Arrays.asList(DATA, KEY, INFO, KEEP, VALUE)), columns);
    }

    void insertAndQuery(Insert insert, final String table) throws InterruptedException {
        mCursor = null;
        mSimplDb.get().insert(insert, new Callback() {
            @Override
            public void onInsertFinished(long rowId, Insert insert, SimplDb db) {
                query(table, null);
            }
        });
        while (mCursor == null)
            Thread.sleep(100);
    }

    void query(String table, String selection, String... args) {
        mCursor = mSimplDb.db().query(table, null, selection, args, null, null, null);
    }
}
