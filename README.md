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


## Thruput
