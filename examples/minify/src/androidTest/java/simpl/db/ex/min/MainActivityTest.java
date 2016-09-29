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

package simpl.db.ex.min;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.EspressoException;
import android.support.test.espresso.IdlingResource;
import android.support.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static simpl.db.ex.min.ExamplDb.getName;

@Ignore("These UI tests fail on CI")
public class MainActivityTest {
    private IdlingResource mIdlingResource;

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void registerIntentServiceIdlingResource() {
        mIdlingResource = new IsListFilled(mActivityRule.getActivity());
        Espresso.registerIdlingResources(mIdlingResource);
    }

    @After
    public void unregisterIntentServiceIdlingResource() {
        Espresso.unregisterIdlingResources(mIdlingResource);
        mIdlingResource = null;
    }

    @Test
    public void randomNameNotDisplayed() throws Exception {
        try {
            assertNameDisplayed(ExamplDb.class.getSimpleName());
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(EspressoException.class));
        }
    }

    @Test
    public void names() throws Exception {
        assertEquals("exampl_db", getName(ExamplDb.class));
        assertNameDisplayed(getName(ExamplDb.class));
        assertEquals("exampl_table", getName(ExamplTable.class));
        assertNameDisplayed(getName(ExamplTable.class));
    }

    @Test
    public void loader() throws Exception {
        assertNameDisplayed(ExamplTable.NAME);
        assertNameDisplayed(ExamplTable.VALUE);
        assertNameDisplayed(ExamplTable._ID);
        assertNameDisplayed(ExamplTable._TIMESTAMP);
    }

    private static void assertNameDisplayed(String name) {
        onData(hasToString(endsWith(name))).check(matches(isDisplayed()));
    }
}
