package com.learningassistant.app.models;

public class StudyDay {
    private String dayLabel;
    private String taskDescription;
    private boolean isToday;

    public StudyDay(String dayLabel, String taskDescription, boolean isToday) {
        this.dayLabel = dayLabel;
        this.taskDescription = taskDescription;
        this.isToday = isToday;
    }

    public String getDayLabel() { return dayLabel; }
    public void setDayLabel(String dayLabel) { this.dayLabel = dayLabel; }

    public String getTaskDescription() { return taskDescription; }
    public void setTaskDescription(String taskDescription) { this.taskDescription = taskDescription; }

    public boolean isToday() { return isToday; }
    public void setToday(boolean today) { isToday = today; }
}
