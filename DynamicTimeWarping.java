package com.example.androidaudiorecorder;

/**
 * SuperClass For DynamicTimeWarping2D
 *
 *@author liwenzhe (Wenzhe Li)
 */
public abstract class DynamicTimeWarping {

    protected static int globalPathConstraint = 20;

    /**
     * Calculate the distance between two feature vectors.
     * @return
     */

    public static double getMin(double ... num){
        double min = Double.MAX_VALUE;
        for (int i=0; i<num.length; i++){
            if (num[i] < min)
                min = num[i];
        }

        return min;
    }

    /**
     * Setter for globalPatConstraint
     * @param globalPathConstraint
     */
    public void setGlobalPathConstraint(int globalPathConstraint){
        this.globalPathConstraint = globalPathConstraint;
    }

}