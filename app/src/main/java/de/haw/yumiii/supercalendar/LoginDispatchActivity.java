package de.haw.yumiii.supercalendar;

/**
 * Created by Yumiii on 19.06.16.
 */

import com.parse.ui.ParseLoginDispatchActivity;

public class LoginDispatchActivity extends ParseLoginDispatchActivity {

    @Override
    protected Class<?> getTargetClass() {
        return DayActivity.class;
    }
}