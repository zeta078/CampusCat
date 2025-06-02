package com.example.campuscat;

public class TimetableItem {
    private String subject;
    private String place;
    private String day;
    private String startTime;
    private String endTime;

    public TimetableItem(String subject, String place, String day, String startTime, String endTime) {
        this.subject = subject;
        this.place = place;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getSubject() { return subject; }
    public String getPlace() { return place; }
    public String getDay() { return day; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
}
