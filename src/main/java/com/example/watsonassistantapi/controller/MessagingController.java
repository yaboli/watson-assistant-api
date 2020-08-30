package com.example.watsonassistantapi.controller;

import com.example.watsonassistantapi.model.MessageResponse;
import com.example.watsonassistantapi.model.MessageRequest;
import com.example.watsonassistantapi.service.MessagingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messaging")
public class MessagingController {

    @Autowired
    private MessagingService messagingService;

    @CrossOrigin(origins = "http://react-chatbot-app-bucket.s3-website-us-east-1.amazonaws.com")
    @PostMapping
    public ResponseEntity<MessageResponse> getResponse(@RequestBody MessageRequest messageRequest) {
        MessageResponse messageResponse = messagingService.processMessage(messageRequest);
        return new ResponseEntity<>(messageResponse, HttpStatus.OK);
    }
}
