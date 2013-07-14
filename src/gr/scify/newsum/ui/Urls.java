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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

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

public class Urls extends Activity {
	protected static String sSeparator = NewSumServiceClient.getFirstLevelSeparator();
	
	protected final static String URLS_PREFERENCES_TAG = "urls";
//	protected final static String URLS_SELECTIONS_INTENT_TAG = "selections";
	protected final static String HIDDEN_URLS_PREFERENCES_TAG = "hiddenURLs";
//	protected final static String HIDDEN_LINKS_PREFERENCES_TAG = "UserLinks";
	
	protected static List<String> lsHiddenURLsCollector = null;
	protected static  LinkedHashMap<String, String> hsLinksAndLabels = null;

	public static void resetURLs() {
		hsLinksAndLabels.clear();
		hsLinksAndLabels = null;
	}
	
	protected static Map<String, String> getLinksAndLabels() {
		if (hsLinksAndLabels == null) {
			String[] saLinksAndLabels = NewSumServiceClient.getLinkLabels();

			hsLinksAndLabels = NewSumServiceClient.unpackArray(saLinksAndLabels,
							NewSumServiceClient.getSecondLevelSeparator());
		}
		// get User Categories and keep only linksAndLabels that are for these
		// Categories.
		return hsLinksAndLabels;
	}

	protected static List<String> getAllURLs() {
		// Get links and labels
		Map<String, String> hsLinksAndLabels = getLinksAndLabels();

		// Keep only URLs (keys)
		ArrayList<String> lsRes = new ArrayList<String>(hsLinksAndLabels.keySet());
		return lsRes;
	}

	protected static List<String> getAllLabels() {
		// Get links and labels
		Map<String, String> hsLinksAndLabels = getLinksAndLabels();

		// Keep only URLs (keys)
		ArrayList<String> lsRes = new ArrayList<String>(hsLinksAndLabels.values());
		return lsRes;
	}

	public static String getUserVisibleURLsAsString() {
		// Get VISIBLE URLS
		List<String> lsURLs = getUserVisibleURLs();
		StringBuffer sb = new StringBuffer();
		// For every URL
		Iterator<String> isURLs = lsURLs.iterator();
		
		while (isURLs.hasNext()) {
			String sURL = isURLs.next();
			// Add to buffer
			sb.append(sURL);
			// If not the last
			if (isURLs.hasNext())
				// Add separator
				sb.append(NewSumServiceClient.getFirstLevelSeparator());
		}
		// Return list
		return sb.toString();
	}
	
	public static List<String> getUserVisibleURLs() {
		ArrayList<String> lsRes = new ArrayList<String>();
		List<String> lsHidden = getUserHiddenURLs(); 
		// For each visible category url
		for (Map.Entry<String, String> eCur : getVisibleCategoryLinksAndLabels().entrySet()) {
			// If it is not hidden
			if (!lsHidden.contains(eCur.getKey()))
				// Add to results
				lsRes.add(eCur.getKey());
		}
		// Return results
		return lsRes;
	}
	
	public static List<String> getUserVisibleLabels() {
		ArrayList<String> lsRes = new ArrayList<String>();
		List<String> lsHiddenURLs = getUserHiddenURLs(); 
		// For each visible category url
		for (Map.Entry<String, String> eCur : getVisibleCategoryLinksAndLabels().entrySet()) {
			// If it is not hidden
			if (!lsHiddenURLs.contains(eCur.getKey()))
				// Add value to results
				lsRes.add(eCur.getValue());
		}
		// Return results
		return lsRes;
	}
	
	private static List<String> getUserHiddenURLs() {
		if (lsHiddenURLsCollector  == null) {
			// Restore preferences USING language info
			String sLang = Locale.getDefault().getLanguage();
			
			// create default categories for the first time
			SharedPreferences settings = NewSumUiActivity.getAppContext(null).getSharedPreferences(
					URLS_PREFERENCES_TAG + sLang, 0);
			// Get the user hidden URLs
			String sURLs = settings.getString(HIDDEN_URLS_PREFERENCES_TAG, 
					"");
			// remove brackets
			sURLs = sURLs.replace("[", "").replace("]", "").trim();
			// convert to list		
			List<String> lsUrls;
			if (sURLs.trim().length() > 0)			
				lsUrls = new ArrayList<String>(Arrays.asList(sURLs.trim().split("\\s*,\\s*")));
			else
				lsUrls = new ArrayList<String>();
			
			// Update local veriable
			lsHiddenURLsCollector = lsUrls;
		}
		
		return lsHiddenURLsCollector;
	}
	
