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
import android.support.test.filters.SdkSuppress;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import simpl.db.db.TestDatabase;
import simpl.db.db.v21.TableTest;
import simpl.db.test.rules.SimplDbTestRule;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static simpl.db.SimplDb.getName;

@SdkSuppress(minSdkVersion = LOLLIPOP)
public class SimplDbLollipopTest {
    static final String[] rows = {"rowid"};

    @Rule
    public SimplDbTestRule mSimplDb = TestDatabase.v(21);

    @Rule
    public ExpectedException failure = ExpectedException.none();

    @Test
    public void rowid() throws Exception {
        mSimplDb.db().query(getName(TableTest.MI.class), rows, null, null, null, null, null).close();
        failure.expect(SQLException.class);
        mSimplDb.db().query(getName(TableTest.OI.class), rows, null, null, null, null, null).close();
    }
}
