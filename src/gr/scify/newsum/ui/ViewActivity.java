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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.google.analytics.tracking.android.EasyTracker;

public class ViewActivity extends Activity implements
		OnGesturePerformedListener, Runnable {
	
	
	// private SimpleGestureFilter detector;
	private GestureLibrary gestureLib;
	public static String Category = "";
//	protected String sSentenceSeparator;
	protected boolean loading = false;
	protected boolean nothingFound = false;
	protected int saTopicIDsLength;
	//protected final Context AppContext = this;
	protected static String sText;
	protected static String pText;
	// for google analytics
	private static final String SHARING_ACTION = "Sharing";
	private static final String VIEW_SUMMARY_ACTION = "View Summary";
	private static final String SWIPING_ACTION = "User Swipe";
	private static final String GA_ENABLED = NewSumUiActivity.GA_ENABLED;
	// for setting each Client a Unique ID
	private static final String UID_PREFS_NAME = "UID_Key_Storage";
	protected static String TOPIC_ID_INTENT_VAR = "topicID";
	protected static String CATEGORY_INTENT_VAR = "category";
	protected boolean bShowWaiting = true;
	
	
	public static String sCustomCategory = "";
	
	protected ProgressDialog pd = null;

	private void showWaitingDialog() {
		if (!bShowWaiting) {
			return;
		}
		
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (pd == null)
					// show progress dialog
					pd = ProgressDialog.show(ViewActivity.this, "",
							getResources().getText(R.string.wait_msg));
				else
					pd.show();
			}
		});
	}
	
	private void closeWaitingDialog() {
		if (!bShowWaiting) {
			return;
		}
		
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (pd != null) {
					pd.dismiss();
				}
			}
		});
		
	}
	
	protected void onResume() {
		super.onResume();

		bShowWaiting = true;
		
		// Reconnect to data source
		NewSumUiActivity.setDataSource(this);
		
		// Attach gesture filter
		initGestures();

		// Init vars
		nothingFound = false;

		initZoomControls();
		
		// Set rating bar visibility
		initRatingBar();
		
		// Update text size
		updateTextSize();
				
		showWaitingDialog();
		
		// Run thread to load data
		new Thread(this).start();
		
	}
	
	protected void onRestart() {
		super.onRestart();		
	}

	
	protected void initGestures() {
		GestureOverlayView gestureOverlayView = new GestureOverlayView(this);
		View inflate = getLayoutInflater().inflate(R.layout.view, null);
		gestureOverlayView.addView(inflate);
		gestureOverlayView.addOnGesturePerformedListener(this);
		gestureOverlayView.setGestureColor(Color.TRANSPARENT);
		gestureOverlayView.setUncertainGestureColor(Color.TRANSPARENT);
		gestureLib = GestureLibraries.fromRawResource(this, R.raw.gestures);
		if (!gestureLib.load()) {
			finish();
		}		
		setContentView(gestureOverlayView);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        
        ImageView title_image = (ImageView) findViewById(R.id.title_image);
        title_image.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse( getResources().getString(R.string.scify)));
			    startActivity( browse );
			}
        	
        });
		
		// Allow links to be followed into browser
		final TextView tx = (TextView) findViewById(R.id.textView1);
		tx.setMovementMethod(LinkMovementMethod.getInstance());
	}

	protected void showHelpDialog() {
		SharedPreferences setvmassage = getSharedPreferences("dialog", 0);
		boolean dialogShown = setvmassage.getBoolean("dialogShown", false);

		if (!dialogShown) {
			// prepare the alert box
			AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
			alertbox.setMessage(R.string.view_massage);
			alertbox.setNeutralButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0, int arg1) {
						}
					});
			alertbox.setCancelable(false);
			alertbox.show();

			SharedPreferences.Editor editor = setvmassage.edit();
			editor.putBoolean("dialogShown", true);
			editor.commit();
		}
		
	}
	
	protected void initAnalytics() {
		EasyTracker.getInstance().setContext(this);		
	}
	
	protected boolean getAnalyticsPref() {
		SharedPreferences gAnalyticsSettings = getSharedPreferences(GA_ENABLED, Context.MODE_PRIVATE);
		boolean isGAENabled = gAnalyticsSettings.getBoolean("isGAEnabled", true);
		Log.d(GA_ENABLED, String.valueOf(isGAENabled));
		return isGAENabled;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		
		// Init custom category
		sCustomCategory = NewSumUiActivity.getAppContext(this).getResources().getString(
				  R.string.custom_category);		
		// Always select landscape orientation for big screens (?)
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
	    
	}

	private void initRatingBar() {
		final TextView tx = (TextView) findViewById(R.id.textView1);
		
		final RatingBar rb = (RatingBar) findViewById(R.id.ratingBar);
		rb.setVisibility(View.VISIBLE);
		final TextView rateLbl = (TextView) findViewById(R.id.rateLbl);
		rateLbl.setVisibility(View.VISIBLE);
		final Button btnSubmit = (Button) findViewById(R.id.submitRatingBtn);
		// Set rating bar reaction
		btnSubmit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				HttpParams params = new BasicHttpParams(); // Basic params
				HttpClient client = new DefaultHttpClient(params);
				HttpPost post = new HttpPost(
						"http://scify.iit.demokritos.gr/NewSumWeb/rate.php");
				ArrayList<NameValuePair> alParams = new ArrayList<NameValuePair>();

				NameValuePair nvSummary = new NameValuePair() {

					@Override
					public String getValue() {
						return tx.getText().toString();
					}

					@Override
					public String getName() {
						return "sid";
					}
				};

				NameValuePair nvRating = new NameValuePair() {

					@Override
					public String getValue() {
						return String.valueOf(rb.getRating());
					}

					@Override
					public String getName() {
						return "rating";
					}
				};
				// added User ID
				NameValuePair nvUserID = new NameValuePair() {

					@Override
					public String getValue() {
						SharedPreferences idSettings = getSharedPreferences(UID_PREFS_NAME, Context.MODE_PRIVATE);
						String sUID = idSettings.getString("UID", "");
						return sUID;
					}

					@Override
					public String getName() {
						return "userID";
					}
				};					
				
				alParams.add(nvSummary);
				alParams.add(nvRating);
				alParams.add(nvUserID);

				boolean bSuccess = false;
				try {
					post.setEntity(new UrlEncodedFormEntity(alParams,
							HTTP.UTF_8)); // with list of key-value pairs
					client.execute(post);
					bSuccess = true;
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (bSuccess) {
					Toast.makeText(ViewActivity.this, R.string.thankyou_rate,
							Toast.LENGTH_SHORT).show();
					rb.setVisibility(View.GONE);
					rateLbl.setVisibility(View.GONE);
					btnSubmit.setVisibility(View.GONE);
				} else
					Toast.makeText(ViewActivity.this, R.string.fail_rate,
							Toast.LENGTH_SHORT).show();

			}
		});

		rb.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {

			@Override
			public void onRatingChanged(final RatingBar ratingBar,
					float rating, boolean fromUser) {
				// Ignore auto setting
				if (!fromUser)
					return;
				// Allow submit
				btnSubmit.setVisibility(View.VISIBLE);

			}
		});
		rb.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Allow submit
				btnSubmit.setVisibility(View.VISIBLE);
			}
		});
		rb.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// Allow submit
				btnSubmit.setVisibility(View.VISIBLE);
				return false;
			}
		});
		
	}

	private void initZoomControls() {
		final TextView tx = (TextView) findViewById(R.id.textView1);
		final float minm = tx.getTextSize();
		final float maxm = (minm + 24);
		
		// Add zoom controls
		ZoomControls zoom = (ZoomControls) findViewById(R.id.zoomControls1);
		zoom.setOnZoomInClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (tx.getTextSize() < maxm) {
					tx.setTextSize(TypedValue.COMPLEX_UNIT_PX,
							tx.getTextSize() + 2);
					SharedPreferences textsize = getSharedPreferences("textS",
							0);
					SharedPreferences.Editor editor = textsize.edit();
					editor.putFloat("size", tx.getTextSize());
					editor.commit();
				}
			}
		});
		zoom.setOnZoomOutClickListener(new OnClickListener() {// to the default
																// size

			@Override
			public void onClick(View v) {
				if (tx.getTextSize() > minm)
					tx.setTextSize(TypedValue.COMPLEX_UNIT_PX,
							tx.getTextSize() - 2);
				SharedPreferences textsize = getSharedPreferences("textS", 0);
				SharedPreferences.Editor editor = textsize.edit();
				editor.putFloat("size", tx.getTextSize());
				editor.commit();
			}
		});
		
	}

	// create a menu for this activity
	public boolean onCreateOptionsMenu(Menu menu) {
		SharedPreferences userlang = getSharedPreferences("lang", 0);
    	String newlang = userlang.getString("lang", Locale.getDefault().getLanguage());
        Setlanguage.updateLanguage(getApplicationContext(), newlang);

		MenuInflater inflater = getMenuInflater();

		inflater.inflate(R.menu.viewactivity, menu);
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
			menu.findItem(R.id.share).setVisible(false);
		}
		return true;

	}

	// set items for the menu
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.Copy:
			Copy();
			return true;
		case R.id.mail:
			Sendmail();
			return true;
		case R.id.share:
			share();
			return true;	