	public static List<String> getVisibleCategoryURLs() {
		return new ArrayList<String>(getVisibleCategoryLinksAndLabels().keySet());
	}

	public static List<String> getVisibleCategoryLabels() {
		return new ArrayList<String>(getVisibleCategoryLinksAndLabels().values());
	}
	
	public static Map<String,String> getVisibleCategoryLinksAndLabels() {
		  // Get links and labels
		  Map<String, String> hsLinksAndLabels = getLinksAndLabels();
		  
		  // Get default language
		  String sLang = Locale.getDefault().getLanguage();
		  // Get visible categories 
		  final List<String> lsVisibleCategories = Categories.getVisibleCategories(sLang);

		  // For every entry of the map between labels and feed URL
		  LinkedHashMap<String, String> hsTmpLabels = new LinkedHashMap<String, String>();		  
		  Iterator<Entry<String, String>> mit = hsLinksAndLabels.entrySet().iterator();
		  while (mit.hasNext()) {
			  Map.Entry<String, String> mp = (Map.Entry<String, String>) mit.next();
			  String tmpLink  = mp.getKey();
			  String tmpLabel = mp.getValue();
			  // For each of the visible categories
			  for (String each: lsVisibleCategories) {
				  // If the label contains the category text
				  if (tmpLabel.contains(each)) {
					  // Add link and label to visible ones
					  hsTmpLabels.put(tmpLink, tmpLabel);
				  }
			  }
		  }
		
		  return hsTmpLabels;
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
		
		SharedPreferences userlang = getSharedPreferences("lang", 0);
    	String newlang = userlang.getString("lang", Locale.getDefault().getLanguage());
        Setlanguage.updateLanguage(getApplicationContext(), newlang);
        
		setContentView(R.layout.categories);

		// Init list view
		final ListView list = (ListView) findViewById(R.id.listView1);
		// Get all labels
		List<String> lsAllLabels = getVisibleCategoryLabels();
		String[] saLabels= new String[lsAllLabels.size()]; 
		lsAllLabels.toArray(saLabels);
		// Create adapter
		final ArrayAdapter<String> urlnames = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_multiple_choice, saLabels);
		list.setAdapter(urlnames);
		list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		// Get user hidden URLs
		List<String> lsHiddenURLs = getUserHiddenURLs();
		
		// Create a "check all" control
		final CheckedTextView checkall = (CheckedTextView) findViewById(R.id.checkedTextView1);
		checkall.setChecked(true);
		// Create final arg
		final List<String> lsHiddenURLsArg = lsHiddenURLs;
		
		// Add listener
		checkall.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Toggle check
				checkall.setChecked(!checkall.isChecked());
				// Init collector
				lsHiddenURLsArg.clear();
				
