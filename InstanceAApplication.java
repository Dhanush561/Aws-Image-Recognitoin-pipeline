import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.regions.Regions;

import java.util.List;

public class InstanceAApplication {
    public static void main(String[] args) {
        final AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                                    .withRegion(Regions.US_EAST_1) // Specify the S3 bucket region
                                    .build();
        final AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard()
                                          .withRegion(Regions.US_EAST_1) // Specify the Rekognition client region if needed
                                          .build();
        final AmazonSQS sqsClient = AmazonSQSClientBuilder.standard()
                                  .withRegion(Regions.US_EAST_1) // Specify the SQS queue region if needed
                                  .build();

        final String bucketName = "njit-cs-643"; // Your S3 Bucket name
        final String queueUrl = "https://sqs.ap-southeast-2.amazonaws.com/975049993809/instanceA"; // Your SQS Queue URL

        // List the objects in the S3 bucket
        ListObjectsV2Result result = s3Client.listObjectsV2(bucketName);
        List<S3ObjectSummary> objects = result.getObjectSummaries();

        for (S3ObjectSummary os : objects) {
            String photo = os.getKey();
            DetectLabelsRequest request = new DetectLabelsRequest()
                    .withImage(new Image().withS3Object(new S3Object().withName(photo).withBucket(bucketName)))
                    .withMaxLabels(10)
                    .withMinConfidence(75F);

            DetectLabelsResult response = rekognitionClient.detectLabels(request);
            for (Label label : response.getLabels()) {
                if ("Car".equals(label.getName()) && label.getConfidence() > 90) {
                    // Send a message to the SQS queue with the name of the image
                    SendMessageRequest send_msg_request = new SendMessageRequest()
                            .withQueueUrl(queueUrl)
                            .withMessageBody(photo);
                    sqsClient.sendMessage(send_msg_request);
                }
            }
        }

        // Signal the end of image processing to the queue
        SendMessageRequest sendTerminationMsg = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody("-1");
        sqsClient.sendMessage(sendTerminationMsg);
    }
}
