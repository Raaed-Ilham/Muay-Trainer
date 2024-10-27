package com.example.visiontest;

import static java.lang.Math.atan2;

import com.google.mlkit.vision.pose.PoseLandmark;

public class Calculations {
    public float xDiff;
    public float yDiff;
    public float zDiff;

    public void difference (float X,float Y, float Z, float X2, float Y2, float Z2){
     xDiff = Math.abs(X2 - X);
     yDiff = Math.abs(Y2-Y);
     zDiff= Math.abs(Z2-Z);
}

   public double getAngle(PoseLandmark firstPoint, PoseLandmark midPoint, PoseLandmark lastPoint) {
        double result =
                Math.toDegrees(
                        atan2(lastPoint.getPosition().y - midPoint.getPosition().y,
                                lastPoint.getPosition().x - midPoint.getPosition().x)
                                - atan2(firstPoint.getPosition().y - midPoint.getPosition().y,
                                firstPoint.getPosition().x - midPoint.getPosition().x));
        result = Math.abs(result); // Angle should never be negative
        if (result > 180) {
            result = (360.0 - result); // Always get the acute representation of the angle
        }
        return result;
    }


}

