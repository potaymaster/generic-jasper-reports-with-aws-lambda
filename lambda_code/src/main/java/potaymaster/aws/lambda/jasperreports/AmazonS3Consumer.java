package potaymaster.aws.lambda.jasperreports;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.core.ResponseBytes;

public class AmazonS3Consumer {
    
	static final String fileName = "/tmp/template.jrxml";
	
	private LambdaLogger logger;

	public AmazonS3Consumer(LambdaLogger logger) {
		this.logger = logger;
	}
	
	public void retrieveTemplateFromS3(String key_name) throws IOException {
		String bucket_name = System.getenv("BUCKET_NAME");
    	logger.log("Downloading file " + key_name + " from bucket " + bucket_name + "...\n");

		Region region = Region.EU_WEST_1;
        S3Client s3 = S3Client.builder()
                .region(region)
                .build();

		try {
			GetObjectRequest objectRequest = GetObjectRequest.builder()
				.bucket(bucket_name)
				.key(key_name)
				.build();
		
            ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(objectRequest);
            byte[] data = objectBytes.asByteArray();

            // Write the data to a local file
            File myFile = new File(fileName);
            OutputStream os = new FileOutputStream(myFile);
            os.write(data);
            logger.log("Successfully obtained bytes from an S3 object");
            os.close();

        } catch (IOException ex) {
            logger.log("There was an error when reading template file: " + ex.getMessage());
			throw ex;
        } catch (S3Exception e) {
			logger.log("There was an error when creating the output template file: " + e.getMessage());
		   throw e;
        }
    }
}