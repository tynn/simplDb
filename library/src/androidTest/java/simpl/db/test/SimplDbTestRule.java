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

package simpl.db.test;

import android.database.sqlite.SQLiteDatabase;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import simpl.db.SimplDb;

public class SimplDbTestRule implements TestRule {
    private final int mVersion;
    private SimplDb mSimplDb;

    @SuppressWarnings("unchecked")
    public SimplDbTestRule(int version) {
        mVersion = version;
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                mSimplDb = createDb(mVersion);
                base.evaluate();
                mSimplDb.delete();
                mSimplDb = null;
            }
        };
    }

    public SimplDb get() {
        return mSimplDb;
    }

    public SQLiteDatabase db() {
        return mSimplDb.getWritableDatabase();
    }


    private static SimplDb createDb(int version) {
        switch (version) {
            default:
            case 1:
                return new simpl.db.db.v1.DatabaseTest();
            case 2:
                return new simpl.db.db.v2.DatabaseTest();
            case 3:
                return new simpl.db.db.v3.DatabaseTest();
            case 10:
                return new simpl.db.db.v10.DatabaseTest();
            case 21:
                return new simpl.db.db.v21.DatabaseTest();
        }
    }
}
