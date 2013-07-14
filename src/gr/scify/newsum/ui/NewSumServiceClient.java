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

import gr.scify.newsum.Utils;
import gr.scify.newsum.structs.TopicInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.brickred.socialauth.android.Util;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import android.text.Html;

public class NewSumServiceClient {
		private static final String NAMESPACE = "http://NewSumFreeService.Server.NewSumServer.scify.org/";
//		public static  String URL = "http://143.233.226.97:28080/NewSumFreeService/NewSumFreeService?WSDL";//changed
		public static  String URL = "";
//		public static String URL = "http://192.168.1.200:8080/NewSumFreeService/NewSumFreeService?WSDL";
//		private static final String SOAP_ACTION = "http://192.168.178.27:8080/NewSumFreeService/NewSumFreeService";
		private static final String SOAP_ACTION = "http://143.233.226.97:28080/NewSumFreeService/NewSumFreeService";//changed
//		private static final String SOAP_ACTION = "http://192.168.1.200:8080/NewSumFreeService/NewSumFreeService";
		private static final String READ_TOPICIDS_METHOD = "getTopicIDs";
		private static final String READ_TOPICS_METHOD = "getTopicTitles";
		private static final String READ_CATEGORIES_METHOD = "getCategories";
		private static final String READ_CATEGORY_SOURCES = "getCategorySources";
		private static final String GET_SUMMARY_METHOD = "getSummary";
		private static final String GET_FIRST_LVL_SEPARATOR_METHOD = "getFirstLevelSeparator";
		private static final String GET_SECOND_LVL_SEPARATOR_METHOD = "getSecondLevelSeparator";
		private static final String GET_THIRD_LVL_SEPARATOR_METHOD = "getThirdLevelSeparator";
		private static final String GET_LINK_LABELS = "getLinkLabels";
		private static final String GET_TOPICIDS_BY_KEYWORD = "getTopicIDsByKeyword";
		private static final String GET_TOPIC_TITLES_BY_IDS = "getTopicTitlesByIDs";
		private static final String GET_TOPICS_BY_KEYWORD = "getTopicsByKeyword";
		private static String FirstLevelSeparator = null;
		private static String SecondLevelSeparator = null;
		private static String ThirdLevelSeparator = null;

		/**
		 * Initial program call. Gets all sources from the server for the 
		 * specified category.
		 * @param sCategory The Category of interest
		 * @return The sources for the specified
		 * @deprecated
		 */
		public static String[] readCategorySources(String sCategory) {
			SoapObject request = new SoapObject(NAMESPACE, READ_CATEGORY_SOURCES);

			SoapSerializationEnvelope envelope = 
				new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = false;
			
			request.addProperty("sUserSources", sCategory);
			envelope.setOutputSoapObject(request);
			HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

			try {
				androidHttpTransport.call(SOAP_ACTION, envelope);
				SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
				
				String[] saRes = resultsRequestSOAP.getProperty("return").toString().split(getFirstLevelSeparator());
				return unHTML(saRes);
				
			} catch (Exception e) {
				e.printStackTrace();
				return new String[0];
			}
		}
		
