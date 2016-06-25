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

package simpl.db.db;

import simpl.db.SimplDb;
import simpl.db.test.rules.SimplDbTestRule;

import static android.support.test.InstrumentationRegistry.getContext;

public abstract class TestDatabase extends SimplDb {
    public TestDatabase() {
        super(getContext());
    }

    public static SimplDbTestRule v(int version) {
        return new SimplDbTestRule<>(getSimplDbClass(version));
    }

    private static Class<? extends SimplDb> getSimplDbClass(int version) {
        switch (version) {
            default:
                throw new AssertionError("no database with version=" + version);
            case 1:
                return simpl.db.db.v1.DatabaseTest.class;
            case 2:
                return simpl.db.db.v2.DatabaseTest.class;
            case 3:
                return simpl.db.db.v3.DatabaseTest.class;
            case 10:
                return simpl.db.db.v10.DatabaseTest.class;
            case 21:
                return simpl.db.db.v21.DatabaseTest.class;
        }
    }
}
