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

package gr.scify.newsum;

import gr.scify.newsum.structs.TopicInfo;
import gr.scify.newsum.ui.NewSumServiceClient;
import gr.scify.newsum.ui.NewSumUiActivity;
import gr.scify.newsum.ui.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.analytics.tracking.android.EasyTracker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;

public class Utils {
	private static int sTheme;
	private static java.util.Map<CharSequence, Integer> mThemesToIDs = null;
	private static java.util.Map<CharSequence, Integer> mThemesToResourceIDs = null;
	
	public static CharSequence getThemeFromThemeID(int iThemeID, Activity activity) {
		for (Map.Entry<CharSequence, Integer> eCur : getThemeMap(activity).entrySet()) {
			if (eCur.getValue() == iThemeID)
				return eCur.getKey();
		}
		
		return null;
	}
	
	public static void resetThemeMaps() {
		mThemesToIDs = null;
		mThemesToResourceIDs = null;
	}

	public static java.util.Map<CharSequence, Integer> getThemeMap(Activity activity) {
		if (mThemesToIDs == null) {
			mThemesToIDs = new java.util.TreeMap<CharSequence, Integer>();
			Resources rRes = NewSumUiActivity.getAppContext(activity).getResources();
			mThemesToIDs.put(rRes.getString(R.string.theme_black_name), 0);
			mThemesToIDs.put(rRes.getString(R.string.theme_orange_name), 1);
			mThemesToIDs.put(rRes.getString(R.string.theme_blue_name), 2);
		}
		
		return mThemesToIDs;
		
	}
	
	public static java.util.Map<CharSequence, Integer> getThemesToResourceIDs(Activity activity) {
		if (mThemesToResourceIDs == null) {
			mThemesToResourceIDs = new java.util.TreeMap<CharSequence, Integer>();
			Resources rRes = NewSumUiActivity.getAppContext(activity).getResources();
			mThemesToResourceIDs.put(rRes.getString(R.string.theme_black_name), R.style.Theme);
			mThemesToResourceIDs.put(rRes.getString(R.string.theme_orange_name), R.style.Theme_Orangesilver);
			mThemesToResourceIDs.put(rRes.getString(R.string.theme_blue_name), R.style.Theme_blue);
		}
		
		return mThemesToResourceIDs;
		
	}
	
//	public final static int THEME_DEFAULT = 0;
//	public final static int THEME_ORANGE_SILVER = 1;
//	public final static int THEME_BLUE = 2;

	
	/**
	 * Set the theme of the Activity, and restart it by creating a new Activity
	 * of the same type.
	 */
	public static void changeToTheme(Activity activity, int theme)
	{
		sTheme = theme;
		activity.finish();

		activity.startActivity(new Intent(activity, activity.getClass()));
	}

	/** Set the theme of the activity, according to the configuration. 
	 * @param sTheme */
	public static void onActivityCreateSetTheme(Activity activity, int iTheme)
	{
		try {
			activity.setTheme(
				getThemesToResourceIDs(activity).get(
						getThemeFromThemeID(iTheme, activity)));
		}
		catch (Exception e) {
			// Could not set theme
			System.err.println("Could not set theme... Continuing normally.");
		}
	}
	/**
	 * 
	 * @param lhs First {@link #TopicInfo} object
	 * @param rhs Second {@link #TopicInfo} object
	 * @return The difference in days between the two {@link #TopicInfo} objects
	 */
	public static int getDiffInDays(TopicInfo early, TopicInfo late) {
		// Compare using formatted date
		return early.getSortableDate().compareTo(late.getSortableDate());
	}

	/**
	 * 
	 * @param lhs First Calendar date
	 * @param rhs Second Calendar date
	 * @return The difference in days between two Calendar dates
	 */
	@SuppressLint("SimpleDateFormat")
	public static int getDiffInDays(Calendar lhs, Calendar rhs) {
		// Compare using formatted date
        SimpleDateFormat df = new SimpleDateFormat();
        df.applyPattern("yyyy-MM-dd");
        String sLhs = df.format(lhs.getTime());

        String sRhs = df.format(rhs.getTime());		
        // debug line //
        // System.out.println(sLhs + " : " + sRhs + " := " + sLhs.compareTo(sRhs));
        // debug line //
		return sLhs.compareTo(sRhs);
	}
	/**
	 * 
	 * @param tiTopics The array of {@link #TopicInfo} objects to Sort
	 * @return The array sorted according to date and Topic sources count
	 */
	public static TopicInfo[] sortTopics(TopicInfo[] tiTopics) {
	  // Sort topics
	  Arrays.sort(tiTopics, new Comparator<TopicInfo>() {
		  @Override
		  public int compare(TopicInfo lhs, TopicInfo rhs) {
			  // Get date difference in DAYS - Nice! :S
			  int iDiff = -getDiffInDays(lhs, rhs);
			  int iSourcesDiff = -(lhs.getSourceCount() - rhs.getSourceCount());

			  if (iDiff != 0)
				  return iDiff;
			  // If they share the exact same date, then compare their source count.
			  if (iSourcesDiff != 0)
				  return iSourcesDiff;
			  // If they share the exact same date and source count,
			  // then sort alphabetically
			  return lhs.getTitle().compareTo(rhs.getTitle());
		  }				  
	  });
		
	  return tiTopics;
	}

