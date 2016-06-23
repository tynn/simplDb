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
import android.database.Cursor;

import org.junit.Rule;
import org.junit.Test;

import simpl.db.SimplDb.Insert;
import simpl.db.db.v1.TableTest;
import simpl.db.db.v2.ColumnTest;
import simpl.db.test.SimplDbTestRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static simpl.db.SimplDb.getName;
import static simpl.db.SimplDb.getTables;
import static simpl.db.db.v1.TableTest.DATA;
import static simpl.db.db.v1.TableTest.DROP;
import static simpl.db.db.v1.TableTest.DROP$;
import static simpl.db.db.v1.TableTest.KEEP;

public class SimplDbUpgradeTest {
    private final static String TABLE = getName(TableTest.class);

    Cursor mCursor;

    @Rule
    public SimplDbTestRule mSimpleDb1 = new SimplDbTestRule(1);

    @Rule
    public SimplDbTestRule mSimpleDb2 = new SimplDbTestRule(2);

    @Rule
    public SimplDbTestRule mSimpleDb3 = new SimplDbTestRule(3);

    @Test
    public void keepAndDropColumns() throws Exception {
        ContentValues values = new ContentValues();
        values.put(DATA, DATA);
        values.put(DROP$, false);
        values.put(KEEP, true);

        insertAndQuery(mSimpleDb1.get(), values);
        try {
            assertTrue(mCursor.moveToFirst());
            assertEquals(DATA, mCursor.getString(mCursor.getColumnIndex(DATA)));
            assertEquals(0, mCursor.getInt(mCursor.getColumnIndex(DROP)));
            assertEquals(1, mCursor.getInt(mCursor.getColumnIndex(KEEP)));
        } finally {
            mCursor.close();
        }

        mCursor = queryTable(mSimpleDb2.get());
        try {
            assertEquals(-1, mCursor.getColumnIndex(DROP));
            assertTrue(mCursor.moveToFirst());
            assertEquals(DATA, mCursor.getString(mCursor.getColumnIndex(DATA)));
            assertEquals(1, mCursor.getInt(mCursor.getColumnIndex(KEEP)));
        } finally {
            mCursor.close();
        }
    }

    @Test
    public void dropTable() throws Exception {
        ContentValues values = new ContentValues();
        values.put(DATA, DATA);

        insertAndQuery(mSimpleDb1.get(), values);
        try {
            assertTrue(mCursor.moveToFirst());
            assertEquals(DATA, mCursor.getString(mCursor.getColumnIndex(DATA)));
        } finally {
            mCursor.close();
        }

        final String table = getName(ColumnTest.class);
        assertTrue(getTables(mSimpleDb2.db()).contains(table));
        assertFalse(getTables(mSimpleDb3.db()).contains(table));

        mCursor = queryTable(mSimpleDb3.get());
        try {
            assertTrue(mCursor.moveToFirst());
            assertEquals(DATA, mCursor.getString(mCursor.getColumnIndex(DATA)));
        } finally {
            mCursor.close();
        }
    }

    private void insertAndQuery(SimplDb db, ContentValues values) throws Exception {
        mCursor = null;
        db.insert(TableTest.class, values, new Insert.Callback() {
            @Override
            public void onInsertFinished(long rowId, Insert insert, SimplDb db) {
                mCursor = queryTable(db);
            }
        });
        while (mCursor == null)
            Thread.sleep(100);
    }

    static Cursor queryTable(SimplDb db) {
        return db.getReadableDatabase().query(TABLE, null, null, null, null, null, null);
    }
}
