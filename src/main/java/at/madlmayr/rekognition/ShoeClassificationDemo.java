package at.madlmayr.rekognition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class ShoeClassificationDemo {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShoeClassificationDemo.class);
    private static final DateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("YYYY-MM-dd'T'HH.mm.ss");
    private static final String projectName = "shoe-classification";

    public static void main(String[] args) throws Exception {
        ShoeClassificationDemo demo = new ShoeClassificationDemo();
        demo.createAndTrainModel();
        demo.cleanUp();
    }

    private void createAndTrainModel() throws Exception {
        // We are going to save some properties to a local file to retrieve them later again.
        Properties properties = new Properties();
        OutputStream stream = new FileOutputStream(new File("local.properties"));
        if (stream == null) {
            LOGGER.error("Unable to load local.properties from resources folder");
            return;
        }

        // Put Information in local variables, as easier to use.
        String pathToTrainManifest = "shoes/train/train.manifest";
        String pathToTestManifest = "shoes/test/test.manifest";


        RemoteBucket r = new RemoteBucket(projectName);
        String bucketName = r.createBucket();

        // The Bucket Name has 8 Random digits in the end in order to make it unique
        LOGGER.info("bucketName: {}", bucketName);
        r.updateLoadImages(pathToTrainManifest, pathToTestManifest);

        Model shoes = new Model();

        // ProjectArn is required to train the model
        String projectArn = shoes.create(projectName);
        LOGGER.info("ProjectArn for the AWS Rekognition Model: '{}'", projectArn);

        // The Version Contains the Date, in order not to accidentally overwrite an exiting version.
        String projectVersionArn = projectName.concat(".").concat(SIMPLE_DATE_FORMAT.format(new Date()));
        LOGGER.info("ProjectVersionArn for the AWS Rekognition Model: '{}'", projectArn);

        // we save all the data to a properties file, in order to have it later for the restart of the second part.
        properties.setProperty("projectArn", projectArn);
        properties.setProperty("projectVersionArn", projectVersionArn);
        properties.setProperty("bucketName", bucketName);

        properties.store(stream, "Writing Data");
        stream.close();

        LOGGER.info("We now start training the model. Training the model will take round about 60 minutes");
        // shoes.train(projectArn, projectVersionArn , bucketName, "output", bucketName, pathToTrainManifest, bucketName, pathToTestManifest);
    }

    private void startAndRun() throws Exception {

        Properties properties = new Properties();
        InputStream stream = new FileInputStream(new File("local.properties"));
        if (stream != null) {
            properties.load(stream);
            stream.close();
        } else {
            LOGGER.error("Unable to load local.properties from resources folder");
            return;
        }

        String projectArn = properties.getProperty("project-arn");
        String projectVersionArn = properties.getProperty("project-version-arn");
        String projectVersion = properties.getProperty("project-version");
        String bucketName = properties.getProperty("bucketName");

        Model shoes = new Model();
        shoes.start(projectVersionArn, projectArn, projectVersion);

        // dresses.stop(projectVersionArn);
        /*
        for (String image : images) {
            dresses.detect(projectVersionArn, "madlmayr-dresses", image);
        }
        */

    }


    private void cleanUp() throws Exception {
        Properties properties = new Properties();
        InputStream stream = new FileInputStream(new File("local.properties"));
        if (stream != null) {
            properties.load(stream);
            stream.close();
        } else {
            LOGGER.error("Unable to load local.properties from resources folder");
            return;
        }

        String projectArn = properties.getProperty("projectArn");
        Model shoes = new Model();
        LOGGER.info("Removing Project '{}'", projectArn);
        shoes.remove(projectArn);

        String bucketName = properties.getProperty("bucketName");
        LOGGER.info("Removing Bucket '{}'", bucketName);
        RemoteBucket.createExistingBucket(properties.getProperty("bucketName")).cleanup();
    }

}

