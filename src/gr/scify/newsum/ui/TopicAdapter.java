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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import gr.scify.newsum.Utils;
import gr.scify.newsum.structs.TopicInfo;
import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TopicAdapter extends BaseAdapter {
    public static final String INVALID_ID = "-1";
    
    private Activity activity;
    private String[] data;
    private String[] saIds;
    private static LayoutInflater inflater=null;
    private IVisitedChecker visitedChecker=null;

	public void setVisitedChecker(IVisitedChecker visitedChecker) {
		this.visitedChecker = visitedChecker;
	}
	
    public TopicAdapter(Activity a, String[] saTopics) {
        activity = a;
    	data = saTopics;
    	// Ignoring saIds: Assigning to null to detect wrong use
    	saIds = null;
    	
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    public TopicAdapter(Activity a, TopicInfo[] tiaTopics) {
        activity = a;
        // Init topic titles
        ArrayList<String> lsData = new ArrayList<String>();
        // Init Topic IDs
        ArrayList<String> lsIDs = new ArrayList<String>();
        // Check empty topic set
        if (tiaTopics.length == 0) {
        	data = new String[0];
        	return;
        }
        // Run through topics
        // Init last date encountered to... 2 days later
        Calendar lastDate = Calendar.getInstance();
        lastDate.setTimeInMillis(new Date().getTime() +
        		2 * (1000 * 60 * 60 * 24));
        // get a short date representation of the device's default locale
        DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT, NewSumUiActivity.getDefaultLocale());
        
        // For every topic
        for (TopicInfo t : tiaTopics) {
        	// If in different date than last news item
        	if (Utils.getDiffInDays(lastDate, t.getLastDate()) != 0) {
        		// Update last date encountered
                lastDate = t.getLastDate();
        		// Add date entry
//                lsData.add("<i>" + df.format(lastDate.getTime()) + "</i>: " + t.getTitle());
                lsData.add("<i>" + dateFormatter.format(lastDate.getTime()) + "</i>: " + t.getTitle());
        	} else {
        		lsData.add(t.getTitle());
        	}
        	// Also keep IDs
            lsIDs.add(t.getID());
        }
        data=new String[lsData.size()];
        saIds=new String[lsIDs.size()];
        // Update data and id arrays
        lsData.toArray(data);
        lsIDs.toArray(saIds);
        
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return data.length;
    }

    public Object getItem(int position) {
        return saIds[position];
    }

    public long getItemId(int position) {
        return position;
    }
    
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		// Init list view
        View vi=convertView;
        // Create item if needed and init textview data
        TextView text = null;
        if(convertView==null) {
            vi = inflater.inflate(R.layout.image_topic_item, null);
        }
        // Get list item
        text = (TextView)vi.findViewById(R.id.text);
        
        // De-HTML title
        if (text != null)
        	text.setText(Html.fromHtml(data[position]).toString());

        // Get image ref
        ImageView image =(ImageView)vi.findViewById(R.id.image);
        
        // If a checker has been assigned 
        if (visitedChecker != null) {
        	// If IDs have been retrieved
        	if (saIds != null) {
        		if (text != null)
		        	// Then if it has been visited
		        	if (visitedChecker.hasBeenVisited((String)getItem(position))) {
		        		//text.setTextColor(Color.DKGRAY);
		        		image.setVisibility(View.VISIBLE);
		        	}
		        	else
		        	{
		        		image.setVisibility(View.GONE);
		        	}
        	}
        }
        
        
        return vi;
    }

	protected int getUniqueID(View v) {
		// Randomize
		Random r = new Random();
		// Get next possible ID
		int iRes = r.nextInt();
		// Check if it is already used
		while (v.findViewById(iRes) != null)
			iRes = r.nextInt();
		
		return iRes;
	}
}