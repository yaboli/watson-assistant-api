package com.example.watsonassistantapi.controller;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceInUseException;
import com.example.watsonassistantapi.model.User;
import com.example.watsonassistantapi.repository.DynamoDBRepository;
import com.example.watsonassistantapi.service.DynamoDBService;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class DynamoDBControllerIntegrationTest {

    private static DynamoDBMapper dynamoDBMapper;
    private static AmazonDynamoDB amazonDynamoDB;

    private DynamoDBRepository repository;
    private DynamoDBService dynamoDBService;

    private static final String DYNAMODB_ENDPOINT = "amazon.dynamodb.endpoint";
    private static final String AWS_ACCESSKEY = "amazon.aws.accesskey";
    private static final String AWS_SECRETKEY = "amazon.aws.secretkey";
    private static final String AMAZON_REGION = "amazon.region";

    @BeforeAll
    public static void setupClass() {
        Properties testProperties = loadFromFileInClasspath("test.properties")
                .filter(properties -> !isEmpty(properties.getProperty(AWS_ACCESSKEY)))
                .filter(properties -> !isEmpty(properties.getProperty(AWS_SECRETKEY)))
                .filter(properties -> !isEmpty(properties.getProperty(DYNAMODB_ENDPOINT)))
                .filter(properties -> !isEmpty(properties.getProperty(AMAZON_REGION)))
                .orElseThrow(() -> new RuntimeException("Unable to get all of the required test property values"));

        String amazonAWSAccessKey = testProperties.getProperty(AWS_ACCESSKEY);
        String amazonAWSSecretKey = testProperties.getProperty(AWS_SECRETKEY);
        String amazonDynamoDBEndpoint = testProperties.getProperty(DYNAMODB_ENDPOINT);
        String awsRegion = testProperties.getProperty(AMAZON_REGION);

        amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(amazonDynamoDBEndpoint, awsRegion))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(amazonAWSAccessKey, amazonAWSSecretKey)))
                .build();
        dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB);
    }

    @BeforeEach
    public void setup() {
        try {
            repository = new DynamoDBRepository();
            repository.setMapper(dynamoDBMapper);
            dynamoDBService = new DynamoDBService();
            dynamoDBService.setRepository(repository);

            CreateTableRequest tableRequest = dynamoDBMapper.generateCreateTableRequest(User.class);

            tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));

            amazonDynamoDB.createTable(tableRequest);
        } catch (ResourceInUseException e) {
            // Do nothing, table already created
        }

        dynamoDBMapper.batchDelete((List<User>) repository.findAll());
    }

    @Test
    public void testInsertUserAndGetUserById() {
        User expected = new User();
        expected.setUserId("7d44cdcf-5950-469d-b1df-81fc0467b31e");
        expected.setLastActiveTime("2020-04-09");
        expected.setSessionId("fa0755ce-5b3e-4c3b-932f-e8b362dc8cc3");
        dynamoDBService.insertUser(expected);

        User result = dynamoDBService.getUserById("7d44cdcf-5950-469d-b1df-81fc0467b31e");
        assertEquals(result.getUserId(), "7d44cdcf-5950-469d-b1df-81fc0467b31e");
        assertEquals(result.getLastActiveTime(), "2020-04-09");
        assertEquals(result.getSessionId(), "fa0755ce-5b3e-4c3b-932f-e8b362dc8cc3");
    }


    private static boolean isEmpty(String inputString) {
        return inputString == null || "".equals(inputString);
    }

    private static Optional<Properties> loadFromFileInClasspath(String fileName) {
        InputStream stream = null;
        try {
            Properties config = new Properties();
            Path configLocation = Paths.get(ClassLoader.getSystemResource(fileName).toURI());
            stream = Files.newInputStream(configLocation);
            config.load(stream);
            return Optional.of(config);
        } catch (Exception e) {
            return Optional.empty();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
