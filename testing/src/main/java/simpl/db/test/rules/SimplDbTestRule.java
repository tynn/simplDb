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

package simpl.db.test.rules;

import android.database.sqlite.SQLiteDatabase;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import simpl.db.SimplDb;

public class SimplDbTestRule<D extends SimplDb> implements TestRule {
    private final Class<? extends D> mSimplDbClass;
    private D mSimplDb;

    public SimplDbTestRule(Class<? extends D> simplDbClass) {
        mSimplDbClass = simplDbClass;
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                mSimplDb = mSimplDbClass.newInstance();
                mSimplDb.delete();
                base.evaluate();
            }
        };
    }

    public D get() {
        return mSimplDb;
    }

    public SQLiteDatabase db() {
        return mSimplDb.getWritableDatabase();
    }
}
