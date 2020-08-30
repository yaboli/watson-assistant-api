package com.example.watsonassistantapi.controller;

import com.example.watsonassistantapi.model.MessageResponse;
import com.example.watsonassistantapi.model.MessageRequest;
import com.example.watsonassistantapi.service.MessagingService;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(MessagingController.class)
class MessagingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessagingService messagingService;

    @Test
    @WithMockUser(username = "test_user", password = "123", roles = "USER")
    public void testGetResponse() throws Exception {

        // Mock the data returned by the messagingService class
        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setRecipientId("6612b357-1909-4d02-b628-2fd18253d32e");
        messageResponse.setText("hello!");
        when(messagingService.processMessage(any(MessageRequest.class)))
                .thenReturn(messageResponse);

        // Create a mock HTTP request to verify the expected result
        Gson gson = new Gson();
        String jsonBody = gson.toJson(new MessageRequest());
        mockMvc.perform(MockMvcRequestBuilders
                .post("/api/messaging")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.recipientId").value("6612b357-1909-4d02-b628-2fd18253d32e"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.text").value("hello!"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

}