	/**
	 * Counts the number of different Sources the Summary refers to
	 * @param Summary The summary of interest
	 * @return The number of different sources that the summary comes from
	 */
	public static int countDiffArticles(String[] Summary) {        
	    // Init string set for sources
		HashSet<String> hsSources = new HashSet<String>();
		// Get first entry, i.e. links and labels
		String sAllLinksAndLabels = Summary[0];
		// if only one link
		if (!sAllLinksAndLabels.contains(NewSumServiceClient.getSecondLevelSeparator())) {
			return 1;
		} else {
			// For every pair
			for (String sTmps : sAllLinksAndLabels.split(NewSumServiceClient.getSecondLevelSeparator())) {
				// Get the link (1st field out of 2)  
				hsSources.add(sTmps.split(NewSumServiceClient.getThirdLevelSeparator())[0]);
			}
		}
	    // Return unique sources
	    return hsSources.size();
	}
	/**
	 * 
	 * @param sCustomUrl the URL to access data
	 * @return the data processed from that URL
	 */
	public static String getFromHttp(String sCustomUrl, boolean getHeader) {
		String sRes = null;
        BufferedReader in = null;
		// if getHead is True, bring url head, else bring url text
	//		return "this is a new String from the custom tab";
	        try {
	            HttpClient client = new DefaultHttpClient();
	            HttpGet request = new HttpGet();
	            // set the url
	            request.setURI(new URI(sCustomUrl));
	            HttpResponse response = client.execute(request);
	            // init the reader
	            in = new BufferedReader
	            (new InputStreamReader(response.getEntity().getContent()));
	            

	            
	            StringBuffer sb = new StringBuffer();
	            String line = "";
	            while ((line = in.readLine()) != null) {
//	                sb.append(Html.fromHtml(line).toString());
	                sb.append(line);
	            }
	            System.out.println(sb.toString());
	            in.close();
	            
	    		if (getHeader) {
	    			// fetch only header
	    			return getTitle(sb.toString(), "<title>(.*|\\\n*)</title>");
	    		}	            
	            
	            sRes = sb.toString();
	//            System.out.println(page);
	        } catch (Exception ex) {
	        	System.err.println("ERROR" + ex.getCause());
	        }
	        return sRes;   
	}
	
	public static String getTitle(String sContent, String sRegex) {
		
        Matcher m = Pattern.compile(sRegex).matcher(sContent);
        if (m.find()) {
            return m.group(1);
        }
        return "not found";
        
	
	}
	
	// Using HTTP_NOT_MODIFIED
	public static boolean urlChanged(String url){
	    try {
	      HttpURLConnection.setFollowRedirects(false);
	      HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
	      con.setRequestMethod("HEAD");
	      return (con.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED);
	    }
	    catch (Exception e) {
	       e.printStackTrace();
	       return false;
	    }
	  }

	/**
	 * Returns the last modified HTTP property of a given URL.
	 * @param url The url to examine
	 * @return A long number indicating the modified header field
	 * of the URL. 
	 * @throws IOException If the connection to the URL fails.
	 */
	public static long lastModified(URL url) throws IOException
	{
	  HttpURLConnection.setFollowRedirects(true);
	  HttpURLConnection con = (HttpURLConnection) url.openConnection();
	  long date = con.getLastModified();

	  return date;
	}
	
	public static String getHTMLfromURL(String uri) throws ClientProtocolException, IOException {
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(uri);
		HttpResponse response = client.execute(request);

		String html = "";
		InputStream in = response.getEntity().getContent();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder str = new StringBuilder();
		String line = null;
		while((line = reader.readLine()) != null)
		{
		    str.append(line);
		}
		in.close();
		html = str.toString();
		
		return html;
	}
    /**
     * returns the substring of the given string from 0 to pattern match start
     * @param sText the given text
     * @param sRegex the matcher
     * @return the substring of the given string from 0 to pattern match start
     */
    public static String removeSourcesParen(String sText) {
    	String sRegex = "\\(\\d+\\)\\s*\\Z";
        Matcher m = Pattern.compile(sRegex).matcher(sText);
        if (m.find()) {
            return sText.substring(0, m.start());
        }
        return sText;
    }
  
    
}
	

	
	
	
	

