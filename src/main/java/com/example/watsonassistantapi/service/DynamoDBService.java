package com.example.watsonassistantapi.service;

import com.example.watsonassistantapi.model.User;
import com.example.watsonassistantapi.repository.DynamoDBRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DynamoDBService {

    @Autowired
    private DynamoDBRepository repository;

    public void insertUser(User user) {
        repository.insertIntoDynamoDB(user);
    }

    public User getUserById(String userId) {
        return repository.getOneUserDetails(userId);
    }

    public void updateUserInfo(User user) {
        repository.updateUserDetails(user);
    }

    public void deleteUser(User user) {
        repository.deleteUserDetails(user);
    }

    public void setRepository(DynamoDBRepository repository) {
        this.repository = repository;
    }
}
