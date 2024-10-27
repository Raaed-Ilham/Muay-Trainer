package com.example.visiontest;
import java.util.ArrayList;

public class positionStorage {
    private static positionStorage instance;
    private ArrayList<Float>RightWristPositionX = new ArrayList<>();
    private ArrayList<Float>RightWristPositionY = new ArrayList<>();

    private ArrayList<Float>LeftWristPositionX = new ArrayList<>();

    private ArrayList<Float>LeftWristPositionY = new ArrayList<>();


    private ArrayList<Float>AllLandmarks = new ArrayList<>();

    public positionStorage() {
    }

    public static positionStorage getInstance(){
        if (instance == null) {
            instance = new positionStorage();
        }
        return instance;
    }
    public ArrayList<Float> getRightWristPositionX() {
        return RightWristPositionX;
    }

    public ArrayList<Float> getRightWristPositionY() {
        return RightWristPositionY;
    }

    public ArrayList<Float> getLeftWristPositionX() {
        return LeftWristPositionX;
    }

    public ArrayList<Float> getLeftWristPositionY() {
        return LeftWristPositionY;
    }

    public ArrayList<Float> getAllLandmarks() {return AllLandmarks;}


}
