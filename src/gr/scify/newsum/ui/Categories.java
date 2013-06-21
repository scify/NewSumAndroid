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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

public class Categories extends Activity{
	
	// Constants
	public static final String LANGUAGE_INTENT_VAR = "language";
	public static final String TO_HIDE_INTENT_VAR = "toHide"; 
	protected static final String HIDDEN_TABS_PREF = "hiddenTabs";

	ArrayList<String> alToHideCollector = new ArrayList<String>();
	protected static String[] defValuesCa = null;

	protected static ArrayList<String> getAllCategories() {
		if (defValuesCa == null)
			defValuesCa = NewSumServiceClient.readCategories("All", NewSumServiceClient.URL);
		
		return new ArrayList<String>(Arrays.asList(defValuesCa));
	}

	protected static void clearAllCategories() {
		defValuesCa = null;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Init layout
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
        
		setContentView(R.layout.categories);

		// Create item list
		final ListView list = (ListView) findViewById(R.id.listView1);
		final ArrayAdapter<String> tabnames = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_multiple_choice, getAllCategories());
		list.setAdapter(tabnames);
		list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		// Restore preferences
		
		// Get language info
		Intent iCalled = getIntent();
		String sLang = iCalled.getStringExtra(LANGUAGE_INTENT_VAR);
		if (sLang == null) {
			// Default to device default
			sLang = Locale.getDefault().getLanguage();
		}
		// Create final dummy var for inline functions
		final String sLangArg = sLang;
		
		// Init default categories
		List<String> lAllCategories = getAllCategories();
		
		// Use language information to get corresponding tab preferences
		List<String> lsToHide = getHiddenCategories(sLang);
		
		// Get "Check all" button
		final CheckedTextView checkall = (CheckedTextView) findViewById(R.id.checkedTextView1);
		// Init "Check all" option to checked 
		checkall.setChecked(true);
		// Assign listener
		checkall.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				int j = tabnames.getCount();
				checkall.setChecked(!checkall.isChecked());
				
				if (checkall.isChecked()) { // check all items
					for (int i = 0; i < j; i++) {
						list.setItemChecked(i, true);
					}
					// Clear hide collector
					alToHideCollector.clear();
				} else { // uncheck all items
					for (int i = 0; i < j; i++) {
						list.setItemChecked(i, false);
					}
					// Add all items to hide collector
					alToHideCollector.addAll(getAllCategories()); 
				}
			}
		});

		// Check items, based on user preference
		for (String sCurCategory : getAllCategories()) {
			// Check current category
			int index = lAllCategories.indexOf(sCurCategory);
			// Check it, if it is not contained in the hidden list
			list.setItemChecked(index, !lsToHide.contains(sCurCategory));
		}
		// Reset hidden tabs collector
		alToHideCollector.clear();
		// and update with hidden tabs, based on user prefs
		alToHideCollector.addAll(lsToHide);
		

		// Assign listener for item check toggle
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				// If something is unchecked
				if (!list.isItemChecked(position)) {
					// Add it to hidden list
					list.setItemChecked(position, false);
					alToHideCollector.add(list.getItemAtPosition(position).toString());
				} else {
					// else remove it from the list
					list.setItemChecked(position, true);
					alToHideCollector.remove(list.getItemAtPosition(position).toString());
				}
			}
		});

		// Set OK (save) button events
		Button ok = (Button) findViewById(R.id.AnnounceOKBtn);
		ok.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				// If everything is to be hidden
				if (alToHideCollector.size() == getAllCategories().size()) {
					// Warn and do not allow it
					Toast.makeText(Categories.this, R.string.select_or_cancel,
							Toast.LENGTH_LONG).show();
				} else {
					// Init intent
					Intent passintent = new Intent();
					Bundle userselections = new Bundle();
					// Add hidden tab list to bundle
					userselections.putStringArrayList(TO_HIDE_INTENT_VAR, alToHideCollector);
					passintent.putExtras(userselections);
					setResult(RESULT_OK, passintent);
					
					// Save settings, using language information to differentiate
					// between languages
					SharedPreferences settings = getSharedPreferences("tab" + sLangArg, 0);
					SharedPreferences.Editor editor = settings.edit();
					editor.putString(HIDDEN_TABS_PREF, alToHideCollector.toString());
					editor.commit();
					// Close dialog
					finish();
				}
			}
		});    

		// cancel
		Button cancel = (Button) findViewById(R.id.button2);
		cancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	public static List<String> getHiddenCategories(String sLang) {
		SharedPreferences settings = NewSumUiActivity.getAppContext().getSharedPreferences("tab" + sLang, 0);
		
		List<String> lsToHide = new ArrayList<String>();
		// 	get the user settings for tabs
		String sHiddenTabs = settings.getString(HIDDEN_TABS_PREF, "");
		// remove the brackets from string
		sHiddenTabs = sHiddenTabs.replace("[", "").replace("]", "");
		// convert it to a list
		if (sHiddenTabs.trim().length() > 0)
			lsToHide = Arrays.asList(sHiddenTabs.trim().split("\\s*,\\s*"));
		else
			lsToHide = new ArrayList<String>();
		return lsToHide;
	}

	public static List<String> getVisibleCategories(String sLang) {
		List<String> lAllCategories = getAllCategories();
		HashSet<String> sHiddenCategories = new HashSet<String>(getHiddenCategories(sLang));
		lAllCategories.removeAll(sHiddenCategories);
		
		return lAllCategories;
	}
}