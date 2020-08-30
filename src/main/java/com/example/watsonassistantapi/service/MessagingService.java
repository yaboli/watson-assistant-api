package com.example.watsonassistantapi.service;

import com.example.watsonassistantapi.config.WatsonAssistantConfig;
import com.example.watsonassistantapi.model.MessageResponse;
import com.example.watsonassistantapi.model.MessageRequest;
import com.example.watsonassistantapi.model.User;
import com.example.watsonassistantapi.repository.DynamoDBRepository;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.assistant.v2.Assistant;
import com.ibm.watson.assistant.v2.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;

@Service
public class MessagingService {

    @Autowired
    private DynamoDBRepository repository;

    @Autowired
    private WatsonAssistantConfig watsonAssistantConfig;

    public MessageResponse processMessage(MessageRequest messageRequest) {

        String userId = messageRequest.getRecipientId();
        User user = repository.getOneUserDetails(userId);

        IamAuthenticator authenticator = new IamAuthenticator(watsonAssistantConfig.getApiKey());
        Assistant assistant = new Assistant(watsonAssistantConfig.getVersion(), authenticator);
        assistant.setServiceUrl(watsonAssistantConfig.getUrl());

        if (user != null) {
            String lastActiveTime = user.getLastActiveTime();
            Timestamp ts1 = Timestamp.valueOf(lastActiveTime); // last active time

            Date date = new Date();
            long time = date.getTime();
            Timestamp ts2 = new Timestamp(time); // current timestamp

            long milliseconds = ts2.getTime() - ts1.getTime();
            int seconds = (int) milliseconds / 1000;
            int minutes = (seconds % 3600) / 60;

            String sessionId = "";
            // Session time out is 5 min; if 5 minutes have passed, nee to create a new session
            if (minutes >= 5) {
                // Retrieve new session id
                CreateSessionOptions options = new CreateSessionOptions.Builder(watsonAssistantConfig.getAssistantId()).build();
                SessionResponse response = assistant.createSession(options).execute().getResult();
                sessionId = response.getSessionId();
            } else {
                // Retrieve existing session id
                sessionId = user.getSessionId();
            }

            // Update session id
            user.setSessionId(sessionId);
            // Update last active time
            user.setLastActiveTime(ts2.toString());
            repository.updateUserDetails(user);

        } else {

            user = new User();
            user.setUserId(userId);

            Date date = new Date();
            long time = date.getTime();
            Timestamp ts = new Timestamp(time); // current timestamp
            user.setLastActiveTime(ts.toString());

            // Retrieve new session id
            CreateSessionOptions options = new CreateSessionOptions.Builder(watsonAssistantConfig.getAssistantId()).build();
            SessionResponse response = assistant.createSession(options).execute().getResult();
            String sessionId = response.getSessionId();;
            user.setSessionId(sessionId);

            repository.insertIntoDynamoDB(user);
        }

        String text = messageRequest.getText();
        MessageInput input = new MessageInput.Builder().
                messageType("text").text(text).build();
        MessageOptions options = new MessageOptions.
                Builder(watsonAssistantConfig.getAssistantId(), user.getSessionId()).
                input(input).build();
        com.ibm.watson.assistant.v2.model.MessageResponse response = assistant.message(options).execute().getResult();
        String responseText = response.getOutput().getGeneric().get(0).text();

        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setRecipientId(messageRequest.getRecipientId());
        messageResponse.setText(responseText);
        return messageResponse;
    }

    public void setWatsonAssistantConfig(WatsonAssistantConfig config) {
        this.watsonAssistantConfig = config;
    }

    public void setRepository(DynamoDBRepository repository) {
        this.repository = repository;
    }
}
