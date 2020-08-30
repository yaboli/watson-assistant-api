package com.example.watsonassistantapi.controller;

import com.example.watsonassistantapi.model.User;
import com.example.watsonassistantapi.service.DynamoDBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dynamoDB")
public class DynamoDBController {

    @Autowired
    private DynamoDBService dynamoDBService;

    @PostMapping
    public String insertIntoDynamoDB(@RequestBody User user) {
        String userId = user.getUserId();
        user.setUserId(userId);
        dynamoDBService.insertUser(user);
        return "Successfully inserted into DynamoDB table";
    }

    @GetMapping
    public ResponseEntity<User> getOneUserDetails(@RequestParam String userId) {
        User user = dynamoDBService.getUserById(userId);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PutMapping
    public void updateUserDetails(@RequestBody User user) {
        dynamoDBService.updateUserInfo(user);
    }

    @DeleteMapping(value = "{userId}")
    public void deleteUserDetails(@PathVariable("userId") String userId) {
        User user = new User();
        user.setUserId(userId);
        dynamoDBService.deleteUser(user);
    }
}
