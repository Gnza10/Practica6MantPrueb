package com.uma.example.springuma.integration.base;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uma.example.springuma.model.Medico;

public class MedicoControllerIT extends AbstractIntegration{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

  // Verify the results
    @Test
    @DisplayName("Test to verify if the medico is saved")
    public void testSaveMedico() throws Exception {
        // Arrange
        Medico medico = new Medico("1", "Grenheir", "Radiólogo");

        // Act
        this.mockMvc.perform(post("/medico")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(medico)))
                .andExpect(status().isCreated())
                .andExpect(status().is2xxSuccessful());
        
        // Assert
        this.mockMvc.perform(get("/medico/1"))
         .andDo(print())
         .andExpect(status().is2xxSuccessful())
         .andExpect(content().contentType("application/json"))
         .andExpect(jsonPath("$.dni").value(medico.getDni()));
    }
}
