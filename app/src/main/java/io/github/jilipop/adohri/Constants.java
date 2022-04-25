package io.github.jilipop.adohri;

public class Constants {
    public interface ACTION {
        String MAIN_ACTION = "io.github.jilipop.adohri.action.main";
        String STOPRECEIVER_ACTION = "io.github.jilipop.adohri.action.startreceiver";
        String STARTRECEIVER_ACTION = "io.github.jilipop.adohri.action.startreceiver";
    }
    public interface NOTIFICATION {
        int NOTIFICATION_ID = 101;
        String CHANNEL_ID = "Receiver Notification Channel";
    }
    public static String sourceCodeURL = "https://github.com/jilipop/adOHRi-Android";
}
