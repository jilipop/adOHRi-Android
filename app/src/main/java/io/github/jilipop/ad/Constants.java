package io.github.jilipop.ad;

public class Constants {
    public interface ACTION {
        public static String MAIN_ACTION = "io.github.jilipop.ad.action.main";
        public static String STOPRECEIVER_ACTION = "io.github.jilipop.ad.action.startreceiver";
        public static String STARTRECEIVER_ACTION = "io.github.jilipop.ad.action.startreceiver";
    }
    public interface NOTIFICATION {
        public static int NOTIFICATION_ID = 101;
        public static String NOTIFICATION_ID_STRING = "Receiver Notification ID";
        public static String CHANNEL_ID = "Receiver Notification Channel";
    }
}
