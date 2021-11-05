# LocatingRecordsInLogs-gRPC
A distributed program for locating requested records in the log files using gRPC.
---
Name: Karan Malhotra
---

### Installations
+ Install [Simple Build Toolkit (SBT)](https://www.scala-sbt.org/1.x/docs/index.html)
+ Ensure you can create, compile and run Java and Scala programs.

### Development Environment
+ Windows 10
+ Java 11.0.11
+ Scala 2.13.7
+ SBT Script Version 1.5.5
+ Other dependencies exist in build.sbt and scalapb.sbt
+ IntelliJ IDEA Ultimate
+ AWS API Gateway
+ AWS Lambda

## Entire Setup of the Project:
### Setting up Log File Generator
- Create your own AWS Lambda Function, AWS API Gateway and an S3 Bucket.
- Refer this lambda function for a better understanding of the binary search analysis of the log file.
- Watch this [tutorial](https://www.youtube.com/watch?v=MgQDeKwTnDQ&feature=youtu.be) to give S3 get object access to your Lambda function.
- Launch an EC2 Instance on AWS and SSH via AWS CLI. Store the key-val pair.
- After ssh via ec2-user, type `aws configure` and enter your key-val pair.
- Install java, sbt and git on the EC2 instance.
- Clone the [Log Generator Repository](https://github.com/karanmalh0tra/LogFileGenerator).
- Change the configurations to enter your S3 Bucket and Key name. The code will always push the latest log file to the S3 Bucket.
- Perform `sbt clean compile test` to clean, compile and execute the test cases.
- `sbt run` will start generating logs and push it to your S3 Bucket.

### Steps to Run the gRPC Application
- Clone the github repo on your local machine.
- Open the command line at the root of the project.
- Enter your desired configs in application.conf.
- Enter `sbt clean compile` to create protoc files in the target location based on the .proto file present in the main/protobuf directory.
- Enter `sbt test` to run all the test cases.
- On one terminal, execute `sbt "runMain gRPCServer"`. The server will start listening on the port mentioned in the config file.
- Open another terminal and execute, `sbt "runMain gRPCClient 05:58:27,00:00:07"` where the last argument is Time followed by , and deltaTime.
- Press `y` if you're asked to create a new sbt server.

## Test Cases
1. Check whether a port is entered for the gRPC Server.
2. Tests if URL is https or not.
3. Check the format of Time
4. Check the format of deltaTime
5. Perform check of logs to see if the GET Request to the API Gateway is successful or not

## Overview
This project covers the gRPC Client-Server interaction as well as gRPC Server's interaction with the AWS API Gateway. <br/>
The gRPC Client makes a call to the gRPC Server along with a message which is a string addition of time and delta time seperated by a comma. <br/>
The gRPC Server calls the AWS API Gateway which triggers the Lambda Function. <br/>
The Lambda function checks in a binary search algorithm whether the logs in the given time range are present or not. <br/>
If the logs are present, the response returned is the md5 hash of logs with a statusCode of 200. <br/>
If no logs were present in the timerange, the response is a 400-level message with a message that logs were not present in the time range.

## Output
gRPC Server Running: <br/>
![image](https://user-images.githubusercontent.com/22276682/140577537-e2fd76cc-0b4e-4bfc-8ebb-4d23672e8d9d.png)
<br/>
When logs are not present: <br/>
![image](https://user-images.githubusercontent.com/22276682/140577876-0b033d37-1eb7-4fbb-9acd-9fbf6c7c7240.png)
<br/>
When logs are present: <br/>
![image](https://user-images.githubusercontent.com/22276682/140577790-41623998-4ff5-4e51-a054-1034e5618313.png)
<br/>

Video Link:
