package com.example.androidaudiorecorder;

import android.content.Context;
import android.widget.Toast;

import java.io.File;



/**
 * This class used to calculate the distance between two vectors. these two
 * vectors might have different length, so use dynamic programming is used to
 * calculate the minimum distance.
 *
 * The algorithm can be used for calculating the distance between two audio
 * files, where each one is represented as MFCCs (2 dimensional features)
 *
 * There are FIVE things we must specify
 *
 * <li>End point constraint</li> First Point in test pattern meet with first
 * point in reference pattern Last Point in test pattern meet with last point in
 * reference pattern
 *
 * <li>local continuity constraint</li> Support five different local path p1 -->
 * (1,0) p2 --> (2,1) p3 --> (1,1) p4 --> (1,2) p5 --> (0,1)
 *
 * <li>Global Path constraint</li> |i(k) - j(k)| <= R R is constraint value, k
 * is the timing index.
 *
 * <li>Axis Orientation</li> x-axis refers to the test pattern, y-axis refers to
 * reference/template pattern by default, we use the symmetric case.
 *
 * <li>Distance Measure</li> Use Euclidean distance measurement. For the
 * weighting function, choose W(k) = i(k) - i(k-1) + j(k) - j(k-1), which is
 * symmetric function, the advantage of choosing this weighting function is, we
 * don't need to consider the normalization value for all the summation. Because
 * for all cases, the normalization value will be constant.
 *
 * For technical detail, please look at my paper:
 * http://www.aaai.org/ocs/index.php/AAAI/AAAI11/paper/view/3791
 *
 * @author liwenzhe (Wenzhe Li)
 *
 * https://github.com/wenzheli/DTWForAudioRecognition/blob/master/README.md
 *
 * @author Emma Cox
 *
 */
public class DynamicTimeWarping2D extends DynamicTimeWarping {

    public Context context;



    private String userName;
    private String email;
    private String phone;
    private int accuracyThreshold;
    private String filePath;


    public static double[] variance;

    /**
     * Default Constructor
     */
    public DynamicTimeWarping2D (){

    }

    /**
     * Constructor with args
     * @param context
     * @param filePath
     * @param userName
     * @param email
     * @param phone
     * @param accuracyThreshold
     */
    public DynamicTimeWarping2D(Context context, String filePath,String userName, String email, String phone, int accuracyThreshold){
        this.context = context;
         this.filePath = filePath;
        this.userName = userName;
        this.email = email;
        this.phone = phone;
        this.accuracyThreshold = accuracyThreshold;

    }


    /**
     * Takes the Feature vectors from Monitor Wav File Event and Tagged Wav File Event and calculates difference
     * @param test
     * @param reference
     * @param monitorDataSource
     */
    public   void DynamicTimeWrapping2D(double[][] test, double[][] reference, String monitorDataSource) {

        // by default variance is 1 for each dimension.
        variance = new double[test[0].length];
        for (int i = 0; i < variance.length; i++)
            variance[i] = 1;

        calDistance(test, reference, variance, monitorDataSource);
    }

    public   void DynamicTimeWrapping2D(double[][] test, double[][] reference, double[] variance, String monitorDataSource) {

        calDistance(test, reference, variance, monitorDataSource);
    }

    public void calDistance(double[][] test, double[][] reference, double[] variance, final String monitorDataSource) {

        int n = test.length;
        int m = reference.length;

        // DP for calculating the minimum distance between two vector.
        // DTW[i,j] = minimum distance between vector test[0..i] and reference[0..j]
        double[][] DTW = new double[n][m];

        // initialization
        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++)
                DTW[i][j] = Double.MAX_VALUE;

        // initialize base case
        DTW[0][0] = getDistance(test[0], reference[0]);

        // initialize boundary condition.
        for (int i = 1; i < n; i++)
            DTW[i][0] = DTW[i - 1][0] + getDistance(test[i], reference[0]);

        for (int i = 1; i < m; i++)
            DTW[0][i] = DTW[0][i - 1] + getDistance(test[0], reference[i]);

        // DP comes here...
        for (int i = 1; i < n; i++) {
            for (int j = Math.max(1, i - globalPathConstraint); j < Math.min(m, i + globalPathConstraint); j++) { // consider
                // five
                // different
                // moves.
                double cost = getDistance(test[i], reference[j]);
                double d1 = cost + DTW[i - 1][j];
                double d2 = cost + DTW[i][j - 1];
                double d3 = 2 * cost + DTW[i - 1][j - 1];
                double d4 = Double.MAX_VALUE;
                if (j > 1)
                    d4 = 3 * cost + DTW[i - 1][j - 2];
                double d5 = Double.MAX_VALUE;
                if (i > 1)
                    d5 = 3 * cost + DTW[i - 2][j - 1];

                DTW[i][j] = getMin(d1, d2, d3, d4, d5);
            }
        }

        double DTWDistance = DTW[n - 1][m - 1] / (m + n);
        Toast.makeText(  context, "DTW " +   DTWDistance , Toast.LENGTH_SHORT).show();


        if (DTWDistance < accuracyThreshold) {



            //Sends email notification if threshold is high enough.
            Thread thread=new Thread(){
                @Override
                public void run() {


                    GMailSender sender = new GMailSender("ecox6464@gmail.com","ProjectPassword22", context);
                    try{
                        sender.sendMailAttach("Dog Audio Monitor", ("Hi " +  userName + " The Dog Audio Monitor has detected you dogs barking ! Download the attachment to listen.  And please call your emergency contact if needed : " + phone   ), "ecox6464@gmail.com", email,   monitorDataSource);

                    }catch (Exception lang){

                    }
                    try {
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            };
            thread.start();




        }else{

            //Deletes event and original 30 second Monitor wav from directory after comparing

            File event = new File(monitorDataSource);
            if (event.exists() ){
                event.delete();
            }

            File source = new File(filePath);
            if (source.exists()){
                source.delete();
            }
        }


    }


    /**
     * Calculate difference between vectors
     * @param vec1
     * @param vec2
     * @return
     */
    private static double getDistance(double[] vec1, double[] vec2) {
        double distance = 0.0;
        for (int i = 0; i < vec1.length; i++)
            distance += (vec1[i] - vec2[i]) * (vec1[i] - vec2[i]) / variance[i];

        return Math.sqrt(distance);
    }
}