				// If "check all" is checked
				int j = urlnames.getCount();
				for (int i = 0; i < j; i++) {
					// (Un)Check it accordingly 
					list.setItemChecked(i, checkall.isChecked());
				}
				// If "check all" is checked
				if (checkall.isChecked())
					// Clear hidden list
					lsHiddenURLsArg.clear();
				else
					// Add all to list
					lsHiddenURLsArg.addAll(getAllURLs());
			}		
		});

		// Init using preferences
		// For each item of the links and labels set
		int iCnt = 0;
		for (Map.Entry<String,String> eLinkAndLabel : getLinksAndLabels().entrySet()) {
			// (Un)Check accordingly
			list.setItemChecked(iCnt, !lsHiddenURLsArg.contains(eLinkAndLabel.getKey()));
			
			iCnt++;
		}

		// Add reaction to item click
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				// If not selected
				if (!list.isItemChecked(position)) {
					list.setItemChecked(position, false);
					// It should be added to hidden list
					lsHiddenURLsArg.add(getVisibleCategoryURLs().get(position));
				} else {
					list.setItemChecked(position, true);
					// else it should be removed
					lsHiddenURLsArg.remove(getVisibleCategoryURLs().get(position));
				}
			}
		});

		// save settings
		Button ok = (Button) findViewById(R.id.AnnounceOKBtn);
		ok.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				// If all is hidden
				if (getUncheckedLabels().size() == getVisibleCategoryLabels().size()) {
					// warn and do not allow exit
					Toast.makeText(Urls.this, R.string.select_or_cancel,
							Toast.LENGTH_LONG).show();
					return;
				} else {
					// If found empty categories
					List<String> lsEmptyCategs = getEmptyCategories();
					if (lsEmptyCategs.size() > 0) {
						String sMessage;
						// check if more than one
						if (lsEmptyCategs.size() > 1) {
							sMessage = getString(R.string.select_many_sources)
									+ " " + lsEmptyCategs.toString().replaceAll("\\[|\\]", "");
						} else { // if one forgotten category
							sMessage = getString(R.string.select_one_source)
									+ " " + lsEmptyCategs.get(0);
						}
						Toast.makeText(Urls.this, sMessage,
								Toast.LENGTH_LONG).show();
						return;
					}
					
					// proceed with saving user data
					Intent passintent = new Intent();
					
					// Ready results
					setResult(RESULT_OK, passintent);
					// Save preferences USING language
					String sLang = Locale.getDefault().getLanguage();
					SharedPreferences settings = getSharedPreferences(URLS_PREFERENCES_TAG + sLang, 0);
					SharedPreferences.Editor editor = settings.edit();
					
					editor.putString(HIDDEN_URLS_PREFERENCES_TAG, lsHiddenURLsArg.toString());
					editor.commit();
					finish();
				}
			}
		});    

	    //cancel settings
	    Button cancel = (Button) findViewById(R.id.button2);
	    cancel.setOnClickListener(new View.OnClickListener() {
	       public void onClick(View arg0) {
	    	   finish();
	       }
	    });
	}

	protected List<String> getCheckedLabels() {
		// Get all labels
		List<String> lsAll = new ArrayList<String>(getVisibleCategoryLabels());
		// Get user hidden urls
		List<String> lsHidden = getUserHiddenURLs();
		
		// For every link and label
		for (Map.Entry<String, String> eCur : getLinksAndLabels().entrySet()) {
			// If the link is in the hidden ones
			if (lsHidden.contains(eCur.getKey()))
				// Remove label
				lsAll.remove(eCur.getValue());
		}
		// Return remainder
		return lsAll;
	}

	protected List<String> getUncheckedLabels() {
		// Get all labels
		List<String> lsAll = new ArrayList<String>(getVisibleCategoryLabels());
		// Get user hidden urls
		List<String> lsHidden = getUserHiddenURLs();

		// For every link and label
		for (Map.Entry<String, String> eCur : getLinksAndLabels().entrySet()) {
			// If the link is shown
			if (!lsHidden.contains(eCur.getKey()))
				// Remove from Unchecked list
				lsAll.remove(eCur.getValue());
		}
		// Return remainder as unchecked list
		return lsAll;
	}
	
	protected List<String> getEmptyCategories() {
		// Get default language
		String sLang = Locale.getDefault().getLanguage();
		
		// Get all visible categories for language
		List<String> lsVisibleCategories = Categories.getVisibleCategories(sLang);
		
		// Get user visible labels
		List<String> lsVisibleLabels = getCheckedLabels();
		List<String> lsForgottenCategs = new ArrayList<String>();
		
		// For each category
		for (String sCurCategory : lsVisibleCategories) {
			boolean bFoundSomething = false;
			// For each visible label
			for (String sCurLbl : lsVisibleLabels) {
				// If a match is found
				if (sCurLbl.contains(sCurCategory)) {
					// Stop, because at least one thing contains
					// the category label
					bFoundSomething = true;
					break;
				}
			}
			// If nothing was found
			if (!bFoundSomething)
				// Add category as forgotten
				lsForgottenCategs.add(sCurCategory);
		}
		
		
		// Return list of missing categories
		return lsForgottenCategs;
		
	}
}
