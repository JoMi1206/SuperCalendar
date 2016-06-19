package de.haw.yumiii.supercalendar;

/**
 * Created by Yumiii on 22.05.16.
 */
public class Settings {

//    public static final String REST_API_BASEURL_EMULATOR = "http://10.0.2.2:3000/";
//    public static final String REST_API_BASEURL_EMULATOR = "http://192.168.1.4:3000/";
    public static final String REST_API_BASEURL_EMULATOR = "http://131.159.220.32:3000/";

    public static final String REST_API_BASEURL_LOCAL = "http://192.168.1.4:3000/";

    public static final String DATE_FORMAT = "dd.MM.yyyy";

    public static String getRestBaseUrl() {
        return REST_API_BASEURL_LOCAL;
    }
}
