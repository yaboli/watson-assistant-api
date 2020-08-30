package com.example.watsonassistantapi.controller;

import com.example.watsonassistantapi.model.User;
import com.example.watsonassistantapi.service.DynamoDBService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(DynamoDBController.class)
class DynamoDBControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DynamoDBService dynamoDBService;

    @Test
    @WithMockUser(username = "admin", password = "123", roles = "ADMIN")
    public void testGetOneUserDetails() throws Exception {

        // Mock the data returned by the DynamoDBService class
        User user = new User();
        user.setUserId("6612b357-1909-4d02-b628-2fd18253d32e");
        user.setLastActiveTime("2020-04-09");
        user.setSessionId("cbdb342b-b49e-4bea-a6ea-dd941041b570");
        when(dynamoDBService.getUserById(anyString()))
                .thenReturn(user);

        // Create a mock HTTP request to verify the expected result
        mockMvc.perform(MockMvcRequestBuilders
                .get("/dynamoDB?userId=some_random_string"))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value("6612b357-1909-4d02-b628-2fd18253d32e"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastActiveTime").value("2020-04-09"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sessionId").value("cbdb342b-b49e-4bea-a6ea-dd941041b570"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

}