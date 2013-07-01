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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
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

public class SearchViewActivity extends Activity implements
		OnGesturePerformedListener {
	// private SimpleGestureFilter detector;
	private GestureLibrary gestureLib;
	public static String sTopicIds;
	protected boolean loading = false;
	protected static String sText;
	protected static String pText;
	protected static String sSeparator = NewSumServiceClient.getFirstLevelSeparator();
	protected static String sSentenceSeparator = NewSumServiceClient.getSecondLevelSeparator();
	// google analytics
	private static final String SHARING_ACTION = "Sharing"; 
	private static final String VIEW_SUMMARY_ACTION = "View Summary";
	private static final String GA_ENABLED = NewSumUiActivity.GA_ENABLED;

	protected ProgressDialog pd = null;

	private void showWaitingDialog() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (pd == null)
					// show progress dialog
					pd = ProgressDialog.show(SearchViewActivity.this, "",
							getResources().getText(R.string.wait_msg));
				else
					pd.show();
			}
		});
	}
	
	private void closeWaitingDialog() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (pd != null) {
					pd.dismiss();
				}
			}
		});
		
	}
	
	protected boolean getAnalyticsPref() {
		SharedPreferences gAnalyticsSettings = getSharedPreferences(GA_ENABLED, Context.MODE_PRIVATE);
		boolean isGAENabled = gAnalyticsSettings.getBoolean("isGAEnabled", true);
		return isGAENabled;
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
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
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
		
		// setContentView(R.layout.view);
		// TODO: Add Loading dialog

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

		// Init waiting dialog
		showWaitingDialog();
		
		initLayoutAndControls();
		
		

	}

	private void initLayoutAndControls() {
		// Init topics
		initTopicSpinner();

		// Init zoom controls
		initZoomControls();
		
		// Init rating bar
		initRatingBar();
		
		// Init selected item
		Spinner spinner = (Spinner) findViewById(R.id.spinner1);
		// take the selected topic from the TopicActivity
		Bundle extras = getIntent().getExtras();
		// Show active topic
		final int num = extras.getInt("topicNum");
		spinner.setSelection(num);

	}
	
	private void initZoomControls() {
		// Add zoom controls
		final TextView tx = (TextView) findViewById(R.id.textView1);
		// tx.setMovementMethod(LinkMovementMethod.getInstance());
		final float minm = tx.getTextSize();
		final float maxm = (minm + 24);
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

	private void initTopicSpinner() {
		// Get topics in category. Null accepts all user sources. Modify
		// according to user selection
		final String[] saTopicIDs = SearchTopicActivity.saTopicIDs;
		final String[] saTitles = SearchTopicActivity.saTopicTitles;
		final String[] saDates = SearchTopicActivity.saTopicDates;
		// TODO add TopicInfo for SearchResults as well and parse accordingly

        // final String[] saTopicIDs = extras.getStringArray("searchresults");
		final TextView title = (TextView) findViewById(R.id.title);
		// Fill topic spinner
		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
				this, android.R.layout.simple_spinner_item, saTitles);

		final TextView tx = (TextView) findViewById(R.id.textView1);
		// tx.setMovementMethod(LinkMovementMethod.getInstance());
//		final float minm = tx.getTextSize();
//		final float maxm = (minm + 24);

		// create an invisible spinner just to control the summaries of the
		// category (i will use it later on Swipe)
		Spinner spinner = (Spinner) findViewById(R.id.spinner1);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		// Scroll view init
		final ScrollView scroll = (ScrollView) findViewById(R.id.scrollView1);
		// Add selection event
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				// Show waiting dialog
				showWaitingDialog();

				// Changing summary
				loading = true;
				// Update visibility of rating bar
				final RatingBar rb = (RatingBar) findViewById(R.id.ratingBar);
				rb.setRating(0.0f);
				rb.setVisibility(View.VISIBLE);
				final TextView rateLbl = (TextView) findViewById(R.id.rateLbl);
				rateLbl.setVisibility(View.VISIBLE);

				scroll.scrollTo(0, 0);
//				String[] saTopicIDs = sTopicIds.split(sSeparator);

				SharedPreferences settings = getSharedPreferences("urls", 0);
				// get user settings for sources
				String UserSources = settings.getString("UserLinks", "All");
				String[] Summary = NewSumServiceClient.getSummary(
						saTopicIDs[arg2], UserSources);
				if (Summary.length == 0) { // TODO APPLICATION HANGS, DOES NOT
											// WORK. Updated: CHECK
					// Close waiting dialog
					closeWaitingDialog();
					
					AlertDialog.Builder alert = new AlertDialog.Builder(
							SearchViewActivity.this);
					alert.setMessage(R.string.shouldReloadSummaries);
					alert.setNeutralButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface arg0,
										int arg1) {
									startActivity(new Intent(
											getApplicationContext(),
											NewSumUiActivity.class)
											.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
								}
							});
					alert.setCancelable(false);
					alert.show();
					loading = false;
					return;
				}
				// track summary views per Search and topic title
				if (getAnalyticsPref()) {
					EasyTracker.getTracker().sendEvent(VIEW_SUMMARY_ACTION, "From Search", 
							saTitles[arg2] + ": " + saDates[arg2], 0l);
				}
				// Generate summary text
				sText = ViewActivity.generateSummaryText(Summary);
				pText = ViewActivity.generatesummarypost(Summary);
				tx.setText(Html.fromHtml(sText));
				tx.setMovementMethod(LinkMovementMethod.getInstance());
				title.setText(saTitles[arg2] + ": " + saDates[arg2]);
				float defSize = tx.getTextSize();
				SharedPreferences usersize = getSharedPreferences("textS", 0);
				float newSize = usersize.getFloat("size", defSize);
				tx.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
				// update the TopicActivity with viewed item
				TopicActivity.addVisitedTopicID(saTopicIDs[arg2]);

				// Close waiting dialog
				closeWaitingDialog();

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}

		});
		
	}

	protected void initRatingBar() {
		final TextView tx = (TextView) findViewById(R.id.textView1);
		
		// Set rating bar visibility
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
						return "summary";
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

				alParams.add(nvSummary);
				alParams.add(nvRating);

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
					Toast.makeText(SearchViewActivity.this,
							R.string.thankyou_rate, Toast.LENGTH_SHORT).show();
					rb.setVisibility(View.GONE);
					rateLbl.setVisibility(View.GONE);
					btnSubmit.setVisibility(View.GONE);
				} else
					Toast.makeText(SearchViewActivity.this, R.string.fail_rate,
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
		
		// track the copy to SD button
		if (getAnalyticsPref()) {
			EasyTracker.getTracker().sendEvent(SHARING_ACTION, "Save to SD", alphaAndDigits, 0l);
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				output.write(pText);
			} catch (IOException e) {
				// TODO Auto-generated catch block
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Toast.makeText(SearchViewActivity.this, R.string.check_sd,
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
//		EasyTracker.getInstance().activityStart(this);
	}
	
	@Override
	public void onStop() {
		super.onStop();
//		EasyTracker.getInstance().activityStop(this);
	}
	

	@Override
	public void onGesturePerformed(GestureOverlayView overlay,
			final Gesture gesture) {
		showWaitingDialog();
		new Thread(new Runnable() {

			@Override
			public void run() {
				
				String[] saTopicIDs = SearchTopicActivity.saTopicIDs;
				final Spinner spinner = (Spinner) findViewById(R.id.spinner1);
				ArrayList<Prediction> predictions = gestureLib
						.recognize(gesture);
				for (Prediction prediction : predictions) {
					if (prediction.score > 1.0) {
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

							if (spinner.getSelectedItemPosition() < saTopicIDs.length - 1) { 
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
}

class SpinnerPositionGetter implements Runnable {
	Spinner sSpinner;
	
	public SpinnerPositionGetter(Spinner spinner) {
		this.sSpinner = spinner;
	}
	public void run() {
		sSpinner.setSelection(sSpinner.getSelectedItemPosition() 
				+ 1,
				true);
	};
}
