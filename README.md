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