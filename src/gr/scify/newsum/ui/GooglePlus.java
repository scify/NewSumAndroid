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

import gr.scify.newsum.ui.PlusClientFragment.OnSignedInListener;

import java.io.InputStream;
import java.net.URL;

import com.google.android.gms.plus.GooglePlusUtil;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.PlusShare;
import com.google.android.gms.plus.model.people.Person;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class GooglePlus extends FragmentActivity
implements View.OnClickListener, OnSignedInListener {
	
private static final String TAG_ERROR_DIALOG_FRAGMENT = "errorDialog";
public static final int REQUEST_CODE_PLUS_CLIENT_FRAGMENT = 0;
private static final int REQUEST_CODE_RESOLVE_GOOGLE_PLUS_ERROR = 2;
private TextView mSignInStatus;
private ImageView mprofile;
private PlusClientFragment mSignInFragment;
String summaryF;

@Override
protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.googleplus);
	
	Bundle extras = getIntent().getExtras();
	summaryF=extras.getString("summaryF");


	mSignInFragment = PlusClientFragment.getPlusClientFragment(this, MomentUtil.VISIBLE_ACTIVITIES);

	findViewById(R.id.gpost).setEnabled(false);
	findViewById(R.id.gpost).setOnClickListener(this);
	findViewById(R.id.sign_in_button).setOnClickListener(this);
	findViewById(R.id.sign_out_button).setOnClickListener(this);
	mSignInStatus = (TextView) findViewById(R.id.sign_in_status);
	mprofile = (ImageView) findViewById(R.id.profile_image);
	}

@Override
public void onClick(View view) {
	final int errorCode = GooglePlusUtil.checkGooglePlusApp(this);
	switch(view.getId()) {
    case R.id.sign_out_button:
        resetAccountState();
        mSignInFragment.signOut();
        break;
    case R.id.sign_in_button:
        	mSignInFragment.signIn(REQUEST_CODE_PLUS_CLIENT_FRAGMENT);
        break;
    case R.id.gpost:
        if (errorCode == GooglePlusUtil.SUCCESS) {
        	Intent shareIntent = new PlusShare.Builder(this)
            .setType("text/plain")
            .setText(summaryF)
            .setContentUrl(Uri.parse("https://developers.google.com/+/"))
            .getIntent();
        startActivityForResult(shareIntent, 0);
        } else {
            // Prompt the user to install the Google+ app.
            GooglePlusErrorDialogFragment
                    .create(errorCode, REQUEST_CODE_RESOLVE_GOOGLE_PLUS_ERROR)
                    .show(getSupportFragmentManager(), TAG_ERROR_DIALOG_FRAGMENT);
        }  
        break;
        }
	}

@Override
protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
	mSignInFragment.handleOnActivityResult(requestCode, responseCode, intent);
	}

@Override
public void onSignedIn(PlusClient plusClient) {
	mSignInStatus.setText(getString(R.string.signed_in_status));

	// We can now obtain the signed-in user's profile information.
	Person currentPerson = plusClient.getCurrentPerson();
	if (currentPerson != null) {
		String greeting = getString(R.string.greeting_status, currentPerson.getDisplayName());
		mSignInStatus.setText(greeting);    
		Drawable drawable = LoadImageFromWebOperations(currentPerson.getImage().getUrl());    
		mprofile.setImageDrawable(drawable);    
		findViewById(R.id.sign_in_button).setVisibility(8);
		findViewById(R.id.sign_out_button).setVisibility(0);
		findViewById(R.id.gpost).setEnabled(true);
		
	} else {
		resetAccountState();
		}
	}

private Drawable LoadImageFromWebOperations(String url){
	try{
		InputStream is = (InputStream) new URL(url).getContent();
		Drawable d = Drawable.createFromStream(is, "src name");
		return d;
	}catch (Exception e) {
		System.out.println("Exc="+e);
		return null;
		}
	}
private void resetAccountState() {
	mprofile.setImageResource(R.drawable.person);
	findViewById(R.id.gpost).setEnabled(false);
	findViewById(R.id.sign_out_button).setVisibility(8);
	findViewById(R.id.sign_in_button).setVisibility(0);
	mSignInStatus.setText(getString(R.string.signed_out_status));
	}
}
