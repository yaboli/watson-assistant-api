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
import com.example.watsonassistantapi.config.WatsonAssistantConfig;
import com.example.watsonassistantapi.model.MessageResponse;
import com.example.watsonassistantapi.model.User;
import com.example.watsonassistantapi.model.MessageRequest;
import com.example.watsonassistantapi.repository.DynamoDBRepository;
import com.example.watsonassistantapi.service.MessagingService;
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

public class MessagingControllerIntegrationTest {

    private static DynamoDBMapper dynamoDBMapper;
    private static AmazonDynamoDB amazonDynamoDB;

    private DynamoDBRepository repository;
    private static MessagingService messagingService;

    private static final String DYNAMODB_ENDPOINT = "amazon.dynamodb.endpoint";
    private static final String AWS_ACCESSKEY = "amazon.aws.accesskey";
    private static final String AWS_SECRETKEY = "amazon.aws.secretkey";
    private static final String AMAZON_REGION = "amazon.region";

    private static final String WATSON_API_KEY = "watson.api-key";
    private static final String WATSON_VERSION = "watson.version";
    private static final String WATSON_URL = "watson.url";
    private static final String WATSON_ASSISTANT_ID = "watson.assistant-id";


    @BeforeAll
    public static void setupClass() {
        Properties testProperties = loadFromFileInClasspath("test.properties")
                .filter(properties -> !isEmpty(properties.getProperty(AWS_ACCESSKEY)))
                .filter(properties -> !isEmpty(properties.getProperty(AWS_SECRETKEY)))
                .filter(properties -> !isEmpty(properties.getProperty(DYNAMODB_ENDPOINT)))
                .filter(properties -> !isEmpty(properties.getProperty(AMAZON_REGION)))
                .filter(properties -> !isEmpty(properties.getProperty(WATSON_API_KEY)))
                .filter(properties -> !isEmpty(properties.getProperty(WATSON_VERSION)))
                .filter(properties -> !isEmpty(properties.getProperty(WATSON_URL)))
                .filter(properties -> !isEmpty(properties.getProperty(WATSON_ASSISTANT_ID)))
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

        String watsonApiKey = testProperties.getProperty(WATSON_API_KEY);
        String watsonVersion = testProperties.getProperty(WATSON_VERSION);
        String watsonUrl = testProperties.getProperty(WATSON_URL);
        String watsonAssistantId = testProperties.getProperty(WATSON_ASSISTANT_ID);

        WatsonAssistantConfig watsonAssistantConfig = new WatsonAssistantConfig();
        watsonAssistantConfig.setApiKey(watsonApiKey);
        watsonAssistantConfig.setAssistantId(watsonAssistantId);
        watsonAssistantConfig.setUrl(watsonUrl);
        watsonAssistantConfig.setVersion(watsonVersion);

        messagingService = new MessagingService();
        messagingService.setWatsonAssistantConfig(watsonAssistantConfig);
    }

    @BeforeEach
    public void setup() {
        try {
            repository = new DynamoDBRepository();
            repository.setMapper(dynamoDBMapper);
            messagingService.setRepository(repository);

            CreateTableRequest tableRequest = dynamoDBMapper.generateCreateTableRequest(User.class);

            tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));

            amazonDynamoDB.createTable(tableRequest);
        } catch (ResourceInUseException e) {
            // Do nothing, table already created
        }

        dynamoDBMapper.batchDelete((List<User>) repository.findAll());
    }

    @Test
    public void testGetResponse() {

        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setRecipientId("6612b357-1909-4d02-b628-2fd18253d32e");
        messageRequest.setText("hello");

        MessageResponse expected = new MessageResponse();
        expected.setRecipientId("6612b357-1909-4d02-b628-2fd18253d32e");
        expected.setText("Welcome, good to see you");

        MessageResponse result = messagingService.processMessage(messageRequest);
        assertEquals(result.getRecipientId(), expected.getRecipientId());
        assertEquals(result.getText(), expected.getText());
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
