package at.madlmayr.rekognition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    /**
     * Static Method to generate three manifest files out of a TSV File.
     * <p>
     * Create a line JSON file
     * <p>
     * https://docs.aws.amazon.com/sagemaker/latest/dg/sms-data-output.html
     * <p>
     * ** SAMPLE ***
     * {
     * "source-ref":"s3://custom-labels-console-eu-west-1-27b0b4db6f/assets/shoe-testing/1587392000/31.jpg",
     * "shoe-testing_leihnen-schuhe":1,
     * "shoe-testing_leihnen-schuhe-metadata":{
     * "confidence":1,
     * "job-name":"labeling-job/shoe-testing_leihnen-schuhe",
     * "class-name":"leihnen-schuhe",
     * "human-annotated":"yes",
     * "creation-date":"2020-04-20T14:17:37.603Z",
     * "type":"groundtruth/image-classification"
     * }
     * }
     * <p>
     * ** STRUCTURE ***
     * {
     * "source-ref": "S3 bucket location", # Required
     * "sport":0, # Required
     * "sport-metadata": { # Required
     * "class-name": "football", # Required
     * "confidence": 0.8, # Required
     * "type":"groundtruth/image-classification", # Required
     * "job-name": "identify-sport", # Not required
     * "human-annotated": "yes", # Required
     * "creation-date": "2018-10-18T22:18:13.527256" # Required#
     * }
     * }
     *
     * @throws Exception In case file access has a problem, this will be thrown.
     */

    private static void printFile() throws Exception {
        final String mini = "mini";
        final String midi = "midi";
        final String longDress = "long";

        final String train = "train";
        final String test = "test";
        final String val = "val";

        // These are the File we are going to write.
        File trainFile = new File("shoes/train/train.manifest");
        File testFile = new File("shoes/test/test.manifest");
        File valFile = new File("cal.manifest");

        trainFile.createNewFile();
        testFile.createNewFile();
        valFile.createNewFile();

        BufferedWriter trainWriter = new BufferedWriter(new FileWriter(trainFile));
        BufferedWriter testWriter = new BufferedWriter(new FileWriter(testFile));
        BufferedWriter valWriter = new BufferedWriter(new FileWriter(valFile));

        // This is the input file.
        // Structure of the input file format:
        // image_key	long	midi	mini
        // images/length/train/mini/bfv3hlqxldprr8wavnnq.jpg	0	0	1
        File file = new File(
                Model.class.getClassLoader().getResource("length_samples.tsv").getFile()
        );

        StringTokenizer st;
        BufferedReader tsvFile = new BufferedReader(new FileReader(file));
        String dataRow = tsvFile.readLine(); // Read first line.

        // Amount of Dresses in the Category Long, Mini, Midi
        int countLong = 0;
        int countMini = 0;
        int countMidi = 0;

        // Amount of Records for Test, Evaluation and Training
        int countTest = 0;
        int countVal = 0;
        int countTrain = 0;

        // Iterate row-by-row over the TSV File
        while (dataRow != null) {
            BufferedWriter temp = null;

            // Generate Token from the Line which are separated by Tab.
            st = new StringTokenizer(dataRow, "\t");
            List<String> dataArray = new ArrayList<String>();
            while (st.hasMoreElements()) {
                dataArray.add(st.nextElement().toString());
            }

            // Element on Index 0 Contains the Image Path and therefore the information if test/eval/train
            // Based on the findings we chose the appropriate File Writer.
            if (dataArray.get(0).contains(train)) {
                countTrain++;
                temp = trainWriter;
            } else if (dataArray.get(0).contains(test)) {
                countTest++;
                temp = testWriter;
            } else if (dataArray.get(0).contains(val)) {
                temp = valWriter;
                countVal++;
            } else {
                LOGGER.error("Unclear what to do with this: {}", dataArray.get(0));
                continue;
            }

            // Element on Index 0 also contains the information on midi, mini and maxi.
            if (dataArray.get(0).contains(mini)) {
                countMini++;
                temp.write("{\"source-ref\":\"s3://madlmayr-dresses/dresses/" + dataArray.get(0).replace("images/length/", "") + "\", \"dress-length\":1, \"dress-length-metadata\":{ \"confidence\":1, \"job-name\":\"labeling-job/dress-lenght\", \"class-name\":\"mini\", \"human-annotated\":\"yes\", \"creation-date\":\"2020-04-20T14:17:37.603Z\", \"type\":\"groundtruth/image-classification\" } }\n");
            } else if (dataArray.get(0).contains(midi)) {
                countMidi++;
                temp.write("{\"source-ref\":\"s3://madlmayr-dresses/dresses/" + dataArray.get(0).replace("images/length/", "") + "\", \"dress-length\":1, \"dress-length-metadata\":{ \"confidence\":1, \"job-name\":\"labeling-job/dress-lenght\", \"class-name\":\"midi\", \"human-annotated\":\"yes\", \"creation-date\":\"2020-04-20T14:17:37.603Z\", \"type\":\"groundtruth/image-classification\" } }\n");
            } else if (dataArray.get(0).contains(longDress)) {
                countLong++;
                temp.write("{\"source-ref\":\"s3://madlmayr-dresses/dresses/" + dataArray.get(0).replace("images/length/", "") + "\", \"dress-length\":1, \"dress-length-metadata\":{ \"confidence\":1, \"job-name\":\"labeling-job/dress-lenght\", \"class-name\":\"long\", \"human-annotated\":\"yes\", \"creation-date\":\"2020-04-20T14:17:37.603Z\", \"type\":\"groundtruth/image-classification\" } }\n");
            } else {
                LOGGER.error("Unclear what to do with this: {}", dataArray.get(0));
            }


            dataRow = tsvFile.readLine(); // Read next line of data.
        } //  while (dataRow != null)

        // Close the file once all data has been read.
        tsvFile.close();
        valWriter.close();
        testWriter.close();
        trainWriter.close();
        // End the printout with a blank line.
        LOGGER.info("Done");

        // Print some Stats.
        LOGGER.info("Long {}, Midi {}, Mini {}, Total {}", countLong, countMidi, countMini, (countLong + countMini + countMidi));
        LOGGER.info("Train {}, Test {}, Val {}, Total {}", countTrain, countTest, countVal, (countTrain + countTest + countVal));
    }

}
