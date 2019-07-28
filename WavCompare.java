package com.example.androidaudiorecorder;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;

//Import methods from DynamicTimeWarping class
import static com.example.androidaudiorecorder.DynamicTimeWarping.getMin;
import static com.example.androidaudiorecorder.DynamicTimeWarping.globalPathConstraint;


/**
 * Application: DogAudioMonitor
 * Component: Wav File Filter
 *
 *
 * This class is an Activity that takes the enrollment wav file and filters out the high energy sections.
 * Each section is considered a significant audio "Event" and converted into it's own individual wav file.
 * This class initiates Enrollment wavFile And uses an Instance of the RecordWavTask to execute its recording.
 * The user can start Recording and stop anytime before moving on the the WavEventFilter Activity.
 *
 * @author Emma Cox
 * @author Dr. Andrew Greensted
 * @author Joren six
 * @author liwenzhe (Wenzhe Li)
 *
 * http://www.labbookpages.co.uk/audio/javaWavFiles.html
 *
 */
public class WavCompare  {


    //Class Instance Vars

    private String userName;
    private String email;
    private String phone;
    private int accuracyThreshold;
    private String directory;
    private static String filePath;


    //Used to Collect all the Groups of RMS values for each event
    private static ArrayList<ArrayList<Integer>> eventRMSIDCollect = new ArrayList<ArrayList<Integer>>();

    //Used to Collect all the frames for the events
    private static ArrayList<ArrayList<Double>> bufferEventsCollect;


    private static List<File> taggedWavs;

    private static BlockingQueue<String> fileQueue;

    private static Context context;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getAccuracyThreshold() {
        return accuracyThreshold;
    }