		/**
		 * 
		 * @param sUserSources The Preferable userSources, separated by a specified
		 * delimiter. The Sources are URL Patterns. If "All", the server accepts all
		 * Sources as valid.
		 * @param URL 
		 * @return The categories that contain the specified user Sources
		 */
		public static String[] readCategories(String sUserSources, String URL) {
			SoapObject request = new SoapObject(NAMESPACE, READ_CATEGORIES_METHOD);
			
			SoapSerializationEnvelope envelope = 
				new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = false;
			
			request.addProperty("sUserSources", sUserSources);
			envelope.setOutputSoapObject(request);
			HttpTransportSE androidHttpTransport = new HttpTransportSE(URL); 

			try { 
				androidHttpTransport.call(SOAP_ACTION, envelope);
				SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
				
				String[] saRes = resultsRequestSOAP.getProperty("return").toString().split(getFirstLevelSeparator());
				// Adding Custom Category at position 1
				if (saRes.length != 0) {
					ArrayList<String> lsRes = new ArrayList<String>(Arrays.asList(saRes));
					// 	Check for custom_category
					String sCatToAdd = NewSumUiActivity.getAppContext(null).getResources().getString(
							R.string.custom_category);
					// If it exists
					if ( sCatToAdd.trim().length() > 0) {
						// add as first element
						lsRes.add(0, sCatToAdd);
						// return as array
						return (String[]) lsRes.toArray(new String[lsRes.size()]);
					}
				}
				return saRes;
				
			} catch (Exception e) {
				e.printStackTrace();
				return new String[0];
			}
			
						
		}
		/**
		 * 
		 * @param sUserSources The Separator-delimited String containing the Sources that
		 * the user accepts. If null, All sources are considered valid
		 * @param sCategory The Category of interest
		 * @return An array of Strings containing the Topic IDs for The specified category,
		 * filtered according to User's preference
		 * @deprecated
		 */
		public static String[] readTopicIDs(String sUserSources, String sCategory) {
			SoapObject request = new SoapObject(NAMESPACE, READ_TOPICIDS_METHOD);

			SoapSerializationEnvelope envelope = 
				new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = false;
			

			request.addProperty("sUserSources", sUserSources);
			request.addProperty("sCategory", sCategory);
			envelope.setOutputSoapObject(request);
			HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

			try {
				androidHttpTransport.call(SOAP_ACTION, envelope);
				SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
				String[] saRes = resultsRequestSOAP.getProperty("return").toString().split(getFirstLevelSeparator());
				return saRes;
//				return unHTML(saRes);
			} catch (Exception e) {
				e.printStackTrace();
				return new String[0];
			}
					
		}
		/**
		 * Searches for the specified keyword in the User Sources links, and returns Topic IDs
		 * that are mostly relevant to the search query
		 * @param sKeyword The Search term	
		 * @param sUserSources sUserSources The Separator-delimited String containing the Sources that
		 * the user accepts. If "All", All sources are considered valid
		 * @return Topic IDs in relevance to the search term. First topic is mostly relevant, etc.
		 * @deprecated
		 */
		public static String readTopicIDsByKeyword(String sKeyword, String sUserSources) {
			SoapObject request = new SoapObject(NAMESPACE, GET_TOPICIDS_BY_KEYWORD);

			SoapSerializationEnvelope envelope = 
				new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = false;
			
			request.addProperty("sKeyword", sKeyword);
			request.addProperty("sUserSources", sUserSources);
			envelope.setOutputSoapObject(request);
			HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

			try {
				androidHttpTransport.call(SOAP_ACTION, envelope);
				SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
				String saRes = resultsRequestSOAP.getProperty("return").toString();

				return saRes;
			} catch (Exception e) {
				e.printStackTrace();
				return "";
			}
					
		}
		/**
		 * Searches for the specified keyword in the User Sources links, and returns the Topics
		 * that are mostly relevant to the search query
		 * @param sKeyword The Search term	
		 * @param sUserSources sUserSources The Separator-delimited String containing the Sources that
		 * the user accepts. If "All", All sources are considered valid
		 * @return Topic IDs in relevance to the search term. First topic is mostly relevant, etc.
		 * @since 1.0
		 */
		public static TopicInfo[] readTopicsByKeyword(String sKeyword, String sUserSources) {
			SoapObject request = new SoapObject(NAMESPACE, GET_TOPICS_BY_KEYWORD);

			SoapSerializationEnvelope envelope = 
				new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = false;
			
			request.addProperty("sKeyword", sKeyword);
			request.addProperty("sUserSources", sUserSources);
			envelope.setOutputSoapObject(request);
			HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

			try {
				androidHttpTransport.call(SOAP_ACTION, envelope);
				SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
				String sRes = resultsRequestSOAP.getProperty("return").toString();
				// if more than one search results 
				if (sRes.contains(getFirstLevelSeparator())) {
					String[] saRes = sRes.split(getFirstLevelSeparator());
					return parseTopicInfo(saRes);
				// if only one search result
				} else if (sRes.contains(getSecondLevelSeparator())) { 
					String[] saRes = new String[] {sRes};
					return parseTopicInfo(saRes);
				// no search results
				} else {
					return new TopicInfo[0];
				}
			} catch (Exception e) {
				e.printStackTrace();
				return new TopicInfo[0];
			}
		}			
		/**
		 * Fetches all topics that correspond to the supplied Topic IDs.
		 * @param sTopicIDs A Separator Delimited String containing the IDs
		 * @return The topic Titles that correspond to the specified IDs
		 * @deprecated
		 */
		public static String[] readTopicTitlesByIDs(String sTopicIDs) {
			
			SoapObject request = new SoapObject(NAMESPACE, GET_TOPIC_TITLES_BY_IDS);

			SoapSerializationEnvelope envelope = 
				new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = false;
			
			request.addProperty("sTopicIDs", sTopicIDs);
			envelope.setOutputSoapObject(request);
			HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

			try {
				androidHttpTransport.call(SOAP_ACTION, envelope);
				SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
				String sRes = resultsRequestSOAP.getProperty("return").toString();
				// If nothing found
				if ("anyType{}".equals(sRes)) {
					// Return one empty topic
					return new String[] { "" };
				} else {
					// else return all topics
					String[] saRes = sRes.split(getFirstLevelSeparator());
					return saRes;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return new String[0];
			}
		}		
		/**
		 * 
		 * @param sUserSources The Separator-delimited String containing the Sources that
		 * the user accepts. If null, All sources are considered valid
		 * @param sCategory The Category of interest
		 * @return An array of Strings that represents the Topics for the specified 
		 * category according to User's preference.
		 * @since v1.0
		 */
		public static TopicInfo[] readTopics(String sUserSources, String sCategory) {
			// 	Check for custom_category
			String sCatToAdd = NewSumUiActivity.getAppContext(null).getResources().getString(
				  R.string.custom_category);
			// If it exists
			if ( sCatToAdd.trim().length() > 0) {
				// Get URL and mime from resources
				String sCatToAddURL = NewSumUiActivity.getAppContext(null).getResources().getString(
						  R.string.custom_category_url); 
//				String sCatToAddMime = NewSumUiActivity.getAppContext().getResources().getString(
//						  R.string.custom_category_mime);
				if (sCategory.equals(sCatToAdd)) {
					// get the Topic from the URL
					String sTitle = Utils.getFromHttp(sCatToAddURL , true);
					// Get a webview to parse html
//					WebView wv = new WebView(NewSumUiActivity.getAppContext());
//					wv.loadData(sRes, sCatToAddMime, null);
					
					TopicInfo aTi = new TopicInfo("1", Calendar.getInstance(), sTitle, 1);
					// return the Topic
					return new TopicInfo[] {aTi};
				}
			}			
			SoapObject request = new SoapObject(NAMESPACE, READ_TOPICS_METHOD);//getTopicTitles

			SoapSerializationEnvelope envelope = 
				new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = false;
			
			request.addProperty("sUserSources", sUserSources);
			request.addProperty("sCategory", sCategory);
			envelope.setOutputSoapObject(request);
			HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

			try {
				androidHttpTransport.call(SOAP_ACTION, envelope);
				SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
				
				String[] saRes = resultsRequestSOAP.getProperty("return").toString().split(getFirstLevelSeparator());

				TopicInfo[] tiRes = parseTopicInfo(saRes);
				// sort topics again, in order to avoid Calendar normalization issues
				return Utils.sortTopics(tiRes); 
				
			} catch (Exception e) {
				e.printStackTrace();
				return new TopicInfo[0];
			}
		}
		/**
		 * Helper Function, to acquire several data about a specific Topic
		 * from the String that represents a Topic
		 * @param saRes The String Array Containing the Topics
		 * @return A {@link #TopicInfo} Array
		 */
		protected static TopicInfo[] parseTopicInfo(String[] saRes) {
			// Init return variable
			ArrayList<TopicInfo> altiInfo = new ArrayList<TopicInfo>();
			// For every string
			for (String sCur: saRes) {
				// Decode
				// Get topic ID
				String sID = sCur.split(getSecondLevelSeparator())[0];
				// Get title
				String sTitle = sCur.split(getSecondLevelSeparator())[1];
				// Get number of sources
				Matcher m = Pattern.compile("[(](\\d+)[)]\\s*$").matcher(sTitle);
				String sSourcesNum;
				if (!m.find()) {
					sSourcesNum = "0";
				} else {
					sSourcesNum = m.group(1);
				}
				int iSourcesNum = Integer.valueOf(sSourcesNum);
				
				// Get date
				String sMillis = sCur.split(getSecondLevelSeparator())[2];
				Calendar cDate = Calendar.getInstance();
//				TimeZone tz = TimeZone.getTimeZone("GMT");
//				cDate.setTimeZone(tz);
				cDate.setTimeInMillis(Long.valueOf(sMillis)); 
				// Create Topic Info Object
				TopicInfo ti = new TopicInfo(sID, cDate, Utils.removeSourcesParen(sTitle), iSourcesNum);
				// Add to return list
				altiInfo.add(ti);
			}
//			return unHTML(saRes);
			TopicInfo[] tiRes = new TopicInfo[altiInfo.size()];
			altiInfo.toArray(tiRes);
			return tiRes;
		}
		
		public static String getFirstLevelSeparatorFromWS() {
			SoapObject request = new SoapObject(NAMESPACE, GET_FIRST_LVL_SEPARATOR_METHOD);

			SoapSerializationEnvelope envelope = 
				new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = false;

			envelope.setOutputSoapObject(request);
			HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

			String sRes = null;
			try {
				androidHttpTransport.call(SOAP_ACTION, envelope);
				SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
				sRes = resultsRequestSOAP.getProperty("return").toString();
				return unHTML(sRes);
				
			} catch (Exception e) {
				sRes = null;
				e.printStackTrace();
			}
			
			return sRes;
			
		}
		public static String getSecondLevelSeparatorFromWS() {
			SoapObject request = new SoapObject(NAMESPACE, GET_SECOND_LVL_SEPARATOR_METHOD);

			SoapSerializationEnvelope envelope = 
				new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = false;
			

			envelope.setOutputSoapObject(request);
			HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

			String sRes = null;
			try {
				androidHttpTransport.call(SOAP_ACTION, envelope);
				SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
				sRes = resultsRequestSOAP.getProperty("return").toString();
				return sRes;
			} catch (Exception e) {
				sRes = null;
				e.printStackTrace();
			}
			return "===";//hmm
		}
		public static String getThirdLevelSeparatorFromWS() {
			SoapObject request = new SoapObject(NAMESPACE, GET_THIRD_LVL_SEPARATOR_METHOD);

			SoapSerializationEnvelope envelope = 
				new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = false;
			

			envelope.setOutputSoapObject(request);
			HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

			String sRes = null;
			try {
				androidHttpTransport.call(SOAP_ACTION, envelope);
				SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
				sRes = resultsRequestSOAP.getProperty("return").toString();
				return unHTML(sRes);
				
			} catch (Exception e) {
				sRes = null;
				e.printStackTrace();
			}
			
			return sRes;
			
		}
		/**
		 * Each String coming from the Server is split using this global separator
		 * @return The global Separator. 
		 */
		protected static String getFirstLevelSeparator() {
			// Initialize the separator, if needed
			if (FirstLevelSeparator == null)
				FirstLevelSeparator = getFirstLevelSeparatorFromWS();
			return FirstLevelSeparator;
		}

		/**
		 * The Separator returned is used in getSummary and getLinkLabels Methods.
		 * @return The Second Level Separator
		 */
		public static String getSecondLevelSeparator() {
			// Initialize the separator, if needed
			if (SecondLevelSeparator == null)
				SecondLevelSeparator = getSecondLevelSeparatorFromWS();
			return SecondLevelSeparator;
		}
		/**
		 * Most Inner Separator
		 * @return The third Level Separator
		 */
		public static String getThirdLevelSeparator() {
			// Initialize the separator, if needed
			if (ThirdLevelSeparator == null)
				ThirdLevelSeparator = getThirdLevelSeparatorFromWS();
			return ThirdLevelSeparator;
		}		

		/**
		 * Sends a specified Topic ID to the server and gets the Summary for it. 
		 * @param sTopicID The Topic ID of interest
		 * @param sUserSources The Sources preferences of the user
		 * @return The summary for the specified Topic ID
		 */
		public static String[] getSummary(String sTopicID, String sUserSources) {
			String[] saRes = null;
			SoapObject request = new SoapObject(NAMESPACE, GET_SUMMARY_METHOD);

			SoapSerializationEnvelope envelope = 
				new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = false;
			
			request.addProperty("sTopicID", sTopicID);
			request.addProperty("sUserSources", sUserSources);
			envelope.setOutputSoapObject(request);
			HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

			try {
				androidHttpTransport.call(SOAP_ACTION, envelope);
				SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
				String sRes = resultsRequestSOAP.getProperty("return").toString();
				if ("anyType{}".equals(sRes)) {
					return new String[0];
				} else {
					saRes = sRes.split(getFirstLevelSeparator());
					return saRes;
				}
			} catch (Exception e) {
				e.printStackTrace();
				saRes = null;
				return new String[0];
			}
		}
		/**
		 * 
		 * @return A String describing each URL source with a Label. The data is 
		 * separated by {@link #SecondLevelSeparator} inside.
		 */
		public static String[] getLinkLabels() {
			SoapObject request = new SoapObject(NAMESPACE, GET_LINK_LABELS);

			SoapSerializationEnvelope envelope = 
				new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = false;
			
			envelope.setOutputSoapObject(request);
			HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

			try {
				androidHttpTransport.call(SOAP_ACTION, envelope);
				SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
				
				String[] saRes = resultsRequestSOAP.getProperty("return").toString().split(getFirstLevelSeparator());
				return saRes;	
			} catch (Exception e) {
				e.printStackTrace();
				return new String[]{"Sport.com","World.com","Civilization.com","England.com","Science.com"}; //TODO Error msg?
			}
		}		
		
		public static String unHTML(String sHTMLString) {
			return Html.fromHtml(sHTMLString).toString();
		}
		
		public static String[] unHTML(String[] sHTMLStrings) {
			String[] sRes = new String[sHTMLStrings.length];
			int iCnt=0;
			for (String sCur : sHTMLStrings) {
				sRes[iCnt++] = Html.fromHtml(sCur).toString();
			}
			return sRes;
		}
		/**
		 * Useful for Method getLinkLabels. After the method's return, if the 
		 * array is passed into this method, a map containing
		 * (key, value)=(URL, Label) is returned.
		 * An example entry is (http://www.tovima.gr/feed/culture/, "Ξ¤Ξ� Ξ’Ξ—Ξ�Ξ‘, Ξ ΞΏΞ»ΞΉΟ„ΞΉΟƒΞΌΟ�Ο‚")
		 * Use it with Delimiter getSentenceSeparator()
		 * @param sStr The Array of Strings that contains the data
		 * @param sDel The delimiter used in the string
		 * @return The map describing the string
		 */
		public static LinkedHashMap<String, String> unpackArray(String[] aStr, String sDel) {
	        LinkedHashMap<String, String> m = new LinkedHashMap<String, String>();
	        for (String each : aStr) {
	            String key      = each.split(sDel)[0];
	            String value    = each.split(sDel)[1];
	            m.put(key, value);
	        }
	        return m;
	    }
}
		
		