//		case R.id.sms:
//			Sendsms();
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void share() {

		TextView title = (TextView) findViewById(R.id.title);
		String sTitle = title.getText().toString();
		Intent share = new Intent(this, ShareBarActivity.class);
		share.putExtra("summaryF",""+Html.fromHtml("<h2>"+sTitle+"</h2><br>"+pText));
		share.putExtra("summaryT",sTitle );
	
		this.startActivityForResult(share,0);        
        
	}

	// set menu items methods
	private void Sendmail() {
		TextView title = (TextView) findViewById(R.id.title);
		String emailSubject = title.getText().toString();
		
		// track the Send mail action
		if (getAnalyticsPref()) {
			EasyTracker.getTracker().sendEvent(SHARING_ACTION, "Send Mail", emailSubject, 0l);
		}
		final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.setType("text/html");
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "NewSum app : "+ emailSubject);
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(sText));
		startActivity(Intent.createChooser(emailIntent, "Email:"));
	}

	private void Copy() {
//		TextView tx = (TextView) findViewById(R.id.textView1);
		TextView title = (TextView) findViewById(R.id.title);
		String sdtitle = title.getText().toString();
//		String copytext = tx.getText().toString();
		String alphaAndDigits = sdtitle.replaceAll("[^\\p{L}\\p{N}]", " ");
		Boolean isSDPresent = android.os.Environment.getExternalStorageState()
				.equals(android.os.Environment.MEDIA_MOUNTED);
		
		// track the copy to SD event
		if (getAnalyticsPref()) {
			EasyTracker.getTracker().sendEvent(SHARING_ACTION, "Save to SD", title.getText().toString(), 0l);
		}
		if (isSDPresent) {
			File folder = new File(Environment.getExternalStorageDirectory()
					+ "/NewSum");
			boolean success = false;
			if (!folder.exists()) {
				success = folder.mkdir();
			}
			if (!success) {
				// Do something on success
			} else {
				// Do something else on failure
			}
			File logFile = new File(folder, alphaAndDigits + ".txt");
			// File logFile = new
			// File(Environment.getExternalStorageDirectory().toString(),
			// alphaAndDigits+".txt");
			if (!logFile.exists()) {
				try {
					logFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			BufferedWriter output = null;
			try {
				output = new BufferedWriter(new FileWriter(logFile));
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				output.write(pText);
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {

				output.close();
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.save_massage);
				builder.setMessage(logFile.getPath());
				builder.setPositiveButton(getResources().getText(R.string.ok)
						.toString(), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				AlertDialog a = builder.create();
				a.show();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Toast.makeText(ViewActivity.this, R.string.check_sd,
					Toast.LENGTH_SHORT).show();
		}

	}

	private void Sendsms() {
		TextView title = (TextView) findViewById(R.id.title);
		String sTitle = title.getText().toString();
		String smstext = ""+Html.fromHtml("<h2>"+"NewSum app : "+ sTitle+"</h2><br>"+sText);
		
		// track the sendSms Action
		if (getAnalyticsPref()) {
			EasyTracker.getTracker().sendEvent(SHARING_ACTION, "Send SMS", title.getText().toString(), 0l);
		}
		Intent sendIntent = new Intent(Intent.ACTION_VIEW);
		sendIntent.setData(Uri.parse("sms:"));
		sendIntent.putExtra("sms_body", smstext);
		startActivity(sendIntent);

	}

	@Override
	public void onStart() {
		super.onStart();
		
	    SharedPreferences userlang = getSharedPreferences("lang", 0);
	    String newlang = userlang.getString("lang", Locale.getDefault().getLanguage());
	    Setlanguage.updateLanguage(getApplicationContext(), newlang);
		initAnalytics();
		
//		EasyTracker.getInstance().activityStart(this);
	}

	@Override
	public void onStop() { // Added for android analytics
		super.onStop();
//		EasyTracker.getInstance().activityStop(this);
	}

	@Override
	public void onGesturePerformed(GestureOverlayView overlay,
			final Gesture gesture) {
		// Get the Topic IDs per Category. Null means that all User Sources are
		// accepted
		// User Sources is a separator delimited string containing the URL feeds
		final ProgressDialog pd = ProgressDialog.show(ViewActivity.this, "",
				getResources().getText(R.string.wait_msg));
		new Thread(new Runnable() {

			@Override
			public void run() {

				final Spinner spinner = (Spinner) findViewById(R.id.spinner1);
				ArrayList<Prediction> predictions = gestureLib
						.recognize(gesture);
				for (Prediction prediction : predictions) {
					if (prediction.score > 1.0) {
						// track the swipe Action per category
						if (getAnalyticsPref()) {
							EasyTracker.getTracker().sendEvent(SWIPING_ACTION, "Normal View", Category, 0l);
						}	
						if (prediction.name.contains("right")) {
							if (spinner.getSelectedItemPosition() > 0) {
								runOnUiThread(new Runnable() {
									public void run() {
										spinner.setSelection(spinner
												.getSelectedItemPosition() - 1,
												true);

									};
								});

							}

						} else {

							if (spinner.getSelectedItemPosition() < saTopicIDsLength - 1) {
								runOnUiThread(new Runnable() {
									public void run() {
										spinner.setSelection(spinner
												.getSelectedItemPosition() + 1,
												true);
									};
								});

							}

						}
					}
				}

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						pd.dismiss();

					}
				});
			}
		}).start();
	}

	/**
	 * 
	 * @param Summary The Summary to display
	 * @return A printable html representation of the Summary
	 */
	public static String generateSummaryText(String[] Summary, Context resourceContext) {
		// Ascertain connection to data source
		NewSumUiActivity.setDataSource(resourceContext);
		
		// Get separator
		String sSentenceSeparator = NewSumServiceClient.getSecondLevelSeparator();
		
		// proceed with the Summary view
		int counter;
		counter = Utils.countDiffArticles(Summary);
		String SourceLabel = null; // The label of the Source
		String[] tmpSen = null;
		String LastSource = null;
		String Source = null;
		String LastLabel = null;
		String sText = "";
		// show summary
		if (counter == 1) {
			if (Summary.length == 2) {
				tmpSen = Summary[1].split(sSentenceSeparator);
				// For backward compatibility
				if (tmpSen.length > 3)
					SourceLabel = tmpSen[3];
				else
					try {
						SourceLabel = new URL(tmpSen[2]).getHost();
					} catch (MalformedURLException e) {
						SourceLabel = tmpSen[2];
					}
				sText += "<p>" + "&bull; " + tmpSen[0] + "<br><br>" + "<a href="
						+ tmpSen[1] + ">" + SourceLabel + "</a>" + "</br></br> "
						+ "</p>";
			} else {
				for (int i = 1; i < Summary.length; i++) {
					tmpSen = Summary[i].split(sSentenceSeparator);
					sText += "<p>" + "&bull; " + tmpSen[0] + "</p>";
				}
				// For backward compatibility
				if (tmpSen.length > 3)
					SourceLabel = tmpSen[3];
				else
					try {
						SourceLabel = new URL(tmpSen[2]).getHost();
					} catch (MalformedURLException e) {
						SourceLabel = tmpSen[2];
					}
				sText += "<br>" + "<a href=" + tmpSen[1] + ">" + SourceLabel
						+ "</a>" + "</br>";
			}
		} else {
			for (int i = 1; i < Summary.length; i++) {
				tmpSen = Summary[i].split(sSentenceSeparator);
				// sText += "<p>"+"&bull; "+ tmpSen[0] + "<br>" + tmpSen[1] +
				// "</br> "+ "</p>";
				// For backward compatibility
				if (tmpSen.length > 3)
					SourceLabel = tmpSen[3];
				else
					try {
						SourceLabel = new URL(tmpSen[2]).getHost();
					} catch (MalformedURLException e) {
						SourceLabel = tmpSen[2];
					}
				Source = tmpSen[1];
				// If first source
				if (LastSource == null) {
					// Init
					LastSource = Source;
					LastLabel = SourceLabel;
				}
				// If a new source was found,
				if (!LastSource.equals(Source)) {
					// Add the last source to the text
					sText += "<p> <a href=" + LastSource + ">" + LastLabel
							+ "</a>" + "</br> </p>";
					// Update last source
					LastSource = Source;
					LastLabel = SourceLabel;
				}
				sText += "<p>" + "&bull; " + tmpSen[0] + "</p>";
			}
			// Add last source
			sText += "<p>" + "<a href=" + Source + ">" + SourceLabel
					+ "</a></p>";
		}
		// Add All Sources at the end of the Summary
		String sInitialSources = Summary[0]; // first object of summary is the
												// (sources - labels) data
		if (sInitialSources.contains(sSentenceSeparator)) { // if more than one
															// initial sources
			String[] aInitialLabels = sInitialSources.split(sSentenceSeparator);
			sText += "<br>";
			sText += NewSumUiActivity.getAppContext(null).getResources()
					.getString(R.string.allSources)
					+ ": "; // The text to display for all the Sources
			boolean first = true;
			for (String each : aInitialLabels) {
				if (first) {
					first = false;
				} else {
					sText += " - ";
				}
				String[] tmpLabel = each.split(NewSumServiceClient
						.getThirdLevelSeparator());
				sText += "<a href=" + tmpLabel[0] + ">" + tmpLabel[1] + "</a>";
			}
			sText += "</br>";
		}
		return sText;
	}

	public static String generatesummarypost(String[] Summary, Context resourceContext) {
		// Ascertain connection to data source
		NewSumUiActivity.setDataSource(resourceContext);
		
		// Get separator
		String sSentenceSeparator = NewSumServiceClient.getSecondLevelSeparator();

		// proceed with the Summary view
				int counter;
				counter = Utils.countDiffArticles(Summary);
				String[] tmpSen = null;
				String LastSource = null;
				String Source = null;
				String sText = "";
				
				// show summary
				if (counter == 1) {
					if (Summary.length == 2) {
						tmpSen = Summary[1].split(sSentenceSeparator);
//						// For backward compatibility
//						if (tmpSen.length > 3)
//							SourceLabel = tmpSen[3];
//						else
//							try {
//								SourceLabel = new URL(tmpSen[2]).getHost();
//							} catch (MalformedURLException e) {
//								SourceLabel = tmpSen[2];
//							}
						sText += "<p>" + "&bull; " + tmpSen[0] + "<br>" 
								+ tmpSen[1] + "</br> "
								+ "</p>";
					} else {
						for (int i = 1; i < Summary.length; i++) {
							tmpSen = Summary[i].split(sSentenceSeparator);
							sText += "<p>" + "&bull; " + tmpSen[0] + "</p>";
						}
						// For backward compatibility
//						if (tmpSen.length > 3)
//							SourceLabel = tmpSen[3];
//						else
//							try {
//								SourceLabel = new URL(tmpSen[2]).getHost();
//							} catch (MalformedURLException e) {
//								SourceLabel = tmpSen[2];
//							}
						sText += "<br>" + tmpSen[1] + "</br>";
					}
				} else {
					for (int i = 1; i < Summary.length; i++) {
						tmpSen = Summary[i].split(sSentenceSeparator);
						// sText += "<p>"+"&bull; "+ tmpSen[0] + "<br>" + tmpSen[1] +
						// "</br> "+ "</p>";
//						// For backward compatibility
//						if (tmpSen.length > 3)
//							SourceLabel = tmpSen[3];
//						else
//							try {
//								SourceLabel = new URL(tmpSen[2]).getHost();
//							} catch (MalformedURLException e) {
//								SourceLabel = tmpSen[2];
//							}
						Source = tmpSen[1];
						// If first source
						if (LastSource == null) {
							// Init
							LastSource = Source;
						}
						// If a new source was found,
						if (!LastSource.equals(Source)) {
							// Add the last source to the text
							sText += "<p>" + LastSource + "</br> </p>";
							// Update last source
							LastSource = Source;
						}
						sText += "<p>" + "&bull; " + tmpSen[0] + "</p>";
					}
					// Add last source
					sText += "<p>" + Source + "</p>";
				}
				// Add All Sources at the end of the Summary
				String sInitialSources = Summary[0]; // first object of summary is the
														// (sources - labels) data
				if (sInitialSources.contains(sSentenceSeparator)) { // if more than one
																	// initial sources
					String[] aInitialLabels = sInitialSources.split(sSentenceSeparator);
					sText += "<br>";
					sText += NewSumUiActivity.getAppContext(null).getResources()
							.getString(R.string.allSources)
							+ ": "; // The text to display for all the Sources
					boolean first = true;
					for (String each : aInitialLabels) {
						if (first) {
							first = false;
						} else {
							sText += " - ";
						}
						String[] tmpLabel = each.split(NewSumServiceClient
								.getThirdLevelSeparator());
						sText +=  tmpLabel[0] ;
					}
					sText += "</br>";
				}
				
				// Append by NewSum string
				sText += "<p>" + NewSumUiActivity.getAppContext(null).getResources().getString(
						R.string.PoweredBy) + "</p>";
				return sText;
			}

	@Override
	public void run() {
		// take the String from the TopicActivity
		Bundle extras = getIntent().getExtras();
		Category = extras.getString(CATEGORY_INTENT_VAR);

		// Make sure we have updated the data source
		NewSumUiActivity.setDataSource(this);

		// Get user sources
		String sUserSources = Urls.getUserVisibleURLsAsString(ViewActivity.this);
		
		// get Topics from TopicActivity (avoid multiple server calls)
		TopicInfo[] tiTopics = TopicActivity.getTopics(sUserSources, Category, this);
		
		// Also get Topic Titles, to display to adapter
		final String[] saTopicTitles = new String[tiTopics.length];
		// Also get Topic IDs
		final String[] saTopicIDs = new String[tiTopics.length];
		// Also get Dates, in order to show in summary title
		final String[] saTopicDates = new String[tiTopics.length];
		// DeHTML titles
		for (int iCnt = 0; iCnt < tiTopics.length; iCnt++) {
			// update Titles Array
			saTopicTitles[iCnt] = Html.fromHtml(tiTopics[iCnt].getTitle())
					.toString();
			// update IDs Array
			saTopicIDs[iCnt] = tiTopics[iCnt].getID();
			// update Date Array
			saTopicDates[iCnt] = tiTopics[iCnt].getPrintableDate(NewSumUiActivity.getDefaultLocale());
		}
		// get the value of the TopicIDs list size (to use in swipe)
		saTopicIDsLength = saTopicIDs.length;
		final TextView title = (TextView) findViewById(R.id.title);
		// Fill topic spinner
		final ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
				this, android.R.layout.simple_spinner_item, saTopicTitles);

		final TextView tx = (TextView) findViewById(R.id.textView1);
//		final float minm = tx.getTextSize();
//		final float maxm = (minm + 24);

		// Get active topic
		final int num = extras.getInt(TOPIC_ID_INTENT_VAR);
		// create an invisible spinner just to control the summaries of the
		// category (i will use it later on Swipe)
		final Spinner spinner = (Spinner) findViewById(R.id.spinner1);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				spinner.setAdapter(adapter);

				// Scroll view init
				final ScrollView scroll = (ScrollView) findViewById(R.id.scrollView1);
				final String[] saTopicTitlesArg = saTopicTitles;
				final String[] saTopicIDsArg = saTopicIDs;
				final String[] SaTopicDatesArg = saTopicDates;

				// Add selection event
				spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						// Changing summary
						loading = true;
						showWaitingDialog();
						// Update visibility of rating bar
						final RatingBar rb = (RatingBar) findViewById(R.id.ratingBar);
						rb.setRating(0.0f);
						rb.setVisibility(View.VISIBLE);
						final TextView rateLbl = (TextView) findViewById(R.id.rateLbl);
						rateLbl.setVisibility(View.VISIBLE);
						scroll.scrollTo(0, 0);
						
						String UserSources = Urls.getUserVisibleURLsAsString(ViewActivity.this);

						String[] saTopicIDs = saTopicIDsArg;
					
						// track summary views per category and topic title
						if (getAnalyticsPref()) {
							EasyTracker.getTracker().sendEvent(VIEW_SUMMARY_ACTION, Category, saTopicTitlesArg[arg2], 0l);
						}
						if (sCustomCategory.trim().length() > 0) {
							if (Category.equals(sCustomCategory)) {
								Context ctxCur = NewSumUiActivity.getAppContext(
										ViewActivity.this);
								String sCustomCategoryURL = ctxCur.getResources().getString(
										R.string.custom_category_url);
								// Check if specific element needs to be read
								String sElementID = ctxCur.getResources().getString(
										  R.string.custom_category_elementId);
								// If an element needs to be selected
								if (sElementID.trim().length() > 0) {
									try {
										// Check if specific element needs to be read
										String sViewOriginalPage = ctxCur.getResources().getString(
												  R.string.custom_category_visit_source);
										// Init text by a link to the original page
										sText="<p><a href='" + sCustomCategoryURL + "'>" + sViewOriginalPage + "</a></p>";
										// Get document
										Document doc = Jsoup.connect(sCustomCategoryURL).get();
										// If a table
										Element eCur = doc.getElementById(sElementID);
										if (eCur.tagName().equalsIgnoreCase("table")) {
											// Get table rows
											Elements eRows = eCur.select("tr");
											
											// For each row
											StringBuffer sTextBuf = new StringBuffer();
											for (Element eCurRow: eRows) {
												// Append content
												// TODO: Use HTML if possible. Now problematic (crashes when we click on link)
												sTextBuf.append("<p>" + eCurRow.text() + "</p>");
											}
											// Return as string
											sText = sText + sTextBuf.toString();
										}
										else
											// else get text
											sText = eCur.text();
										
									} catch (IOException e) {
										// Show unavailable text
										sText = ctxCur.getResources().getString(
												  R.string.custom_category_unavailable);
										e.printStackTrace();
									}
									
								}
								else
									sText = Utils.getFromHttp(sCustomCategoryURL, false);
							}
																
							} else {
								// call getSummary with (sTopicID, sUserSources). Use "All" for
								// all Sources
								String[] Summary = NewSumServiceClient.getSummary(
										saTopicIDs[arg2], UserSources);
								// check if Summary exists, otherwise display message
								if (Summary.length == 0) { // DONE APPLICATION HANGS, DOES NOT
															// WORK. Updated: Probably OK
									nothingFound = true;
									AlertDialog.Builder al = new AlertDialog.Builder(
											ViewActivity.this);
									al.setMessage(R.string.shouldReloadSummaries);
									al.setNeutralButton("Ok",
											new DialogInterface.OnClickListener() {
												public void onClick(DialogInterface arg0,
														int arg1) {
													startActivity(new Intent(
															getApplicationContext(),
															NewSumUiActivity.class)
															.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
												}
											});
									al.setCancelable(false);
									al.show();
									// Return to home activity
									loading = false;
									return;
								}
								// Generate Summary text for normal categories
								sText = generateSummaryText(Summary, ViewActivity.this);
								pText = generatesummarypost(Summary, ViewActivity.this);
							}

						// Update HTML
						tx.setText(Html.fromHtml(sText));
						// Allow links to be followed into browser
						tx.setMovementMethod(LinkMovementMethod.getInstance());
						// Also Add Date to Topic Title inside Summary
						title.setText(saTopicTitlesArg[arg2] + " : "
								+ SaTopicDatesArg[arg2]);
						
						// Update size
						updateTextSize();

						// Update visited topics
						TopicActivity.addVisitedTopicID(saTopicIDs[arg2]);
						// Done
						loading = false;
						closeWaitingDialog();
					}
					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
					}

				});
				
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// Get active topic
						spinner.setSelection(num);
					}
				});
				
			}
		});
		

		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				showHelpDialog();
			}
		});
		
		closeWaitingDialog();
	}

	protected void updateTextSize() {
		final TextView tx = (TextView) findViewById(R.id.textView1);
		
		float defSize = tx.getTextSize();
		SharedPreferences usersize = ViewActivity.this.getSharedPreferences("textS", 0);
		float newSize = usersize.getFloat("size", defSize);
		tx.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
		
	}
}
