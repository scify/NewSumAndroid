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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Log;

/** Called when the activity is first created. 
 */
public class NewSumUiActivity extends TabActivity implements Runnable, OnKeyListener {
    
	private static final String TAG = NewSumUiActivity.class.getName();
	protected static final String LAST_ANNOUNCEMENT_READ_PREF_KEY = "lastAnnouncement";	
	protected static final String LAST_ANNOUNCEMENT_READ_DATE_PREF_KEY = "date";
	public static final String GA_ENABLED = "google_analytics_enabled";
	
	public static Locale lLocale;
	
	private static Context context;
	protected static String[] saCategories;
	private ProgressDialog pd = null;
	
	@Override
	public void onStart() {
		super.onStart();
		// initially google analytics are enabled
		initAnalyticsPref();
		// check if user wants analytics
		if (getAnalyticsPref()) {
			// tracking app launches
			EasyTracker.getInstance().activityStart(this);
		}
		// check user's locale
		lLocale = this.getDefaultLocale();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		if (getAnalyticsPref()) {
			EasyTracker.getInstance().activityStop(this);
		}
	}
	
	public static Context getAppContext() {
		return context;
	}
    /**
     * 
     * @return the locale that is used by the device
     */
    public Locale getDefaultLocale() {
    	// this is the in-app locale (it changes on each language change)
//    	Locale lRes = getResources().getConfiguration().locale;
//    	Log.d("Locale : " + lRes);
    	
    	// get the device's default locale
    	Locale lDef = Locale.getDefault();
    	//    	Log.d("DEF Locale : " + lDef);
    	
    	return lDef;
    	
    }
	
	private void initAnalyticsPref() {
		SharedPreferences gAnalyticsSettings = getSharedPreferences(GA_ENABLED,
				Context.MODE_PRIVATE);
		if (!gAnalyticsSettings.contains("isGAEnabled")) {
			boolean isGAEnabled = Boolean.valueOf("true");
			gAnalyticsSettings.edit().putBoolean("isGAEnabled", isGAEnabled).commit();
		}
	}
	
	private boolean getAnalyticsPref() {
		SharedPreferences gAnalyticsSettings = getSharedPreferences(GA_ENABLED,
				Context.MODE_PRIVATE);
		boolean isGAEnabled = gAnalyticsSettings.getBoolean("isGAEnabled", true); // default is true
		return isGAEnabled;
	}

	
	
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((getResources().getConfiguration().screenLayout & 
        	    Configuration.SCREENLAYOUT_SIZE_MASK) != 
        	        Configuration.SCREENLAYOUT_SIZE_NORMAL & (getResources().getConfiguration().screenLayout & 
        	        	    Configuration.SCREENLAYOUT_SIZE_MASK) != 
                	        Configuration.SCREENLAYOUT_SIZE_SMALL) {
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        	}
        
    	// Init context
    	context = this;
    	SharedPreferences userlang = getSharedPreferences("lang", 0);
    	String newlang = userlang.getString("lang", Locale.getDefault().getLanguage());
        Setlanguage.updateLanguage(getApplicationContext(), newlang);

//    	EasyTracker.getInstance().setContext(context);
    	// Remove http keepAlive bug ( http://android-developers.blogspot.ca/2011/09/androids-http-clients.html )
    	System.setProperty("http.keepAlive", "false");
    	requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    	
        // Update theme
        SharedPreferences usertheme = getSharedPreferences("theme", 0);
		int newTheme = usertheme.getInt("theme", 2);
        Utils.onActivityCreateSetTheme(this, newTheme);

