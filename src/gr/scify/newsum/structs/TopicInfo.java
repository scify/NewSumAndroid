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

package gr.scify.newsum.structs;

import gr.scify.newsum.ui.NewSumUiActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * The Class that describes a Topic. 
 * It contains information about it's specific ID number, 
 * it's last date, and it's title.
 *  
 * @author ggianna, gkiom
 *
 */
public class TopicInfo implements Parcelable {
	
	private String sID;
	private Calendar dLastDate;
	private String sTitle;
	private int iSourceCount;	
	
	public TopicInfo(String sIDArg, Calendar dLastDateArg, String sTitleArg, int iSourceCountArg) {
		this.sID = sIDArg;
		this.dLastDate = dLastDateArg;
		this.sTitle = sTitleArg;
		this.iSourceCount = iSourceCountArg;
	}
	
	public TopicInfo(Parcel in) {
        sID = in.readString();
        dLastDate = (Calendar) in.readSerializable();
        iSourceCount = in.readInt();
        sTitle = in.readString();
	}

	public String getID() {
		return sID;
	}
	
	public Calendar getLastDate() {
		return dLastDate;
	}
	
	public String getTitle() {
		return sTitle;
	}
	
	public int getSourceCount() {
		return iSourceCount;
	}
	/**
	 * 
	 * @return A printable representation of the Topic's Date, 
	 * according to device's default Locale.
	 */
	public String getPrintableDate() {
        // get a short date representation of the device's default locale
        DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT, NewSumUiActivity.lLocale);
    
        return dateFormatter.format(this.dLastDate.getTime());
	}

	public String getSortableDate() {
        SimpleDateFormat df = new SimpleDateFormat();
//        df.applyPattern("dd.MM.yyyy - HH:mm:ss z");
        df.applyPattern("yyyy-MM-dd");
        return df.format(this.dLastDate.getTime());		
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return this.hashCode();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.sID);
		dest.writeSerializable(this.dLastDate);
		dest.writeInt(this.iSourceCount);
		dest.writeString(this.sTitle);
		
	}
    public static final Parcelable.Creator<TopicInfo> CREATOR = new Parcelable.Creator<TopicInfo>() {
        public TopicInfo createFromParcel(Parcel in) {
            return new TopicInfo(in); 
        }

        public TopicInfo[] newArray(int size) {
            return new TopicInfo[size];
        }
    };	
}
