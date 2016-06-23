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

import android.content.Context;

import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;

import simpl.db.db.v1.TableTest;
import simpl.db.test.SimplDbTestRule;

import static android.database.DatabaseUtils.queryNumEntries;
import static android.support.test.InstrumentationRegistry.getContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static simpl.db.SimplDb.getName;

public class SimplDbCreateTest {

    @Rule
    public SimplDbTestRule mSimplDb = new SimplDbTestRule(1);

    @Test
    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    public void createAndDeleteDatabase() {
        Context context = getContext();
        assertEquals(0, context.databaseList().length);
        try {
            mSimplDb.db();
            assertTrue(Arrays.asList(context.databaseList()).contains(mSimplDb.get().name));
            assertEquals(0, queryNumEntries(mSimplDb.db(), getName(TableTest.class)));
        } finally {
            mSimplDb.get().delete();
            assertEquals(0, context.databaseList().length);
        }
    }
}
