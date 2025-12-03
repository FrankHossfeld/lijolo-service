package io.github.lijolo.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.waiters.S3Waiter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AwsS3Test
    extends AbstractServiceTest {

  private final static String BUCKET_NAME      = "lijolo-upload-bucket";
  private final static String PATH_TO_DOCUMENT = "my/path/to/document";

  static final LocalStackContainer localstack;
  static final S3Client            s3Client;
  private      byte[]              pdfContent;

  static {
    localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:4.1.1")).withServices(S3);
    localstack.start();
    s3Client = S3Client.builder()
                       .endpointOverride(localstack.getEndpointOverride(S3))
                       .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(localstack.getAccessKey(),
                                                                                                        localstack.getSecretKey())))
                       .forcePathStyle(true)
                       .region(Region.of(localstack.getRegion()))
                       .build();
    AwsS3Test.createBucket("lijolo-upload-bucket");
  }

  private static void createBucket(String name) {
    S3Waiter s3Waiter = AwsS3Test.s3Client.waiter();
    AwsS3Test.s3Client.createBucket(CreateBucketRequest.builder()
                                                       .bucket(name)
                                                       .build());
    HeadBucketRequest request = HeadBucketRequest.builder()
                                                 .bucket(name)
                                                 .build();
    WaiterResponse<HeadBucketResponse> response = s3Waiter.waitUntilBucketExists(request);
    response.matched()
            .response()
            .ifPresent(e -> System.out.println("Bucket '" + name + "' created!"));
  }

  @BeforeAll
  public void beforeAll() {
    Path resourceDirectory = Paths.get("src",
                                       "test",
                                       "resources");
    String pathToResourceDirctory = resourceDirectory.toFile()
                                                     .getAbsolutePath();
    try {
      this.pdfContent = Files.readAllBytes(Paths.get(pathToResourceDirctory + "/test/pdf/Mustang-Beispiel-20221026.pdf"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void test01() {
    this.uploadedDocumentToBucket();
    byte[] pdfContentFromS3 = this.getDocumentFromBucket();
    assertNotNull(pdfContentFromS3);
    assertEquals(this.pdfContent.length,
                 pdfContentFromS3.length);
    this.deleteDocumentFromBucket();
  }

  private void deleteDocumentFromBucket() {
    DeleteObjectRequest requestGet = DeleteObjectRequest.builder()
                                                        .bucket(AwsS3Test.BUCKET_NAME)
                                                        .key(AwsS3Test.PATH_TO_DOCUMENT)
                                                        .build();
    AwsS3Test.s3Client.deleteObject(requestGet);
  }

  private byte[] getDocumentFromBucket() {
    GetObjectRequest requestGet = GetObjectRequest.builder()
                                                  .bucket(AwsS3Test.BUCKET_NAME)
                                                  .key(AwsS3Test.PATH_TO_DOCUMENT)
                                                  .build();
    ResponseBytes<GetObjectResponse> objectBytes          = AwsS3Test.s3Client.getObjectAsBytes(requestGet);
    byte[]                           myDocumentFromBucket = objectBytes.asByteArray();
    if (myDocumentFromBucket.length == 0) {
      throw new RuntimeException("No document found for path: " + AwsS3Test.PATH_TO_DOCUMENT);
    }
    return myDocumentFromBucket;
  }

  private void uploadedDocumentToBucket() {
    Map<String, String> metaData = new HashMap<>();
    metaData.put("key01",
                 "value01");
    metaData.put("key02",
                 "value02");
    PutObjectRequest request = PutObjectRequest.builder()
                                               .bucket(AwsS3Test.BUCKET_NAME)
                                               .key(AwsS3Test.PATH_TO_DOCUMENT)
                                               .metadata(metaData)
                                               .build();
    AwsS3Test.s3Client.putObject(request,
                                 RequestBody.fromBytes(this.pdfContent));
  }

}