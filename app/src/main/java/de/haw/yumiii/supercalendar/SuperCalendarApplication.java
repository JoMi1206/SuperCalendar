package de.haw.yumiii.supercalendar;

import android.app.Application;

import com.parse.Parse;

import net.danlew.android.joda.JodaTimeAndroid;

/**
 * Created by Yumiii on 19.06.16.
 */
public class SuperCalendarApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("p6hd1KznrdoeApttOKGaPmYKn4GIB4KiQWyivdUl")
                .clientKey("LrmimA0sWej7o7mLBWueqQKvICefQnWHA83REYEE")
                .server("https://parseapi.back4app.com/")
                .build());

        JodaTimeAndroid.init(this);
    }
}
