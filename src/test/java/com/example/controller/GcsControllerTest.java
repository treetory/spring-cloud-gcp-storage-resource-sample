package com.example.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GcsControllerTest {

    private static final Logger LOG = LoggerFactory.getLogger("treetory");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    WebApplicationContext webApplicationContext;

    @BeforeEach()
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(new CharacterEncodingFilter("UTF-8", true)).alwaysDo(MockMvcResultHandlers.print()).build();
    }

    @Test
    void getSignedURLTest()  throws Exception {
        MvcResult getResult = mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/gcs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("fileName", "ANALYSIS_FILES/1357_HE18-035-0758028-LHT_final.bam.tdf")
                )
                .andExpect(MockMvcResultMatchers.status().isTemporaryRedirect())
                .andReturn();

        LOG.info("SIGNED URL >>> {}", getResult.getRequest().getHeader("Location"));

        assertNotNull(getResult);
    }

}
