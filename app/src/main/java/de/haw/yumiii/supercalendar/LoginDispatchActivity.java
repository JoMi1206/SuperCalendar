package de.haw.yumiii.supercalendar;

/**
 * Created by Yumiii on 19.06.16.
 */

import com.parse.ui.ParseLoginDispatchActivity;

import de.haw.yumiii.supercalendar.overview.DayActivity;

public class LoginDispatchActivity extends ParseLoginDispatchActivity {

    @Override
    protected Class<?> getTargetClass() {
        return DayActivity.class;
    }
}