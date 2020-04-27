package at.madlmayr.rekognition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        // Reading Project Properties from Properties File.
        Properties properties = new Properties();

        InputStream stream = Main.class.getClassLoader().getResourceAsStream("local.properties");
        if (stream != null) {
            properties.load(stream);
            stream.close();
        } else {
            LOGGER.error("Unable to load local.properties from resources folder");
            return;
        }

        // Put Information in local variables, as easier to use.
        String projectArn = properties.getProperty("project-arn");
        String projectVersionArn = properties.getProperty("project-version-arn");
        String projectVersion = properties.getProperty("project-version");

        RemoteBucket r = new RemoteBucket("demo-shoes");
        r.createBucket();
        r.updateLoadImages();
        r.cleanup();


        Model dresses = new Model();
        // dresses.start(projectVersionArn, projectArn, projectVersion);


        // dresses.stop(projectVersionArn);
        /*
        for (String image : images) {
            dresses.detect(projectVersionArn, "madlmayr-dresses", image);
        }
        */
    }

}