    public void setAccuracyThreshold(int accuracyThreshold) {
        this.accuracyThreshold = accuracyThreshold;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    /**
     * Default Constructor
     */
    public WavCompare() {

    }

    /**
     * Constructor Recieves parameters from calling class (AudioMonitorActivity|)
     *
     * @param context
     * @param fileQueue
     * @param username
     * @param email
     * @param phone
     * @param accuracy
     * @param taggedWavs
     * @param directory
     */
    public WavCompare(Context context, BlockingQueue fileQueue, String username, String email, String phone, int accuracy, List<File> taggedWavs, String directory) {

        this.context = context;
        this.fileQueue = fileQueue;
        this.userName = username;
        this.email = email;
        this.phone = phone;
        this.accuracyThreshold = accuracy;
        this.taggedWavs = taggedWavs;
        this.directory = directory;
    }


    /**
     * Takes the filePath from FileQueue in AudioMonitorActivity and Creates a WavFile object
     * Reads WavFile object into an Array buffer of frames
     *
     * @param monitorFilePath
     * @param context
     */
    public void startCompare(String monitorFilePath, Context context) {

        try {


            // Open the wav file specified as the first argument
            WavFile wavFile = WavFile.openWavFile(new File(monitorFilePath));

            filePath = monitorFilePath;


            int sampleRate = (int) wavFile.getSampleRate();


            // Create a variable that stores number of frames in file.
            int numFrames = (int) wavFile.getNumFrames();

            double duration = numFrames / sampleRate;


            // Get the number of audio channels in the wav file
            int numChannels = wavFile.getNumChannels();

            // Create a buffer of frames
            double[] buffer = new double[numFrames * numChannels];

            int framesRead;

            do {
                // Read frames into buffer
                framesRead = wavFile.readFrames(buffer, numFrames);

            } while (framesRead != 0);


            // Array List to store 100 frames in each element
            ArrayList<double[]> frameCollect = new ArrayList<double[]>();

            // Splits buffer into chunks of 441 frames and adds to frameCollect
            splitBufferAddToList(buffer, 441, frameCollect, context);

            //Gets high energy sections(events) from buffer
            getSignificantEvents(buffer, eventRMSIDCollect, context);

            // Close the wavFile
            wavFile.close();

        } catch (Exception e) {
            System.err.println(e);
        }

    }


    /**
     * Splits array into a smaller arrays of a specified chunk size.
     *
     * @param array
     * @param chunkSize
     * @param frameCollect
     * @param context
     */
    public static void splitBufferAddToList(double[] array, int chunkSize, ArrayList<double[]> frameCollect, Context context) {

        int numOfChunks = (int) Math.ceil((double) array.length / chunkSize);
        double[][] output = new double[numOfChunks][];

        for (int i = 0; i < numOfChunks; ++i) {

            int start = i * chunkSize;

            int length = Math.min(array.length - start, chunkSize);

            double[] temp = new double[length];

            System.arraycopy(array, start, temp, 0, length);

            output[i] = temp;

            frameCollect.add(temp);

        }


        for (int loop = 0; loop < frameCollect.size(); loop++) {
        }
        RMSCollect(frameCollect, context);

    }

    /**
     * Find the RMS of each element of 441 frames .
     * Add each value to an ArrayList
     *
     * @param frameCollect
     * @param context
     */
    public static void RMSCollect(ArrayList<double[]> frameCollect, Context context) {
        // Find the RMS of each element of 100 frames.
        // Add each value to ArrayList

        // ArrayList to contain all RMS values

        ArrayList<Double> rmsValues = new ArrayList<Double>();

        for (int loop = 0; loop < frameCollect.size(); loop++) {
            rmsValues.add(rootMeanSquare(context, frameCollect.get(loop)));

        }


        //Calculates a RMS threshold value.
        energyThreshold(rmsValues, context);

    }


    /**
     * Algorithm that calculates the average RMS value of each element of frameCollection
     *
     * @param context
     * @param nums
     * @return
     */
    public static double rootMeanSquare(Context context, double... nums) {
        double sum = 0.0;
        for (double num : nums)
            sum += num * num;
        return Math.sqrt(sum / nums.length);
    }

    /**
     * Uses Threshold value to filter out all rmsValues from ArrayList that are greater or equal to it
     *
     * @param rmsValues
     * @param context
     */
    public static void energyThreshold(ArrayList<Double> rmsValues, Context context) {

        double total = 0;
        double average = 0;

        for (int loop = 0; loop < rmsValues.size(); loop++) {
            // System.out.println("RMS " + rmsValues.get(loop));
            total += rmsValues.get(loop);

        }

        average = total / rmsValues.size();



        double threshold = average * 1.3;



        //Find Significant Events for
        significantEventIDs(threshold, rmsValues, context);

    }

    /**
     * Uses Threshold value to filter out all rmsValues from ArrayList that are greater or equal to it
     *
     * @param threshold
     * @param rmsValues
     * @param context
     */
    public static void significantEventIDs(double threshold, ArrayList<Double> rmsValues, Context context) {


        ArrayList<Integer> significantEventRMSID = new ArrayList<Integer>();

        double limit = threshold;

        for (int loop = 0; loop < rmsValues.size(); loop++) {

             /*
             Any RMS value greater than or equal to threshold adds their position in loop to an Arraylist
             as a means to identify the the high energy frames from the RMS value it represents .
             */

            if (rmsValues.get(loop) >= limit) {
                significantEventRMSID.add(loop);

                //This will continue until an RMS value in list drops below threshold


            } else if (rmsValues.get(loop) < limit && significantEventRMSID.size() > 0) {

                eventRMSIDCollect.add(significantEventRMSID);

                //The Arraylist is added to an ArrayList of Integer ArrayLists
                //This adds all each collection of high RMS IDs to an individual element.


                significantEventRMSID = new ArrayList<Integer>();

                /*The signigicantEventRMSID array is then cleared until the next
                value in loop is greater than or equal to threshold
                 */


            }
        }


    }

    /**
     * This takes the ArrayList consisting of Collections of RMSIDs and uses them to identify the original frames
     * from buffer that make up each significant event.
     *
     * @param buffer
     * @param significantEventRMSIDCollect
     * @param context
     */
    public void getSignificantEvents(double[] buffer,
                                     ArrayList<ArrayList<Integer>> significantEventRMSIDCollect, Context context) {


        //Each element is a frame within the event
        ArrayList<Double> bufferEvent = new ArrayList<Double>();
        bufferEventsCollect = new ArrayList<ArrayList<Double>>();


        //Outer loop iterates through each event (Collection of IDS) in the ArrayList
        for (int outerLoop = 0; outerLoop < significantEventRMSIDCollect.size(); outerLoop++) {

            /*
             To find the first frame in the buffer Event. The first RMS ID in the Collection
              is Multiplied by 441 (The amount of frames in each element of orginal enrollment buffer).
              */
            int firstID = significantEventRMSIDCollect.get(outerLoop).get(0);
            int firstFrame = firstID * 441;

            /*
            To find the position of the last frame in the buffer Event. The Amount of IDs in the current element
            is Multiplied by 441 and added to the position of the first frame.
            */
            int totalIDs = significantEventRMSIDCollect.get(outerLoop).size();
            int lastFrame = (totalIDs * 441) + firstFrame;


             /*
            The innner loop gathers all frames at a position >= to firstFrame and <= lastFrame
            An ands them to the bufferEvent Collection
            Each buffer Event is added to an ArrayList
             */

            for (int innerLoop = 0; innerLoop < buffer.length; innerLoop++) {
                if (innerLoop >= firstFrame && innerLoop <= lastFrame) {
                    bufferEvent.add(buffer[innerLoop]);
                }
            }

            bufferEventsCollect.add(bufferEvent);

            // Creates a new buffer event
            bufferEvent = new ArrayList<Double>();
        }


        //If events are below a certain length they are removed from collection.
        for (int loop = bufferEventsCollect.size() - 1; loop >= 0; loop--) {
            if (bufferEventsCollect.get(loop).size() <= 20000) {
                bufferEventsCollect.remove(loop);
            }
        }


        String fileName = null;
        int fileNameID = 0;


        //Gives a numbered fileName to each event

        for (int loop = 0; loop < bufferEventsCollect.size(); loop++) {
            fileNameID++;
            fileName = "monEvent" + Integer.toString(fileNameID);
            File wavEvent = new File(context.getFilesDir(), fileName);


            //Creates wavFiles from event with unique numbered filename
            //Overwritten Each time events are created
            createEventWav(bufferEventsCollect.get(loop), fileName, context);


        }


    }


    /**
     * Creates a wavFile from Each Event
     *
     * @param event
     * @param fileName
     * @param context
     */
    public void createEventWav(ArrayList<Double> event, String fileName, Context context) {

        File wavEvent = new File(directory + fileName + ".wav");


        //    Toast.makeText(  context, "fielname  " +   fileName, Toast.LENGTH_SHORT).show();

        try {

            int sampleRate = 44100; // Samples per second
            double duration = event.size() / sampleRate; // Seconds

            // Calculate the number of frames required for specified duration
            long numFrames = (long) (event.size());


            // Create a wav file with the name specified as the first argument
            WavFile wavFileEvent = WavFile.newWavFile(wavEvent, 1, numFrames, 16, sampleRate);


            // Create a buffer
            double[] bufferEvent = new double[(int) numFrames];

            for (int loop = 0; loop < event.size(); loop++) {
                bufferEvent[loop] = event.get(loop);
            }

            int numberFrames = (int) numFrames;

            wavFileEvent.writeFrames(bufferEvent, numberFrames);


            // Close the wavFile
            wavFileEvent.close();

            File monWavEvent = new File("/data/data/com.example.androidaudiorecorder/files/" + fileName + ".wav");

            String monitorEventSource = monWavEvent.getAbsolutePath();


            getTaggedWavEvents(monitorEventSource);


        } catch (Exception e) {
            System.err.println(e);
        }

    }


    /**
     * Takes the filePath from the Monitor Wav file Events and gets the filePath from each Tagged Wav FIle Event
     * Extracts MFCCs from both Wav Files
     *
     * @param monitorEventPath
     * @throws 'Exception'
     */
    public void getTaggedWavEvents(String monitorEventPath) throws Exception {

        //Gets all Tagged Wavs in loop
        for (int loop = 0; loop < taggedWavs.size(); loop++) {
            String taggedEventPath = taggedWavs.get(loop).getAbsolutePath();

            //Gets MFCC from both Wav Files
            getMFCCs(monitorEventPath, taggedEventPath);
        }
    }


    /**
     * Using Tarsos MFCC class Extracts the MFCC values from the Monitor Event Wav Fle and the Tagged Event WavFIle
     *
     * @param monitorEventPath
     * @param taggedEventPath
     * @throws 'Exception'
     */
    public void getMFCCs(String monitorEventPath, String taggedEventPath) throws Exception {

        //Open Wav File from monitot Event filepath
        WavFile wavFile = WavFile.openWavFile(new File(monitorEventPath));


        int sampleRate = (int) wavFile.getSampleRate();


        int bufferSize = 882;// twenty miliseconds window to Extract
        int bufferOverlap = 441; //shift window by 10 milliseconds between Extractions

        //List of MFCC values from Monitor Event
        final List<float[]> monitorEventMFCCList = new ArrayList<>();

        //List of MFCC values from Tagged Event
        final List<float[]> taggedEventMFCCList = new ArrayList<>();

        InputStream inStreamMonitorEvent = new FileInputStream(monitorEventPath);
        InputStream inStreamTaggedEvent = new FileInputStream(taggedEventPath);

        //sends float arrays to registered AudioProcessor for Monitor and Tagged Event
        AudioDispatcher dispatcherMonitor = new AudioDispatcher(new UniversalAudioInputStream(inStreamMonitorEvent, new TarsosDSPAudioFormat(sampleRate, bufferSize, 1, true, true)), bufferSize, bufferOverlap);
        AudioDispatcher dispatcherTagged = new AudioDispatcher(new UniversalAudioInputStream(inStreamTaggedEvent, new TarsosDSPAudioFormat(sampleRate, bufferSize, 1, true, true)), bufferSize, bufferOverlap);

        //Individual MFCC values form Monitor Event and Tagged Event
        final MFCC mfccMon = new MFCC(bufferSize, sampleRate, 20, 50, 300, 16000);
        final MFCC mfccTag = new MFCC(bufferSize, sampleRate, 20, 50, 300, 16000);


        dispatcherMonitor.addAudioProcessor(mfccMon);
        dispatcherMonitor.addAudioProcessor(new AudioProcessor() {

            @Override
            public void processingFinished() {
            }

            @Override
            /**
             * Extracts all MFCC values from Monitor Event and adds them to list
             */
            public boolean process(AudioEvent audioEvent) {
                monitorEventMFCCList.add(mfccMon.getMFCC());
                return true;
            }
        });
        dispatcherMonitor.run();

        dispatcherTagged.addAudioProcessor(mfccTag);
        dispatcherTagged.addAudioProcessor(new AudioProcessor() {

            @Override
            public void processingFinished() {
            }

            @Override
            /**
             * Extracts all MFCC values from Tagged Event and adds them to list
             */
            public boolean process(AudioEvent audioEvent) {
                taggedEventMFCCList.add(mfccTag.getMFCC());
                return true;
            }
        });
        dispatcherTagged.run();

        //Dimensions of featureVector
        // will alyways be a varying no of groups of 20
        int monitorVectorDimensions1 = 0;
        int monitorVectorDimensions2 = 20;


        // How many groups of 20 MFCC values there are
        for (int loop = 0; loop < monitorEventMFCCList.size(); loop++) {

            monitorVectorDimensions1++;

        }


        //Fills Monitor Event Feature Vector with MFCC values
        double[][] monitorEventFeatureVector = new double[monitorVectorDimensions1][monitorVectorDimensions2];

        for (int outerLoop = 0; outerLoop < monitorEventMFCCList.size(); outerLoop++) {

            for (int innerLoop = 0; innerLoop < monitorEventMFCCList.get(outerLoop).length; innerLoop++) {
                monitorEventFeatureVector[outerLoop][innerLoop] = monitorEventMFCCList.get(outerLoop)[innerLoop];
            }
        }


        //Dimensions of featureVector
        // will alyways be a varying no of groups of 20
        int taggedVectorDimension1 = 0;
        int taggedVectorDimension2 = 20;

        // How many groups of 20 MFCC values there are
        for (int loop = 0; loop < taggedEventMFCCList.size(); loop++) {
            taggedVectorDimension1++;

        }


        double[][] taggedEventFeatureVector = new double[taggedVectorDimension1][taggedVectorDimension2];

        for (int loop = 0; loop < taggedEventMFCCList.size(); loop++) {

            for (int loop2 = 0; loop2 < taggedEventMFCCList.get(loop).length; loop2++) {
                taggedEventFeatureVector[loop][loop2] = taggedEventMFCCList.get(loop)[loop2];
            }
        }

        //Fills Tagged Event Feature Vector with MFCC values
        for (int outerLoop = 0; outerLoop < taggedEventFeatureVector.length; outerLoop++) {
            System.out.println(" ");
            for (int innerLoop = 0; innerLoop < taggedEventFeatureVector[outerLoop].length; innerLoop++) {
                System.out.println(innerLoop + " " + taggedEventFeatureVector[outerLoop][innerLoop]);
            }
        }

        //Takes the Feature vectors from Monitor Wav File Event and Tagged Wav File Event and calculates difference

        DynamicTimeWarping2D dtw = new DynamicTimeWarping2D(context,  filePath, userName, email, phone, accuracyThreshold);
        dtw.DynamicTimeWrapping2D(monitorEventFeatureVector, taggedEventFeatureVector, monitorEventPath);

    }

}

