/*******************************************************************************
 *
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
 * 
 * @contributor NaSOS (nasos.loukas@gmail.com)
 *******************************************************************************/
package gr.scify.newsum.controllers;

import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class CacheController {
		
	private static Timer cacheTimer = new Timer();
	public static final long EXPIRATION = 15*60*1000L; // 15 minutes

	private static LruCache<String, Object> cache = createCache();
	
	private static LruCache<String, Object> createCache() {
		// set cache clearance
		cacheTimer.purge();
		cacheTimer.scheduleAtFixedRate(
				new TimerTask() {	
					@Override
					public void run() {
						clearCache();
					}
				}, EXPIRATION, EXPIRATION);
		// create cache
		return new LruCache<String, Object>( (int) (Runtime.getRuntime().maxMemory()/3) );
	}
	
	/*
	 * Add an object to the cache
	 */
	public static <V> void putToCache(String key, V v) {
		if(key==null || v==null) return;
		synchronized (cache) {
			if(cache==null) cache = createCache();
			// if bitmap mark
			cache.put(key, v);
		}
	}
	
	/*
	 * retrieve object from cache
	 */
	@SuppressWarnings("unchecked")
	public static <V> V getFromCache(String key){
		synchronized (cache) {
			if(cache==null) cache = createCache();
			Object data = cache.get(key);
			return (V) data;
		}
	}
	
	/*
	 * remove object from cache
	 */
	public static <V> void removeFromCache(String key){
		synchronized (cache) {
			if(cache==null) cache = createCache();
			cache.remove(key);
		}
	}
	
	/*
	 * clear cache
	 */
	public static void clearCache(){
		synchronized (cache) {
			if(cache==null) return;
			for(String key : cache.snapshot().keySet())
				cache.remove(key);
		}
	}
	
	/*
	 * clear images
	 */
	protected static void clearImageCache(){
		synchronized (cache) {
			if(cache==null) return;
			for(String key : cache.snapshot().keySet())
				if( cache.get(key) instanceof Bitmap )
					cache.remove(key);
		}
	}
		
}
