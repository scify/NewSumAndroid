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

import gr.scify.newsum.Setlanguage;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

public class ShareBarActivity extends Activity {

	// SocialAuth Component
//	SocialAuthAdapter adapter;
//	boolean status;
//	Profile profileMap;
//	List<Photo> photosList;

	// Android Components
	String summaryF;
	String summaryT;
	
	// for google analytics
//	private static final String SHARING_ACTION = "Sharing";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if ((getResources().getConfiguration().screenLayout & 
        	    Configuration.SCREENLAYOUT_SIZE_MASK) != 
        	        Configuration.SCREENLAYOUT_SIZE_NORMAL & (getResources().getConfiguration().screenLayout & 
        	        	    Configuration.SCREENLAYOUT_SIZE_MASK) != 
                	        Configuration.SCREENLAYOUT_SIZE_SMALL) {
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        	}
		
		SharedPreferences userlang = getSharedPreferences("lang", 0);
    	String newlang = userlang.getString("lang", Locale.getDefault().getLanguage());
        Setlanguage.updateLanguage(getApplicationContext(), newlang);
        
		setContentView(R.layout.sharebar);
		
		Bundle extras = getIntent().getExtras();
		summaryF=extras.getString("summaryF");
		summaryT=extras.getString("summaryT");
		LinearLayout bar = (LinearLayout) findViewById(R.id.linearbar);
		bar.setBackgroundResource(R.drawable.bar_gradient);
		
		Button facebookButton = (Button) findViewById(R.id.facebook_button);
		facebookButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				Intent share = new Intent(v.getContext(), Facebook.class);
				share.putExtra("summaryF",summaryF);
				startActivityForResult(share,0);        
			}
		});
		
		Button googleButton = (Button) findViewById(R.id.googleplus_button);
		googleButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				Intent share = new Intent(v.getContext(), GooglePlus.class);
				share.putExtra("summaryF",summaryF);
				startActivityForResult(share,0);        
			}
		});

	}
}