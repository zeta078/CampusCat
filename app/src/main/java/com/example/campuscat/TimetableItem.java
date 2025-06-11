package com.example.campuscat;

public class TimetableItem {
    private String subject;
    private String place;
    private String day;
    private String startTime;
    private String endTime;

    public TimetableItem(String subject, String place, String day,
                         String startTime, String endTime) {
        this.subject   = subject;
        this.place     = place;
        this.day       = day;
        this.startTime = startTime;
        this.endTime   = endTime;
    }

    // === 기존 getter ===
    public String getSubject()   { return subject; }
    public String getPlace()     { return place;   }
    public String getDay()       { return day;     }
    public String getStartTime() { return startTime; }
    public String getEndTime()   { return endTime;   }

    // === 여기에 setter 추가 ===
    public void setSubject(String subject)     { this.subject   = subject;   }
    public void setPlace(String place)         { this.place     = place;     }
    public void setDay(String day)             { this.day       = day;       }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime)     { this.endTime   = endTime;   }
}
