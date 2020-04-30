package at.madlmayr.rekognition;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import com.amazonaws.waiters.Waiter;
import com.amazonaws.waiters.WaiterParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Model {

    private static final Logger LOGGER = LoggerFactory.getLogger(Model.class);

    private final AmazonRekognition rekognitionClient;

    public Model() {
        rekognitionClient = AmazonRekognitionClientBuilder.defaultClient();
    }

    public String create(String projectName) {
        LOGGER.info("createModel - Start");
        CreateProjectRequest createProjectRequest = new CreateProjectRequest()
                .withProjectName(projectName);
        CreateProjectResult response = rekognitionClient.createProject(createProjectRequest);
        LOGGER.info("createModel - Done");
        return response.getProjectArn();
    }

    public void start(String projectVersionArn, String projectArn, String versionName) {
        LOGGER.info("runModel - Start");
        int minInferenceUnits = 1;
        StartProjectVersionRequest request = new StartProjectVersionRequest()
                .withMinInferenceUnits(minInferenceUnits)
                .withProjectVersionArn(projectVersionArn);

        StartProjectVersionResult result = rekognitionClient.startProjectVersion(request);

        LOGGER.info("Status: {}", result.getStatus());
        LOGGER.info("Status: 'STARTING' might be unchanged for up to 10 min. So don't worries if nothing is happening immediatly");
        DescribeProjectVersionsRequest describeProjectVersionsRequest = new DescribeProjectVersionsRequest()
                .withVersionNames(versionName)
                .withProjectArn(projectArn);

        Waiter<DescribeProjectVersionsRequest> waiter = rekognitionClient.waiters().projectVersionRunning();
        waiter.run(new WaiterParameters<>(describeProjectVersionsRequest));
        DescribeProjectVersionsResult response = rekognitionClient.describeProjectVersions(describeProjectVersionsRequest);

        for (ProjectVersionDescription projectVersionDescription : response.getProjectVersionDescriptions()) {
            LOGGER.info("Status: {}", projectVersionDescription.getStatus());
        }
        LOGGER.info("runModel - Done");
    }

    public void stop(String projectVersionArn) {
        StopProjectVersionRequest request = new StopProjectVersionRequest()
                .withProjectVersionArn(projectVersionArn);
        StopProjectVersionResult result = rekognitionClient.stopProjectVersion(request);
        LOGGER.info("Status: {}", result.getStatus());
        LOGGER.info("Stop - Done");
    }

    public void remove(String projectVersionArn) {
        DeleteProjectRequest request = new DeleteProjectRequest().withProjectArn(projectVersionArn);
        DeleteProjectResult result = rekognitionClient.deleteProject(request);
        LOGGER.info("Status: {}", result.getStatus());
        LOGGER.info("Delete - Done");
    }


    public void train(String projectArn, String versionName, String outputBucket, String outputFolder, String trainingBucket, String trainingManifest, String testingBucket, String testingManifest) {
        LOGGER.info("trainModel - Start");
        OutputConfig outputConfig = new OutputConfig()
                .withS3Bucket(outputBucket)
                .withS3KeyPrefix(outputFolder);


        GroundTruthManifest trainingGroundTruthManifest = new GroundTruthManifest()
                .withS3Object(new S3Object()
                        .withBucket(trainingBucket)
                        .withName(trainingManifest));

        // Use the Manifest with the Line JSON to create the Training Data.
        TrainingData trainingData = new TrainingData()
                .withAssets(new Asset()
                        .withGroundTruthManifest(trainingGroundTruthManifest));

        // Create a Testing Data set out of the Training Data
        /*
        TestingData testingData = new TestingData()
                .withAutoCreate(true);
        */
        // OR
        // create a dedicated Testing Data set.

        GroundTruthManifest testingGroundTruthManifest = new GroundTruthManifest()
                .withS3Object(new S3Object()
                        .withBucket(testingBucket)
                        .withName(testingManifest));

        TestingData testingData = new TestingData()
                .withAssets(new Asset()
                        .withGroundTruthManifest(testingGroundTruthManifest));

        CreateProjectVersionRequest request = new CreateProjectVersionRequest()
                .withOutputConfig(outputConfig)
                .withProjectArn(projectArn)
                .withTrainingData(trainingData)
                .withTestingData(testingData)
                .withVersionName(versionName);

        CreateProjectVersionResult result = rekognitionClient.createProjectVersion(request);

        LOGGER.info("Model ARN: {}", result.getProjectVersionArn());

        DescribeProjectVersionsRequest describeProjectVersionsRequest = new DescribeProjectVersionsRequest()
                .withVersionNames(versionName)
                .withProjectArn(projectArn);

        Waiter<DescribeProjectVersionsRequest> waiter = rekognitionClient.waiters().projectVersionTrainingCompleted();
        waiter.run(new WaiterParameters<>(describeProjectVersionsRequest));

        DescribeProjectVersionsResult response = rekognitionClient.describeProjectVersions(describeProjectVersionsRequest);

        for (ProjectVersionDescription projectVersionDescription : response.getProjectVersionDescriptions()) {
            LOGGER.info("Status: {}", projectVersionDescription.getStatus());
        }
        LOGGER.info("trainModel - Done");
    }

    public void detect(String projectVersionArn, String bucket, String pathToImage) {
        float minConfidence = 90;
        DetectCustomLabelsRequest request = new DetectCustomLabelsRequest()
                .withProjectVersionArn(projectVersionArn)
                .withImage(new Image().withS3Object(new S3Object().withName(pathToImage).withBucket(bucket)))
                .withMinConfidence(minConfidence);

        DetectCustomLabelsResult result = rekognitionClient.detectCustomLabels(request);

        List<CustomLabel> customLabels = result.getCustomLabels();
        for (CustomLabel customLabel : customLabels) {
            LOGGER.info("Label '{}' with Confidence '{}' detected.", customLabel.getName(), customLabel.getConfidence());
        }
    }
}
