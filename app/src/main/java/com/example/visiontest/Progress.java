package com.example.visiontest;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Progress {

    private int jab;

    private int cross;

    private int DutchDrills;

    SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd.yyyy");

    String Date= dateFormat.format(new Date());;


    public Progress() {
    }

    public Progress(int jabReps, int crossReps, int dutchDrills) {
        jab = 0;
        cross = 0;
        DutchDrills = 0;

    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public int getJab() {
        return jab;
    }

    public void setJab(int jab) {
        this.jab = jab;
    }

    public int getCross() {
        return cross;
    }

    public void setCross(int cross) {
        this.cross = cross;
    }

    public int getDutchDrills() {
        return DutchDrills;
    }

    public void setDutchDrills(int dutchDrills) {
        DutchDrills = dutchDrills;
    }
}
