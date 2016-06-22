package de.haw.yumiii.supercalendar;

import android.app.Activity;
import android.app.Application;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.parse.Parse;

import net.danlew.android.joda.JodaTimeAndroid;

/**
 * Created by Yumiii on 19.06.16.
 */
public class SuperCalendarApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity a, Bundle savedInstanceState) {
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("p6hd1KznrdoeApttOKGaPmYKn4GIB4KiQWyivdUl")
                .clientKey("LrmimA0sWej7o7mLBWueqQKvICefQnWHA83REYEE")
                .server("https://parseapi.back4app.com/")
                .build());

        JodaTimeAndroid.init(this);
    }
}
