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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import simpl.db.SimplQuery.Filter;
import simpl.db.api.Join;
import simpl.db.api.Query;
import simpl.db.api.QueryDef;
import simpl.db.db.v10.JoinTest;
import simpl.db.db.v10.QueryTest;
import simpl.db.db.v10.TableTest;
import simpl.db.test.SimplDbTestRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static simpl.db.SimplQuery.get;
import static simpl.db.api.JoinType.CROSS;
import static simpl.db.api.JoinType.LEFT;
import static simpl.db.api.Sortorder.DESC;

public class SimplQueryTest {
    Cursor mCursor;

    @Rule
    public SimplDbTestRule mSimplDb = new SimplDbTestRule(10);

    @Before
    public void setupDatabase() {
        ContentValues values = new ContentValues();
        setupTableTest(values);
        values.clear();
        setupQueryTest(values);
        values.clear();
        setupJoinTest(values);
    }

    @After
    public void closeCursor() {
        if (mCursor != null && !mCursor.isClosed())
            mCursor.close();
    }

    private void setupTableTest(ContentValues values) {
        values.put(TableTest.DATA, "payload");
        mSimplDb.get().insert(TableTest.class, values, null);
        sleep(50);
        mSimplDb.get().insert(TableTest.class, values, null);
    }

    @Test
    public void queryTable() throws Exception {
        mCursor = get(TableTest.class).exec(mSimplDb.get(), null);
        assertEquals(2, mCursor.getCount());
        assertEquals(2, mCursor.getColumnCount());
    }

    private void insertToQueryTest(ContentValues values, String key, String value, int ref) {
        values.put(QueryTest.KEY, key);
        values.put(QueryTest.VALUE, value);
        values.put(QueryTest.REF, ref);
        mSimplDb.get().insert(QueryTest.class, values, null);
    }

    @Query(table = QueryTest.class, columns = {QueryTest.KEY, QueryTest.VALUE})
    private interface QueryTestQuery extends QueryDef {
    }

    private void setupQueryTest(ContentValues values) {
        insertToQueryTest(values, "foo", "bar", 1);
        insertToQueryTest(values, "Foo", "Bar", 2);
        insertToQueryTest(values, "fOo", "bar", 1);
        insertToQueryTest(values, "FOo", "BAr", 5);
        insertToQueryTest(values, "FOO", "BAR", 13);
    }

    @Test
    public void query() throws Exception {
        mCursor = get(QueryTestQuery.class).exec(mSimplDb.get(), null);
        assertEquals(5, mCursor.getCount());
        assertEquals(2, mCursor.getColumnCount());
        final List<String> columns = Arrays.asList(QueryTest.KEY, QueryTest.VALUE);
        assertTrue(Arrays.asList(mCursor.getColumnNames()).containsAll(columns));
    }

    @Test
    public void queryFilter() throws Exception {
        Filter filter = new Filter();
        filter.setLimit(1);
        filter.setSelection(QueryTest.VALUE + "=?", "bar");
        filter.setOrderBy(QueryTest._ID + ' ' + DESC);
        mCursor = get(QueryTestQuery.class).exec(mSimplDb.get(), filter);
        assertTrue(mCursor.moveToFirst());
        assertEquals(2, mCursor.getColumnCount());
        final int column = mCursor.getColumnIndex(QueryTest.KEY);
        assertEquals("fOo", mCursor.getString(column));
    }

    private void insertToJoinTest(ContentValues values, String extra, int ref) {
        values.put(JoinTest.EXTRA, extra);
        values.put(JoinTest.REF, ref);
        mSimplDb.get().insert(JoinTest.class, values, null);
    }

    @Query(table = QueryTest.class, columns = {QueryTest.KEY, QueryTest.VALUE})
    @Join(table = JoinTest.class, columns = {JoinTest.EXTRA}, on = JoinTestQuery.ON)
    private interface JoinTestQuery extends QueryDef {
        String ON = "%1$s." + QueryTest.REF + "=%2$s." + JoinTest._ID;
    }

    private void setupJoinTest(ContentValues values) {
        insertToJoinTest(values, "baz", 1);
        insertToJoinTest(values, "Baz", 2);
        insertToJoinTest(values, "bAz", 1);
        insertToJoinTest(values, "baZ", 1);
        insertToJoinTest(values, "BAz", 4);
        insertToJoinTest(values, "bAZ", 1);
        insertToJoinTest(values, "BaZ", 9);
        insertToJoinTest(values, "BAZ", 13);
    }

    @Test
    public void queryJoin() throws Exception {
        mCursor = get(JoinTestQuery.class).exec(mSimplDb.get(), null);
        assertEquals(4, mCursor.getCount());
        assertEquals(3, mCursor.getColumnCount());
        final int valueColumn = mCursor.getColumnIndex(QueryTest.VALUE);
        final int extraColumn = mCursor.getColumnIndex(JoinTest.EXTRA);
        while (mCursor.moveToNext()) {
            String extra = mCursor.getString(extraColumn);
            String value = mCursor.getString(valueColumn);
            assertEquals(value, extra.replace('z', 'r'));
        }
    }

    @Query(table = QueryTest.class, columns = {QueryTest.KEY, QueryTest.VALUE})
    @Join(type = LEFT, table = JoinTest.class, columns = {JoinTest.EXTRA}, on = JoinTestQuery.ON)
    private interface LeftJoinTestQuery extends QueryDef {
    }

    @Test
    public void queryLeftJoin() throws Exception {
        mCursor = get(LeftJoinTestQuery.class).exec(mSimplDb.get(), null);
        assertEquals(5, mCursor.getCount());
        assertEquals(3, mCursor.getColumnCount());
        final int valueColumn = mCursor.getColumnIndex(QueryTest.VALUE);
        final int extraColumn = mCursor.getColumnIndex(JoinTest.EXTRA);
        while (mCursor.moveToNext()) {
            String extra = mCursor.getString(extraColumn);
            if (extra != null) {
                String value = mCursor.getString(valueColumn);
                assertEquals(value, extra.replace('z', 'r'));
            }
        }
    }

    @Query(table = QueryTest.class, columns = {QueryTest.KEY, QueryTest.VALUE})
    @Join(type = CROSS, table = JoinTest.class, columns = {JoinTest.EXTRA}, on = CrossJoinTestQuery.ON)
    private interface CrossJoinTestQuery extends QueryDef {
        String ON = "%1$s." + QueryTest.REF + "=%2$s." + JoinTest.REF;
    }

    @Test
    public void queryCrossJoin() throws Exception {
        mCursor = get(CrossJoinTestQuery.class).exec(mSimplDb.get(), null);
        assertEquals(10, mCursor.getCount());
        assertEquals(3, mCursor.getColumnCount());
        final int valueColumn = mCursor.getColumnIndex(QueryTest.VALUE);
        final int extraColumn = mCursor.getColumnIndex(JoinTest.EXTRA);
        while (mCursor.moveToNext()) {
            String extra = mCursor.getString(extraColumn);
            int extraFirstLower = 0;
            for (char c : extra.toCharArray()) {
                if (Character.isLowerCase(c))
                    break;
                extraFirstLower++;
            }
            String value = mCursor.getString(valueColumn);
            int valueFirstLower = 0;
            for (char c : value.toCharArray()) {
                if (Character.isLowerCase(c))
                    break;
                valueFirstLower++;
            }
            assertEquals(valueFirstLower, extraFirstLower);
        }
    }

    @SuppressWarnings("EmptyCatchBlock")
    private void sleep(int millis) {
        try {
            synchronized (this) {
                wait(millis);
            }
        } catch (InterruptedException e) {
        }
    }
}
