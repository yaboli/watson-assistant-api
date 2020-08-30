package com.example.watsonassistantapi.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.example.watsonassistantapi.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class DynamoDBRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDBRepository.class);

    @Autowired
    private DynamoDBMapper mapper;

    public void insertIntoDynamoDB(User user) {
        mapper.save(user);
    }

    public User getOneUserDetails(String userId) {
        return mapper.load(User.class, userId);
    }

    public void updateUserDetails(User user) {
        try {
            mapper.save(user, buildDynamoDBSaveExpression(user));
        } catch (ConditionalCheckFailedException exception) {
            LOGGER.error("invalid data - " + exception.getMessage());
        }
    }

    public void deleteUserDetails(User user) {
        mapper.delete(user);
    }

    /**
     * <strong>WARNING:</strong> It is not recommended to perform full table scan
     * targeting the real production environment.
     *
     * @return All items
     */
    public List<User> findAll() {
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        return mapper.scan(User.class, scanExpression);
    }

    public void setMapper(DynamoDBMapper dynamoDBMapper) {
        this.mapper = dynamoDBMapper;
    }

    public DynamoDBSaveExpression buildDynamoDBSaveExpression(User user) {
        DynamoDBSaveExpression saveExpression = new DynamoDBSaveExpression();
        Map<String, ExpectedAttributeValue> expected = new HashMap<>();
        expected.put("userId", new ExpectedAttributeValue(new AttributeValue(user.getUserId()))
                .withComparisonOperator(ComparisonOperator.EQ));
        saveExpression.setExpected(expected);
        return saveExpression;
    }

}
