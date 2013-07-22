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
import gr.scify.newsum.Utils;
import gr.scify.newsum.structs.TopicInfo;

import java.util.Locale;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class SearchTopicActivity extends Activity {
	public static String sTopicIds = "";
	// call for topicsView/
	TopicAdapter adapter;
	static String[] saTopicTitles;
	static String[] saTopicDates;
	static String[] saTopicIDs;
	private ProgressDialog pd = null;
	
	@Override
	public void onStart() {
		super.onStart();
	}
	@Override
	public void onStop() {
		super.onStop();
	}
	private void showWaitingDialog() {
		if (pd == null)
	    	// show progress dialog        
		    pd = ProgressDialog.show(SearchTopicActivity.this, "",
	    		getResources().getText(R.string.wait_msg));
		else
			pd.show();
	}
	
	private void closeWaitingDialog() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (pd != null)
					pd.dismiss();								
			}
		});
		
	}
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
		
		SharedPreferences usertheme = getSharedPreferences("theme", 0);
		int newTheme = usertheme.getInt("theme", 2);
	    Utils.onActivityCreateSetTheme(this,newTheme);
	    
	    SharedPreferences userlang = getSharedPreferences("lang", 0);
    	String newlang = userlang.getString("lang", Locale.getDefault().getLanguage());
        Setlanguage.updateLanguage(getApplicationContext(), newlang);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.searchtopic);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        
        ImageView title_image = (ImageView) findViewById(R.id.title_image);
        title_image.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse( getResources().getString(R.string.scify)));
			    startActivity( browse );
			}
        	
        });
        
		ListView list = (ListView) findViewById(R.id.search_list);
		list.setCacheColorHint(0);
		// Getting the Bundle object that pass from NewSumActivity EditText
		Bundle incoming = getIntent().getExtras();
		// String[] input = incoming.getStringArray("msg");
		// get the Topics fetched
		Parcelable[] parcels = incoming.getParcelableArray("SearchTopics");
		// cast to TopicInfo[] //classCastException
		TopicInfo[] tiaTopics = new TopicInfo[parcels.length];
		for (int i = 0; i < parcels.length; i ++) {
			tiaTopics[i] = (TopicInfo) parcels[i];
		}
		// unpack data 
		saTopicIDs = new String[tiaTopics.length];
		saTopicTitles = new String[tiaTopics.length];
		saTopicDates = new String[tiaTopics.length];
		for (int i = 0; i < tiaTopics.length; i++) {
			saTopicIDs[i] = tiaTopics[i].getID();
			saTopicTitles[i] = tiaTopics[i].getTitle();
			saTopicDates[i] = tiaTopics[i].getPrintableDate(NewSumUiActivity.getDefaultLocale());
		}

		adapter = new TopicAdapter(this, saTopicTitles);
		list.setAdapter(adapter);

		// get what item of the list the user click, send it to the
		// ViewActivity and start the activity
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				showWaitingDialog();
				int num = position;
				Intent lvIntent = new Intent(view.getContext(),
						SearchViewActivity.class);
				
				// Update visited
//				TopicActivity.addVisitedTopicID(saTopicIDs[num]);
				
				lvIntent.putExtra("topicNum", num);// send user click to
													// SearchViewActivity
//				lvIntent.putExtra("Topic_IDs", saTopicIDs);
				closeWaitingDialog();
				startActivityForResult(lvIntent, 0);
			}

		});
		}
	}

//}
