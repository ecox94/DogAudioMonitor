package com.example.androidaudiorecorder;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReadWav {
    private static int sampleRate = 0;

    public static void wavRead ( File file) {
        try {


            // Open the wav file specified as the first argument
            WavFile wavFile = WavFile.openWavFile(file);

            sampleRate = (int) wavFile.getSampleRate();

            System.out.println("SR " + sampleRate);


            // Display information about the wav file

            System.out.println("");

            //Create a variable that stores number of frames in file.
            int numFrames = (int) wavFile.getNumFrames();



            // Get the number of audio channels in the wav file
            int numChannels = wavFile.getNumChannels();

            // Create a buffer of  frames
            double[] buffer = new double[numFrames * numChannels];

            int framesRead;


            do {
                // Read frames into buffer
                framesRead = wavFile.readFrames(buffer, numFrames);


            } while (framesRead != 0);

            /**for (int loop = 0; loop < buffer.length; loop ++) {
             System.out.println(" buff " + buffer[loop]);
             }**/



            //Array List to store 100 frames in each element
            ArrayList<double[]> frameCollect = new ArrayList<double[]>();


            //Splits buffer into chunks of 100 frames and adds to frameCollect
            splitBufferAddToList(buffer, 4410, frameCollect);




            // Close the enrollmentWavFile
            wavFile.close();

        } catch (Exception e) {
            System.err.println(e);
        }
    }



    //Calculates RMS of each element of result
    public static double rootMeanSquare(double... nums) {
        double sum = 0.0;
        for (double num : nums)
            sum += num * num;
        return Math.sqrt(sum / nums.length);
    }


    //Splits array into a smaller arrays of a specified chunk size.
    public static void splitBufferAddToList (double[] array, int chunkSize, ArrayList<double[]> frameCollect) {

        int numOfChunks = (int)Math.ceil((double)array.length / chunkSize);
        double[][] output = new double[numOfChunks][];

        //ArrayList to contain all RMS values

        ArrayList<Double> rmsValues = new ArrayList<Double>();



        for(int i = 0; i < numOfChunks; ++i) {

            int start = i * chunkSize;

            int length = Math.min(array.length - start, chunkSize);


            double[] temp = new double[length];

            System.arraycopy(array, start, temp, 0, length);

            output[i] = temp;

            frameCollect.add(temp);


        }
        //Find the RMS of each element of 100 frames.
        //Add each value to ArrayList
        for (int loop = 0;  loop < frameCollect.size(); loop ++) {
            rmsValues.add(rootMeanSquare(frameCollect.get(loop)));

        }




        System.out.println("RMS Values  : ");
        System.out.println("");


        for (int loop = 0;  loop < rmsValues.size() ; loop ++) {
            System.out.println((loop + 1 ) + " : "+ rmsValues.get(loop));
        }



        // calculate0Crossings(sampleRate, rmsValues);




    }




    /**
     * calculate frequency using zero crossings
     */
    public static int calculate0Crossings(int sampleRate, ArrayList<Double> audioData)
    {
        int numSamples = audioData.size();
        int numCrossing = 0;
        for (int p = 0; p < numSamples-1; p++)
        {
            if ((audioData.get(p)> 0 && audioData.get(p + 1) <= 0) ||
                    (audioData.get(p) < 0 && audioData.get(p + 1)>= 0))
            {
                numCrossing++;
            }
        }

        float numSecondsRecorded = (float)numSamples/(float)sampleRate;
        float numCycles = numCrossing/2;
        float frequency = numCycles/numSecondsRecorded;

        return (int)frequency;
    }
}



