package at.madlmayr.rekognition;

import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Principal;
import com.amazonaws.auth.policy.Resource;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.auth.policy.actions.S3Actions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Random;

public class RemoteBucket {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteBucket.class);

    private final AmazonS3 s3Client;
    private final String name;


    public RemoteBucket(final String preFix) {
        s3Client = AmazonS3ClientBuilder.defaultClient();
        name = preFix + "-" + givenUsingJava8_whenGeneratingRandomAlphabeticString_thenCorrect();
        LOGGER.info("Bucket Name {}", name);
    }

    // Sets a public read policy on the bucket.
    public static String getPublicReadPolicy(String bucketName) {
        Policy bucket_policy = new Policy().withStatements(
                new Statement(Statement.Effect.Allow)
                        .withPrincipals(Principal.AllUsers)
                        .withActions(S3Actions.GetObject)
                        .withResources(new Resource(
                                "arn:aws:s3:::" + bucketName + "/*")));
        return bucket_policy.toJson();
    }

    public String createBucket() throws DemoException {
        if (s3Client.doesBucketExistV2(name)) {
            LOGGER.warn("Bucket '{}' already exists.", name);
        } else {
            try {
                s3Client.createBucket(name);
                s3Client.setBucketPolicy(name, getPublicReadPolicy(name));
            } catch (AmazonS3Exception e) {
                LOGGER.error(e.getErrorMessage());
                throw new DemoException(e.getErrorMessage());
            }
        }
        return name;
    }

    public void updateLoadImages(String pathToTrainManifest, String pathToTestManifest) throws DemoException, IOException {

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL urlToTrainFile = classLoader.getResource(pathToTrainManifest);
        if (urlToTrainFile == null) {
            throw new DemoException("Unable to read train.manifest");
        }

        URL urlToTestFile = classLoader.getResource(pathToTestManifest);
        if (urlToTestFile == null) {
            throw new DemoException("Unable to read test.manifest");
        }

        // These are the File we are going to write.
        File trainFile = new File(urlToTrainFile.getFile());
        File testFile = new File(urlToTestFile.getFile());

        trainFile.createNewFile();
        testFile.createNewFile();

        BufferedWriter trainWriter = new BufferedWriter(new FileWriter(trainFile));
        BufferedWriter testWriter = new BufferedWriter(new FileWriter(testFile));

        // Update Train Data
        int counterOfImages = 1;
        for (counterOfImages = 1; counterOfImages < 26; counterOfImages++) {
            uploadImage("shoes/train/canvasshoes/" + counterOfImages + ".jpg");
            writeManifestEntry(trainWriter, "shoes/train/canvasshoes/" + counterOfImages + ".jpg", "canvasshoes");
            uploadImage("shoes/train/chunkysneakers/" + counterOfImages + ".jpg");
            writeManifestEntry(trainWriter, "shoes/train/chunkysneakers/" + counterOfImages + ".jpg", "chunkysneakers");
        }

        // Update Test Data
        for (counterOfImages = 26; counterOfImages < 50; counterOfImages++) {
            uploadImage("shoes/test/canvasshoes/" + counterOfImages + ".jpg");
            writeManifestEntry(testWriter, "shoes/test/canvasshoes/" + counterOfImages + ".jpg", "canvasshoes");
            uploadImage("shoes/test/chunkysneakers/" + counterOfImages + ".jpg");
            writeManifestEntry(testWriter, "shoes/test/chunkysneakers/" + counterOfImages + ".jpg", "chunkysneakers");
        }

        trainWriter.close();
        testWriter.close();

        uploadImage(pathToTestManifest);
        uploadImage(pathToTrainManifest);
    }

    public void writeManifestEntry(BufferedWriter writer, String path, String type) throws IOException {
        // this is not a very nice construct, but does to job for now.
        writer.write("{\"source-ref\":\"s3://" + this.name + "/" + path + "\", \"shoe-type\":1, \"shoe-type-metadata\":{ \"confidence\":1, \"job-name\":\"labeling-job/shoe-type\", \"class-name\":\"" + type + "\", \"human-annotated\":\"yes\", \"creation-date\":\"2020-04-20T14:17:37.603Z\", \"type\":\"groundtruth/image-classification\" } }\n");
    }

    private void uploadImage(String path) throws DemoException {
        // Upload a file as a new object with ContentType and title specified.
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL someFile = classLoader.getResource(path);
        if (someFile == null) {
            throw new DemoException("Unable to read local file " + path);
        }
        PutObjectRequest request = new PutObjectRequest(name, path, new File(someFile.getFile()));
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/jpeg");
        metadata.addUserMetadata("title", path);
        request.setMetadata(metadata);
        s3Client.putObject(request);
        LOGGER.info("'{}' uploaded.", path);
    }

    public void cleanup() throws DemoException {
        if (!s3Client.doesBucketExistV2(name)) {
            LOGGER.warn("Bucket '{}' does NOT exists.", name);
        } else {
            try {
                ObjectListing objectListing = s3Client.listObjects(name);
                while (true) {
                    for (S3ObjectSummary s3ObjectSummary : objectListing.getObjectSummaries()) {
                        s3Client.deleteObject(name, s3ObjectSummary.getKey());
                    }

                    // If the bucket contains many objects, the listObjects() call
                    // might not return all of the objects in the first listing. Check to
                    // see whether the listing was truncated. If so, retrieve the next page of objects
                    // and delete them.
                    if (objectListing.isTruncated()) {
                        objectListing = s3Client.listNextBatchOfObjects(objectListing);
                    } else {
                        break;
                    }
                }

                // Delete all object versions (required for versioned buckets).
                VersionListing versionList = s3Client.listVersions(new ListVersionsRequest().withBucketName(name));
                while (true) {
                    for (S3VersionSummary vs : versionList.getVersionSummaries()) {
                        s3Client.deleteVersion(name, vs.getKey(), vs.getVersionId());
                    }

                    if (versionList.isTruncated()) {
                        versionList = s3Client.listNextBatchOfVersions(versionList);
                    } else {
                        break;
                    }
                }

                s3Client.deleteBucket(name);
                LOGGER.info("Bucket {} deleted", name);
            } catch (AmazonS3Exception e) {
                LOGGER.error(e.getErrorMessage());
                throw new DemoException(e.getErrorMessage());
            }
        }
    }


    // taken from https://www.baeldung.com/java-random-string
    private String givenUsingJava8_whenGeneratingRandomAlphabeticString_thenCorrect() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return generatedString;
    }

}
