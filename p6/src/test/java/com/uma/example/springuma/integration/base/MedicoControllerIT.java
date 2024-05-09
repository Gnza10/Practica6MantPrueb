package com.uma.example.springuma.integration.base;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import org.junit.jupiter.api.BeforeEach;
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
    private Medico medico;
    @BeforeEach
    public void setUp() {
        medico = new Medico("1", "Grenheir", "Radiólogo");
        medico.setId(1);
    }

  // Verify the results
    @Test
    @DisplayName("Test to verify if the medico is saved")
    public void test_SaveMedico_ReturnsDNI() throws Exception {
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

    @Test
    @DisplayName("Test to verify if the medico is updated")
    public void testUpdateMedico() throws Exception {
      //Arrange
        this.mockMvc.perform(post("/medico")
        .contentType("application/json")
        .content(objectMapper.writeValueAsString(medico)))
        .andExpect(status().isCreated())
        .andExpect(status().is2xxSuccessful());

        this.mockMvc.perform(get("/medico/1"))
         .andDo(print())
         .andExpect(status().is2xxSuccessful())
         .andExpect(content().contentType("application/json"))
         .andExpect(jsonPath("$.dni").value(medico.getDni()));

        //Act
        medico.setDni("2");
        this.mockMvc.perform(put("/medico")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(medico)))
                .andExpect(status().is2xxSuccessful());
        
        // Assert
        this.mockMvc.perform(get("/medico/1"))
         .andDo(print())
         .andExpect(status().is2xxSuccessful())
         .andExpect(content().contentType("application/json"))
         .andExpect(jsonPath("$.dni").value(medico.getDni()));
    }
    
}
