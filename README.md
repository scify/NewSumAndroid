NewSumAndroid
=
[NewSum](http://www.scify.gr/site/en/our-projects/completed-projects/newsum-menu-en) is a cutting edge summarization application developed for getting summaries from various news sources.
-

NewSumAndroid is the interface for devices running android os, currently supporting versions 2.1 - 4.2. 

The application depends on the following libraries:

- [ksoap2-android-assembly-2.6.3-jar-with-dependencies.jar](http://code.google.com/p/ksoap2-android/)
- [jsoup-1.7.2.jar](http://jsoup.org/)
- [android-support-v4.jar](http://developer.android.com/tools/extras/support-library.html)
- [libGoogleAnalyticsV2.jar](https://developers.google.com/analytics/devguides/collection/android/v2/)

In order to setup the project in eclipse, do:

clone it :-)

Download facebook sdk (from their site) & google play services (from the sdk manager)
Import them as projects in Eclipse (in case you have not already).

Select *"Import existing Android code into workspace"*

In NewSumAndroid, go to project --> properties --> android, select android 2.2
Below, in the same tab, at *libraries*, please do *add* facebook / google play services as **libraries**.

Then, in the *order and export* tab, keep checked (or check, if not checked) the *android 2.2*, *android private libraries*, & *android dependencies* boxes.

Last, right Click on NewSumAndroid --> Android Tools --> Add support library, add the library you are prompted to.

If you want analytics, you should add your own trackingId in the appropriate analytics.xml files.

You should be ok.

[SciFY NPC](http://www.scify.org), 2013
-

Contributors
- *George G. <ggianna@scify.org>*
- *Panagiotis T. <ptse@scify.org>*
- *George K. <gkiom@scify.org>*
