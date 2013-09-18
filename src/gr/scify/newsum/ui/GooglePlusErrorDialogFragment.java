/*
 * Copyright 2013 SciFY NPO <info@scify.org>.
 *
 * This product is part of the NewSum Free Software.
 * For more information about NewSum visit
 * 
 * 	http://www.scify.gr/site/en/our-projects/completed-projects/newsum-menu-en
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * If this code or its output is used, extended, re-engineered, integrated, 
 * or embedded to any extent in another software or hardware, there MUST be 
 * an explicit attribution to this work in the resulting source code, 
 * the packaging (where such packaging exists), or user interface 
 * (where such an interface exists). 
 * The attribution must be of the form "Powered by NewSum, SciFY"
 */ 


package gr.scify.newsum.ui;

import com.google.android.gms.plus.GooglePlusUtil;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Wraps the {@link Dialog} returned by {@link GooglePlusUtil#getErrorDialog}
 * so that it can be properly managed by the {@link android.app.Activity}.
 */
public final class GooglePlusErrorDialogFragment extends DialogFragment {

    /**
     * The error code returned by the
     * {@link GooglePlusUtil#checkGooglePlusApp(android.content.Context)} call.
     */
    public static final String ARG_ERROR_CODE = "errorCode";

    /**
     * The request code given when calling {@link android.app.Activity#startActivityForResult}.
     */
    public static final String ARG_REQUEST_CODE = "requestCode";

    /**
     * Creates a {@link DialogFragment}.
     */
    public GooglePlusErrorDialogFragment() {}

    /**
     * Returns a {@link Dialog} created by {@link GooglePlusUtil#getErrorDialog} with the
     * provided errorCode, activity, and request code.
     *
     * @param savedInstanceState Not used.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        return GooglePlusUtil.getErrorDialog(args.getInt(ARG_ERROR_CODE), getActivity(),
                args.getInt(ARG_REQUEST_CODE));
    }

    /**
     * Create a {@link DialogFragment} for displaying the {@link GooglePlusUtil#getErrorDialog}.
     * @param errorCode The error code returned by
     *              {@link GooglePlusUtil#checkGooglePlusApp(android.content.Context)}
     * @param requestCode The request code for resolving the resolution activity.
     * @return The {@link DialogFragment}.
     */
    public static DialogFragment create(int errorCode, int requestCode) {
        DialogFragment fragment = new GooglePlusErrorDialogFragment();
        Bundle args = new Bundle();
        args.putInt(GooglePlusErrorDialogFragment.ARG_ERROR_CODE, errorCode);
        args.putInt(GooglePlusErrorDialogFragment.ARG_REQUEST_CODE, requestCode);
        fragment.setArguments(args);
        return fragment;
    }
}
