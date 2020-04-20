package at.madlmayr.rekognition;


import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.CreateProjectRequest;
import com.amazonaws.services.rekognition.model.CreateProjectResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        LOGGER.info("Main");
        String projectName = "some-classifier";

        try {
            // This creates a new Project
            AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.defaultClient();
            CreateProjectRequest createProjectRequest = new CreateProjectRequest()
                    .withProjectName(projectName);
            CreateProjectResult response = rekognitionClient.createProject(createProjectRequest);

            LOGGER.info("Project ARN: " + response.getProjectArn());


        } catch (Exception e) {
            System.out.println(e.toString());
        }
        LOGGER.info("Done...");

    }

}
