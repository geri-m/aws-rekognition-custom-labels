# AWS Rekognition

Testing AWS Rekognition with Custom Labels. I'm putting together the sample code from here: https://docs.aws.amazon.com/rekognition/latest/customlabels-dg/cp-create-project.html#cp-sdk

## Preparation

Make sure you created a user with ID & Key.

Simple put in in `.bash_profile`. (at least on OSX)

```
# Private
export AWS_ACCESS_KEY_ID=xxx
export AWS_SECRET_ACCESS_KEY=yyy
``` 

The Java SDK reads the environment variables and uses them to access the AWS API.

https://docs.aws.amazon.com/de_de/rekognition/latest/dg/rekognition-dg.pdf

## Setup Project with your ARNs

1) Make a copy of `project.properties` and name it `local.properties`
2) Put Project ARN and Project Version ARN into the properties file. 

### ProjectArn

```
aws rekognition  describe-projects
```

### Project-Version-Arn

```
aws rekognition describe-project-versions --project-arn <enter-project-arn-here>
```

## How to use.

The `ShoeClassifcationDemo` contains the main function for the demo. In the Main function you find three calls, which
can be done also independently. 

```
public static void main(String[] args) throws Exception {
    ShoeClassificationDemo demo = new ShoeClassificationDemo();
    demo.createAndTrainModel();
    demo.startAndRun();
    demo.cleanUp();
}
```

### createAndTrainModel

Upload the images to S3, creates the Manifest files, creats and trains a model. This step take up to an hour. (model
training is time consuming). After the process is started you can stop the application, as the training will contine.

The relevant information on project name etc. is stored in the `local.properties` so you can contine later. 

### startAndRun

This step requires that the model is trained and stopped. Thru this method the model is started ("Model-as-a-Service")
and we run a classifcation process with one image. Then the model is stopped again. This step takes about 10 minutes.
The most time consuming part is starting the model. 

### cleanUp

We remove the model versions, the model as well as the images from S3 inkl. the Manifest. 


## Thruput

Round about 1.000 Images per Minute.