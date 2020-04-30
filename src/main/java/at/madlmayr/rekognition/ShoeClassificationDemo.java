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
        demo.startAndRun();
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
        LOGGER.info("ProjectVersionArn for the AWS Rekognition Model: '{}'", projectVersionArn);

        // we save all the data to a properties file, in order to have it later for the restart of the second part.
        properties.setProperty("project-arn", projectArn);
        properties.setProperty("project-version-arn", projectVersionArn);
        properties.setProperty("bucket-name", bucketName);

        properties.store(stream, "Writing Data, 1st drop");

        LOGGER.info("We now start training the model. Training the model will take round about 60 minutes ...");
        LOGGER.info("Feel free to CTRL+C and let the model train and come back later.");
        String version = shoes.train(projectArn, projectVersionArn, bucketName, "output", bucketName, pathToTrainManifest, bucketName, pathToTestManifest);

        properties.setProperty("model-version-arn", version);
        properties.store(stream, "Writing Data, 2nd drop");
        stream.close();
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

        String projectVersion = properties.getProperty("project-version");
        String projectArn = properties.getProperty("project-arn");
        String projectVersionArn = properties.getProperty("project-version-arn");
        String bucketName = properties.getProperty("bucket-name");

        Model shoes = new Model();
        LOGGER.info("Model is starting. This is taking round about 10 min");
        shoes.start(projectVersionArn, projectArn, projectVersion);
        LOGGER.info("Bucket '{}'", bucketName);
        shoes.detect(projectVersionArn, bucketName, "shoes/test/canvasshoes/26.jpg");

        shoes.stop(projectVersionArn);
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

        String projectArn = properties.getProperty("project-arn");
        Model shoes = new Model();
        LOGGER.info("Removing Project '{}'", projectArn);
        shoes.remove(projectArn);

        String bucketName = properties.getProperty("bucket-name");
        LOGGER.info("Removing Bucket '{}'", bucketName);
        RemoteBucket.createExistingBucket(properties.getProperty("bucketName")).cleanup();
    }

}