    	setContentView(R.layout.newsumui);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        
        ImageView title_image = (ImageView) findViewById(R.id.title_image);
        title_image.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse( getResources().getString(R.string.scify)));
			    startActivity( browse );
			}
        	
        });
        
        setDataSource();
    	// show progress dialog
        showWaitingDialog();
        
		new Thread(this).start();
		
    }
    
    protected void updateTopic(Bundle savedInstanceState) {
    	
    }
    
    private void showWaitingDialog() {
    	runOnUiThread( new Runnable() {
			
			@Override
			public void run() {
				if (pd == null) {
				    pd = ProgressDialog.show(NewSumUiActivity.this, "",
				    		getResources().getText(R.string.wait_msg));					
				} else {
					pd.show();
				}
			}
		});
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
		
	/**
	 * 
	 * @return true if network is available
	 */
	public boolean isNetworkAvailable() {
		

		// Check connectivity
	    ConnectivityManager cv 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = cv.getActiveNetworkInfo();
	    
	    // Check actual server URL
		boolean serverAvailable = false;
		// Make sure we have initialized the data source
		setDataSource();

		try {
			URL url = new URL(NewSumServiceClient.URL);
			url.openConnection();
			serverAvailable = true;
		} catch (Exception ex){
			serverAvailable = false;
		}
		
	    return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting() && 
	    		serverAvailable;
	}
	
	private void showAlertMessage(final int iMessageID) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				EditText keyw = (EditText) findViewById(R.id.editText1);
				keyw.setVisibility(4);
	        	AlertDialog.Builder alert = new AlertDialog.Builder(NewSumUiActivity.this);
	            alert.setMessage(iMessageID);
	            alert.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface arg0, int arg1) {
//	                    NewSumUiActivity.this.finish();
	                	finish();
//	                	NewSumUiActivity.this.finish();
	                }
	            });
	            alert.setCancelable(false);

	            alert.show();
			}
		});
	}
	/**
	 * 
	 * @param saCategs the categories enquiry to the server
	 * @return true if server returned categories
	 */
	private boolean arePresentCategories(String[] saCategs) {
		return saCategs.length != 0;
	}

//create menu for the activity
    public boolean onCreateOptionsMenu(Menu menu){
    	SharedPreferences userlang = getSharedPreferences("lang", 0);
    	String newlang = userlang.getString("lang", Locale.getDefault().getLanguage());
        Setlanguage.updateLanguage(getApplicationContext(), newlang);
        
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.newsumuiactivity, menu);
    	menu.findItem(R.id.statistics).setChecked(getAnalyticsPref());
    	// DONE: Re-enable when history is used
    	// Disable first menu item "Clear history"
    	//    	menu.getItem(0).setVisible(false);
    	
    	return true;

    	} 
//create items for the menu    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
    	SharedPreferences gAnalyticsSettings = getSharedPreferences(GA_ENABLED,
				Context.MODE_PRIVATE);
    	SharedPreferences settingscat = getSharedPreferences("tab", 0);
    	SharedPreferences settingsurl = getSharedPreferences("urls", 0);
    	SharedPreferences lang = getSharedPreferences("lang",0);
		SharedPreferences.Editor editor = lang.edit();
        switch (item.getItemId()) {
            case R.id.Categories:
                categories();
                return true;
            case R.id.ForgetVisited:
            	TopicActivity.clearVisitedTopicIDs();
            	// TODO: Also clear last announcement date?
            	return true;
            case R.id.Url:
            	urls();
            	return true;
            case R.id.L_EN:
            	Setlanguage.updateLanguage(context, "en");
				editor.putString("lang", "en");
				editor.commit();
            	settingscat.edit().clear().commit();
            	settingsurl.edit().clear().commit(); // Why twice?
            	// Force reload of categories and urls
            	Categories.clearAllCategories();
            	Urls.resetURLs();
            	finish();
            	Intent en_intent = new Intent(NewSumUiActivity.this, NewSumUiActivity.class);
            	startActivity(en_intent);
            	return true;
            case R.id.L_GR:
            	Setlanguage.updateLanguage(context, "el");
				editor.putString("lang", "el");
				editor.commit();
            	settingscat.edit().clear().commit();
            	settingsurl.edit().clear().commit(); // Why twice?
            	// Force reload of categories and URLs
            	Categories.clearAllCategories();
            	Urls.resetURLs();
            	finish();
            	Intent gr_intent = new Intent(NewSumUiActivity.this, NewSumUiActivity.class);
            	startActivity(gr_intent);
            	return true;
            case R.id.donate:
            	donate();
            	return true;
            case R.id.help:
            	help();
            	return true;
            case R.id.themes:
            	themes();
            	return true;
            case R.id.statistics:
            	if(!item.isChecked()){
                    item.setChecked(true);
        			gAnalyticsSettings.edit().putBoolean("isGAEnabled", true).commit();
        			Toast.makeText(NewSumUiActivity.this,R.string.enable_statistics,
							Toast.LENGTH_LONG).show();
            	}else{
                    item.setChecked(false);
        			gAnalyticsSettings.edit().putBoolean("isGAEnabled", false).commit();
        			Toast.makeText(NewSumUiActivity.this,R.string.disable_statistics,
							Toast.LENGTH_LONG).show();
            	}
                return true;
            	
            default:
                return super.onOptionsItemSelected(item);
        }
    }

	private void themes() {
//    	final CharSequence[] items = {"BLACK", "ORANGE", "BLUE"};
		
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(R.string.themes);
		// Init theme list
		final Map<CharSequence,Integer> mThemes = Utils.getThemeMap();
		// For 
		final CharSequence[] items = new CharSequence[mThemes.keySet().size()];
		mThemes.keySet().toArray(items);
		
    	builder.setItems(items, new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {
//    	    	int iThemePosInThemeMap = new ArrayList<Integer>(
//    	    			mThemes.values()).indexOf(which);
    	    	int iThemePosInThemeMap = mThemes.get(items[which].toString());
    	    	
				// Get pref
				SharedPreferences themenum = getSharedPreferences("theme",0);
				// Update pref
				SharedPreferences.Editor editor = themenum.edit();
				// Find the number of the theme in the map, based on the selection
				editor.putInt("theme", iThemePosInThemeMap);
				editor.commit();
				
				// Update UI
				Utils.changeToTheme( NewSumUiActivity.this, mThemes.get(items[which].toString()));
    	    }
    	});
		
    	AlertDialog alert = builder.create();
    	alert.show();
}
	private void donate() {
//		Intent donate = new Intent(this, paypal.class);
//		this.startActivityForResult(donate,0);
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
				getResources().getString(R.string.donate_uri)));
		startActivity(browserIntent);		
	}
	private void help() {
	// custom dialog
				final Dialog dialog = new Dialog(context);
				dialog.setContentView(R.layout.help);
				dialog.setTitle(R.string.help);
	 
				// set the custom dialog components - text, image and button
				final TextView text = (TextView) dialog.findViewById(R.id.textView1);
				text.setText(R.string.start_massage);
				
				final Button Buttonback = (Button) dialog.findViewById(R.id.prev);
				Buttonback.setVisibility(8);
				final Button Buttonnext = (Button) dialog.findViewById(R.id.next);
				Button Buttonok = (Button) dialog.findViewById(R.id.dialogButtonOK);
				// if button is clicked, close the custom dialog
				Buttonok.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
				
				// if button is clicked, go next
				Buttonnext.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						text.setText(R.string.view_massage);
						Buttonback.setVisibility(0);
						Buttonnext.setVisibility(8);
					}
				});
				
				// if button is clicked, go back
				Buttonback.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						text.setText(R.string.start_massage);
						Buttonback.setVisibility(8);
						Buttonnext.setVisibility(0);
					}
				});
				dialog.setCancelable(false);
				dialog.show();
			  }

