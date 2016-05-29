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

import android.app.Activity;
import android.support.test.espresso.IdlingResource;
import android.view.View;
import android.widget.ListView;

import static simpl.db.ex.min.MainActivity.LIST_VIEW_ID;

class IsListFilled implements IdlingResource {
    private final Activity mActivity;

    private boolean mIsListFilled;
    private ResourceCallback mCallback;

    public IsListFilled(MainActivity activity) {
        mActivity = activity;
    }

    @Override
    public String getName() {
        return "islistfilled";
    }

    @Override
    public boolean isIdleNow() {
        return mIsListFilled || isListFilled();
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        mCallback = callback;
    }

    private boolean isListFilled() {
        if (!isListFilled(mActivity.findViewById(LIST_VIEW_ID)))
            return false;
        if (mCallback != null)
            mCallback.onTransitionToIdle();
        return mIsListFilled = true;
    }

    private static boolean isListFilled(View view) {
        return view instanceof ListView && ((ListView) view).getAdapter().getCount() > 2;
    }
}
