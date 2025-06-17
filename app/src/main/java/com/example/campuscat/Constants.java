package com.example.campuscat;

public class Constants {
    // 전주대학교 캠퍼스 지오펜스 정보 (대략적인 중심 좌표 및 반경)
    public static final double JEONJU_UNIV_LATITUDE = 35.8279;
    public static final double JEONJU_UNIV_LONGITUDE = 127.0984;
    public static final float JEONJU_UNIV_RADIUS_METERS = 800; // 800미터 반경 (테스트 후 조정 권장)
    public static final String GEOFENCE_REQUEST_ID = "JEONJU_UNIV_CAMPUS";

    // 알림 채널 ID
    public static final String ATTENDANCE_NOTIFICATION_CHANNEL_ID = "attendance_channel";
    public static final CharSequence ATTENDANCE_NOTIFICATION_CHANNEL_NAME = "출석 알림";
    public static final int ATTENDANCE_NOTIFICATION_ID = 1002; // 알림 고유 ID

    // SharedPreferences 파일 이름 및 키
    public static final String TIMETABLE_PREFS_NAME = "timetable_prefs";
    public static final String TIMETABLE_ITEMS_KEY = "timetable_items";
    public static final String CAT_PREFS_NAME = "CatPrefs"; // CatPrefs.xml 파일 이름
    public static final String CAT_XP_KEY = "catxp"; // catxp 키 이름

    // 출석 관련 상수
    public static final int XP_PER_ATTENDANCE = 50; // 자동 출석 시 지급할 경험치
    public static final int ATTENDANCE_CHECK_BEFORE_MINUTES = 10; // 수업 시작 N분 전부터 체크
    public static final int ATTENDANCE_CHECK_AFTER_MINUTES = 5; // 수업 시작 N분 후까지 체크
    public static final String ATTENDANCE_LOG_PREFS = "attendance_log_prefs";
}