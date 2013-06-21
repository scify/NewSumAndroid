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

import java.util.Locale;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class Setlanguage extends Application
{
    public static final String FORCE_LOCAL = "force_local";
    public static final String FORCE_CURRENCY = "force_currency";
   
  @Override
  public void onCreate()
  {
    updateLanguage(this,null);
    super.onCreate();
  }

  public static void updateLanguage(Context ctx, String lang)
  {
    Configuration cfg = new Configuration();
    SharedPreferences force_pref = PreferenceManager.getDefaultSharedPreferences(ctx);
    String language = force_pref.getString(FORCE_LOCAL, "");
   
    if(TextUtils.isEmpty(language)&&lang==null){
        cfg.locale = Locale.getDefault();

        SharedPreferences.Editor edit = force_pref.edit();
        String tmp="";
        tmp=Locale.getDefault().toString().substring(0, 2);
       
        edit.putString(FORCE_LOCAL, tmp);
        edit.commit();
       
    }else if(lang!=null){
        cfg.locale = new Locale(lang);
        SharedPreferences.Editor edit = force_pref.edit();
        edit.putString(FORCE_LOCAL, lang);
        edit.commit();
       
    }else if(!TextUtils.isEmpty(language)){
      cfg.locale = new Locale(language);
    }
   
    ctx.getResources().updateConfiguration(cfg, null);
    
    Utils.resetThemeMaps();
  }
 
 
  @Override
  public void onConfigurationChanged(Configuration newConfig)
  {
        SharedPreferences force_pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext().getApplicationContext());

      String language = force_pref.getString(FORCE_LOCAL, "");    
       
      super.onConfigurationChanged(newConfig);
      updateLanguage(this,language);
  }
 
}


