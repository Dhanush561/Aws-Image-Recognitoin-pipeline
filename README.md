AWS Image Recognition Pipeline 

This details the setup and operation of an image recognition pipeline utilizing AWS EC2 instances, S3 for image storage, SQS for message queuing, and Rekognition for image analysis. The system comprises two main components: Instance A for car detection in images and Instance B for text detection in images identified by Instance A.


Step 1: S3 Bucket Setup

1. Log into AWS Management Console and navigate to the S3 service.
2. Create a new bucket named `njit-cs-643` in the `us-east-1` region.
3. Upload your target images to this bucket for processing.

Step 2: SQS Queue Configuration

1. Access the SQS service from the AWS Management Console.
2. Create a new standard queue named `instanceA`.
3. Note down the queue URL provided after creation, which should resemble `https://sqs.ap-southeast-2.amazonaws.com/975049993809/instanceA`.

Step 3: IAM Role Creation

1. Navigate to the IAM service and create a new role with EC2 as the trusted entity.
2. Attach the `AmazonS3ReadOnlyAccess`, `AmazonSQSFullAccess`, and `AmazonRekognitionFullAccess` policies.
3. Name the role descriptively, e.g., `EC2_S3_SQS_Rekognition_Role`.

Step 4: EC2 Instances Launch

1. Go to the EC2 service and launch two Amazon Linux instances.
2. Choose `t2.micro` for testing purposes, ensuring to attach the IAM role `EC2_S3_SQS_Rekognition_Role`.
3. Configure the Security Group to allow SSH access from your IP address.
4. Use an existing key pair or create a new one for SSH access.

Step 5: Application Setup on EC2

Common Setup:

Connect to each instance via SSH and set up the environment:


sudo yum update -y
sudo yum install java-1.8.0-openjdk-devel -y


Verify Java installation:


java -version
javac -version


Instance A Setup (Car Detection):

On Instance A, deploy a Java application that:

Reads images from `njit-cs-643` S3 bucket.
Utilizes AWS Rekognition to detect cars in the images.
Sends image indices with detected cars to the SQS queue https://sqs.ap-southeast-2.amazonaws.com/975049993809/instanceA`.

Instance B Setup (Text Recognition):

On Instance B, deploy a Java application that:

 Monitors the SQS queue `https://sqs.ap-southeast-2.amazonaws.com/975049993809/instanceA` for new messages.
Downloads identified images from the `njit-cs-643` S3 bucket.
Uses AWS Rekognition to detect text in these images.
Outputs detected text alongside the corresponding image indices.

Step 6: Execution

Compile and run the Java applications on each instance accordingly, ensuring the AWS SDK for Java is correctly referenced in the classpath.

Compilation Example:

bash
javac -cp ".:/path/to/aws-java-sdk-1.12.671/lib/*:/path/to/aws-java-sdk-1.12.671/third-party/lib/*" InstanceBApplication.java


Execution Example:


java -cp ".:/path/to/aws-java-sdk-1.12.671/lib/*:/path/to/aws-java-sdk-1.12.671/third-party/lib/*" InstanceBApplication
 



Here is the Final output of the pipeline I received.


