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
import gr.scify.newsum.structs.TopicInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class TopicActivity extends ListActivity implements IVisitedChecker, Runnable {
	
	public static String Category = "";
	static final String VISITED_PREF_GROUP = "Visited";
	static final String VISITED_TOPIC_IDS_PREF_NAME = "TopicIDs";
	static final int MAX_VISITED_ENTRIES = 100;
	private static final String UID_PREFS_NAME = "UID_Key_Storage";
	
	private static TopicInfo[] tiTopics;
	private static List<String> lsVisitedTopicIDs;
	
	protected ProgressDialog pd = null;
	
	static TopicAdapter adapter;
	
	@Override
	public void onStart() {
		super.onStart();
	}
	@Override
	public void onStop() {
		super.onStop();
	}
	
	/***
	 * Returns the set of visited topic IDs as a List of Strings.
	 * @return The list of visited topic IDs.
	 */
	public static synchronized List<String> getVisitedTopicIDs() {
		// Use caching
		if (lsVisitedTopicIDs != null)
			return lsVisitedTopicIDs;
		
		// Read setting
		SharedPreferences settings = NewSumUiActivity.getAppContext(null).getSharedPreferences(VISITED_PREF_GROUP, 0);
	    String sVisitedLinks = settings.getString(VISITED_TOPIC_IDS_PREF_NAME, "");
	    // Check zero size
	    if (sVisitedLinks.length() == 0) {
	    	lsVisitedTopicIDs = new ArrayList<String>();
	    	return lsVisitedTopicIDs;
	    }
	    
	    // Split setting
	    ArrayList<String> lsRes = new ArrayList<String>(Arrays.asList(sVisitedLinks.split(
	    		NewSumServiceClient.getFirstLevelSeparator())));

	    // Assign to caching variable
		lsVisitedTopicIDs = lsRes;
	    // Return as set
	    return lsRes;
	}

	public static void clearVisitedTopicIDs() {
		// Clear cache
		synchronized (lsVisitedTopicIDs) {
			lsVisitedTopicIDs = null;			
		}
		
		// Init setting
		SharedPreferences settings = NewSumUiActivity.getAppContext(null).getSharedPreferences(VISITED_PREF_GROUP, 0);
		// Edit state
		Editor eToChange = settings.edit();
	    // Update
		eToChange.putString(VISITED_TOPIC_IDS_PREF_NAME, "");
		eToChange.commit();
		
		// If adapter is not null
		if (adapter != null)
			adapter.notifyDataSetChanged();
	}
	
	public static void addVisitedTopicID(String sTopicID) {
		ArrayList<String> ssCurVisited = new ArrayList<String>(getVisitedTopicIDs());
		
		// Init setting
		SharedPreferences settings = NewSumUiActivity.getAppContext(null).getSharedPreferences(VISITED_PREF_GROUP, 0);
		// Check if item is already present
		if (ssCurVisited.contains(sTopicID))
			// If so, return
			return;
		
		// else we need to append
		// Append to cache
		synchronized (lsVisitedTopicIDs) {
			lsVisitedTopicIDs.add(sTopicID);
		}

		// If we have reached the full allowed size 
		if (ssCurVisited.size() >= MAX_VISITED_ENTRIES) {
			// Remove oldest
			ssCurVisited.remove(0);
		}
		// Add new item
		ssCurVisited.add(sTopicID);
		
		// Encode as string
		Iterator<String> iCur = ssCurVisited.iterator();
		// Init buffer
		StringBuffer sbJoinedTopicIDs = new StringBuffer();
		// For each item
		while (iCur.hasNext()) {
			// Get it
			String sCur = iCur.next();
			// Append it
			sbJoinedTopicIDs.append(sCur);
			// If not last
			if (iCur.hasNext())
				// add separator
				sbJoinedTopicIDs.append(NewSumServiceClient.getFirstLevelSeparator());
		}
		
		// Edit state
		Editor eToChange = settings.edit();
	    // Update
		eToChange.putString(VISITED_TOPIC_IDS_PREF_NAME, sbJoinedTopicIDs.toString());
		eToChange.commit();
		
		if (adapter != null) {			
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public boolean hasBeenVisited(String sVisited) {
		return (getVisitedTopicIDs().contains(sVisited));
	}
	// call for topicsView/
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences userlang = getSharedPreferences("lang", 0);
    	String newlang = userlang.getString("lang", Locale.getDefault().getLanguage());
        Setlanguage.updateLanguage(getApplicationContext(), newlang);
		// get unique ID for this client
		SharedPreferences idSettings = getSharedPreferences(UID_PREFS_NAME,
				Context.MODE_PRIVATE);
		if (!idSettings.contains("UID")) {
			String sUID = String.valueOf(UUID.randomUUID());
			idSettings.edit().putString("UID", sUID).commit();
		}
		// Get active category
		Bundle extras = getIntent().getExtras();
		Category = extras.getString("category");
		
		// Show progress dialog
		showWaitingDialog();
		
		// DONE: Run thread
		Thread tMainCommands = new Thread(this);
		tMainCommands.start();
	}

	public static TopicInfo[] getTopics(String sUserSources) {
		return tiTopics != null ? tiTopics : NewSumServiceClient.readTopics(sUserSources, Category);
	}

	private void showWaitingDialog() {
		if (pd == null)
	    	// show progress dialog        
		    pd = ProgressDialog.show(TopicActivity.this, "",
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
	public void run() {
		
		String sUserSources = Urls.getUserVisibleURLsAsString();// get the
																		// user
																		// Sources
		// Use sorted topics // Topics are already sorted from the server
		tiTopics = NewSumServiceClient.readTopics(sUserSources, Category);

		// DONE: Support grayed out (visited) topics
		adapter = new TopicAdapter(this, tiTopics);
		adapter.setVisitedChecker(this); // Make sure we check visited links

		// ArrayAdapter<String> aaTopic = new ArrayAdapter<String>(this,
		// R.layout.topic_item, NewSumServiceClient.readTopics(Category));
		// setListAdapter(aaTopic);
		final TopicAdapter adapterArg = adapter;
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				setListAdapter(adapterArg);
				// get what item of the list the user click, send it to the ViewActivity
				// and start the activity
				ListView list = getListView();
				list.setCacheColorHint(0);
				list.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						int num = position;

						Intent lvIntent = new Intent(view.getContext(),
								ViewActivity.class);
						lvIntent.putExtra(ViewActivity.TOPIC_ID_INTENT_VAR, num);
						lvIntent.putExtra(ViewActivity.CATEGORY_INTENT_VAR, Category);
						showWaitingDialog();

						// Update list of visited topics
//						addVisitedTopicID((String) (adapterArg.getItem(position)));

						closeWaitingDialog();
						startActivityForResult(lvIntent, 0);

					}

				});
				closeWaitingDialog();
			}
		});

	}

	}