//methods for each items
	private void urls() {
		Intent urlint = new Intent(this, Urls.class);
        this.startActivityForResult(urlint,0);
	}

	private void categories() {
		Intent category = new Intent(this, Categories.class);
        // Add language information
        category.putExtra(Categories.LANGUAGE_INTENT_VAR, Locale.getDefault().getLanguage());
        // Call category selection list
        this.startActivityForResult(category,0);
	}
	
	protected void resetApp() {
		// restart the activity after the user change the urls
		Intent restart = new Intent(NewSumUiActivity.this,
				NewSumUiActivity.class);
		startActivity(restart.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
		
	}

	/** 
	 * Should be run on the UI Thread.
	 */
	protected synchronized void initCategoryTabs() {
		SharedPreferences userlang = getSharedPreferences("lang", 0);
    	String newlang = userlang.getString("lang", Locale.getDefault().getLanguage());
        Setlanguage.updateLanguage(getApplicationContext(), newlang);
		//add tabs specs
		Resources res = getResources(); // Resource object to get Drawables
		final TabHost tabHost = getTabHost();  // The activity TabHost
		Configuration cfg = res.getConfiguration();
        boolean hor = cfg.orientation == Configuration.ORIENTATION_LANDSCAPE;

        if (hor) {
            TabWidget tw = tabHost.getTabWidget();
            tw.setOrientation(LinearLayout.VERTICAL);
        }
		
		if (tabHost == null) {
			Log.d("Tabhost was null. Ignoring tab host init.");
			return;
		}
		
		// Clear tabs
		tabHost.clearAllTabs();
		
        // Determine maximum category length
        int iMaxLen = 0;
        List<String> lsVisibleCategories = Categories.getVisibleCategories(Locale.getDefault().getLanguage());
        for (String sCurCategory : lsVisibleCategories) {
        	if (sCurCategory.length() > iMaxLen)
        		iMaxLen = sCurCategory.length();
        }
        iMaxLen += 2; // Add 2 spaces to limit overlap
        
        
        // Create tab for every topic
        for (String sCurCategory : lsVisibleCategories) {
	        // Create an Intent to launch an Activity for the tab 
	        Intent intent = new Intent().setClass(NewSumUiActivity.this, TopicActivity.class);
	        intent.putExtra(ViewActivity.CATEGORY_INTENT_VAR, 
	        		sCurCategory).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//this flag is for tab bug
	        // Using padded text for equal title sizes
//			TabHost.TabSpec spec = tabHost.newTabSpec(sCurCategory).setIndicator(createIndicatorView(
//					getPaddedText(sCurCategory,iMaxLen), 
//					res.getDrawable(R.drawable.ic_tab_my))).setContent(intent);
			tabHost.addTab(tabHost.newTabSpec(sCurCategory).setIndicator(createIndicatorView(
					tabHost, getPaddedText(sCurCategory,iMaxLen), 
					res.getDrawable(R.drawable.tab_indicator))).setContent(intent));	        
    	}
        
		// Reset selected tab
        tabHost.setCurrentTab(0);			
        
	}
	
	
	private View createIndicatorView(TabHost tabHost, CharSequence label, Drawable icon) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View tabIndicator = inflater.inflate(R.layout.tab_indicator,
                tabHost.getTabWidget(), // tab widget is the parent
                false); // no inflate params

        final TextView tv = (TextView) tabIndicator.findViewById(R.id.tabtitle);
        tv.setText(label);

//        final ImageView iconView = (ImageView) tabIndicator.findViewById(R.id.icon);
//        iconView.setImageDrawable(icon);

        return tabIndicator;
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		
		// Should return here after a category selection process
		if (resultCode == RESULT_OK) {
//			Bundle getselection = data.getExtras();
//			hiddenCategories = getselection.getStringArrayList(Categories.TO_HIDE_INTENT_VAR);
//			 Toast.makeText(NewSumUiActivity.this,"add "+hiddenCategories,
			// Toast.LENGTH_SHORT).show();
			resetApp();
			
		}
	}
		
	public String setDataSource() {
        NewSumServiceClient.URL =getResources().getString(R.string.urlSource);
        return NewSumServiceClient.URL;
    }
	
	public void run() {
		// if not network present
		if (!isNetworkAvailable()) {
			showAlertMessage(R.string.connection_check);
			closeWaitingDialog();
			return;
		}
		// try to fetch Categories
		saCategories = NewSumServiceClient.readCategories("All", NewSumServiceClient.URL);
		// if not server present
		if (!arePresentCategories(saCategories)) {
			showAlertMessage(R.string.server_check);
			closeWaitingDialog();
			return;
		}
		
		
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				initCategoryTabs();
			}
		});
		
        initKeyboardControls();
	    
        initSearchControls();

		closeWaitingDialog();
		
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				//DONE if app is run for 1st time, both dialogs show at the same time...
				// First show (small) help dialog
				showHelpDialog();
				// The show (bigger) announcement dialog)
				checkAndShowAnnouncement();
			}
		});
	}
	
	private void initSearchControls() {
		final EditText keyw = (EditText) findViewById(R.id.editText1);
		
	    // to trigger the enter button to go to next activity
	    keyw.setOnKeyListener(this);
		
	}
			
		private void initKeyboardControls() {
		    //for edit text 
		    final EditText keyw = (EditText) findViewById(R.id.editText1);
		    keyw.setInputType(InputType.TYPE_NULL);
		    // to control the keyboard not to pop without reason
		    keyw.setOnTouchListener(new View.OnTouchListener() {
		        public boolean onTouch(View v, MotionEvent event) {
		        	keyw.setInputType(InputType.TYPE_CLASS_TEXT);
		        	keyw.onTouchEvent(event);
		        	keyw.setText("");
		        	return true; 
				} 
		    });
		}
		
	
    	protected void checkAndShowAnnouncement() {
    		// Check announcement last data
			SharedPreferences spPrefs = 
					getSharedPreferences(LAST_ANNOUNCEMENT_READ_PREF_KEY, 0);
			long lastRead = spPrefs.getLong(LAST_ANNOUNCEMENT_READ_DATE_PREF_KEY, 0L);
			long lastUpdated = lastRead; // Init last updated to lastRead
			String sURL = getResources().getString(R.string.system_announcement_url);
			
			try {
				URL url = new URL(sURL);
				lastUpdated = Utils.lastModified(url);
				
				// If newer announcement is available
				if (lastUpdated > lastRead) {
					// Get announcement
					String sRes = Utils.getHTMLfromURL(sURL);
					// If non-empty
					if (sRes.trim().length() > 0) {
						// Show announcement
						// prepare the alert box
					    AlertDialog.Builder alertbox = new AlertDialog.Builder(NewSumUiActivity.this);
					    alertbox.setTitle(R.string.last_newsum_announcement_title);
					    alertbox.setMessage(Html.fromHtml(sRes));
					    alertbox.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
					        public void onClick(DialogInterface arg0, int arg1) {
					            
					        }
					    });
					    alertbox.setCancelable(false);
					    // Close waiting dialog
					    closeWaitingDialog();
					    // Show announcement
					    alertbox.show();									
					}
					// Update lastRead in preferences
					SharedPreferences.Editor ePrefs = spPrefs.edit();
					lastRead = lastUpdated;
					ePrefs.putLong(LAST_ANNOUNCEMENT_READ_DATE_PREF_KEY, lastUpdated);
					ePrefs.commit();
				}
			} catch (MalformedURLException e) {
				// Announcement unavailable
				android.util.Log.e(NewSumUiActivity.TAG, "Resource URL for announcement is invalid!" + e.getLocalizedMessage());
				e.printStackTrace();
				return;
			} catch (NotFoundException e) {
				// No announcement found
				android.util.Log.e(TAG, "Announcement URL not found:" + e.getLocalizedMessage());
				e.printStackTrace();
				return;
			} catch (IOException e) {
				// Announcement unavailable
				android.util.Log.e(TAG, "Announcement unavailable (connection failed):" + e.getLocalizedMessage());
				e.printStackTrace();
			}
			
    	}
    	
    	protected void showHelpDialog() {
    		// the help dialog which is shown only on the first time
			SharedPreferences setsmassage = getSharedPreferences("massage", 0);
			boolean dialogShown = setsmassage.getBoolean("dialog", false);
			 
			if (!dialogShown) {
				// prepare the alert box
			    AlertDialog.Builder alertbox = new AlertDialog.Builder(NewSumUiActivity.this);
			    alertbox.setTitle(R.string.help);
			    alertbox.setMessage(R.string.start_massage);
			    alertbox.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface arg0, int arg1) {
			            
			        }
			    });
			    alertbox.setCancelable(false);
			    alertbox.show();
			
			    SharedPreferences.Editor editor = setsmassage.edit();
			    editor.putBoolean("dialog", true);
			    editor.commit();    
			   }
    	}
	    protected String getPaddedText(String sCategory, int iTargetLen) {
	    	// Add spaces left and right continuously until we reach the target length
	    	boolean bLeft = false;
	    	StringBuffer sRes = new StringBuffer(sCategory);
	    	while (sRes.length() < iTargetLen) {
	    		if (bLeft)
	    			sRes.insert(0, " ");
	    		else
	    			sRes.append(" ");
	    		
	    		bLeft = ! bLeft;
	    	}
	    	
	    	return sRes.toString();
	    }
	    
	@Override
	public boolean onKey(final View view, int keyCode, KeyEvent event) {
		EditText keyw = (EditText) findViewById(R.id.editText1);

		if ((event.getAction() == KeyEvent.ACTION_DOWN)
				&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
			// show progress dialog on search entry
			showWaitingDialog();
			InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			// close the keyboard after user presses enter
			imm.hideSoftInputFromWindow(keyw.getWindowToken(), 0);
			// get The text that the user types
			String sSearchQuery = keyw.getText().toString();
			// get the user settings for urls
			String UserSources = Urls.getUserVisibleURLsAsString();
			// Contains The Topics for the search entry
			TopicInfo[] sSearchResults = NewSumServiceClient
					.readTopicsByKeyword(sSearchQuery, UserSources);
			// check if search results
			if (sSearchResults.length == 0) {
				closeWaitingDialog();

				// show 'no results' dialog
				final AlertDialog.Builder alert = new AlertDialog.Builder(
						NewSumUiActivity.this);

				runOnUiThread(new Runnable() {
					@Override
					public void run() {

						alert.setMessage(R.string.search_result);
						alert.setNeutralButton("Ok",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface arg0,
											int arg1) {
										return;
									}
								});
						alert.setCancelable(false);
						alert.show();
						return;
					}
				});
				return false;
			} else {
				// track the successful search entries
				if (getAnalyticsPref()) {
					EasyTracker.getTracker().sendEvent("Search",
							"Search query put", sSearchQuery, 0l);					
				}

				// send data to SearchTopicActivity
				Intent searchIntent = new Intent(view.getContext(),
						SearchTopicActivity.class);

				Bundle outgoing = new Bundle();
				// send IDs to string
				outgoing.putParcelableArray("SearchTopics", sSearchResults);
				searchIntent.putExtras(outgoing);
				// close the dialog
				closeWaitingDialog();
				// starts the activity after the search of the
				// keyword
				startActivityForResult(searchIntent, 0);
				return true;
			}
		}
		return false;
	}

}
