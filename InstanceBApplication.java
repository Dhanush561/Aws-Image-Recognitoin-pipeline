import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.DetectTextRequest;
import com.amazonaws.services.rekognition.model.DetectTextResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.TextDetection;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class InstanceBApplication {

    private static final String SQS_URL = "https://sqs.ap-southeast-2.amazonaws.com/975049993809/instanceA";
    private static final String S3_BUCKET = "njit-cs-643";

    public static void main(String[] args) throws Exception {
        AmazonSQS sqs = AmazonSQSClientBuilder.standard().withRegion("ap-southeast-2").build();
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion("us-east-1").build();
        AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard().withRegion("us-east-1").build();

        while (true) {
            List<Message> messages = sqs.receiveMessage(new ReceiveMessageRequest(SQS_URL).withMaxNumberOfMessages(1)).getMessages();
            for (Message message : messages) {
                String imageIndex = message.getBody();
                if ("-1".equals(imageIndex)) {
                    System.out.println("No more images to process. Exiting.");
                    return;
                }

                InputStream inputStream = s3Client.getObject(S3_BUCKET, imageIndex).getObjectContent();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[1024];
                while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                byte[] imageBytes = buffer.toByteArray();

                DetectTextRequest request = new DetectTextRequest().withImage(new Image().withBytes(ByteBuffer.wrap(imageBytes)));
                DetectTextResult result = rekognitionClient.detectText(request);

                System.out.println("Detected labels for image " + imageIndex + ":");
                for (TextDetection text : result.getTextDetections()) {
                    System.out.println("Detected text: " + text.getDetectedText());
                }

                sqs.deleteMessage(SQS_URL, message.getReceiptHandle());
            }
        }
    }
}
