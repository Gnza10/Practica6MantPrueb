package com.uma.example.springuma.integration.base;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.uma.example.springuma.model.Medico;
import com.uma.example.springuma.model.Paciente;

public class PacienteControllerIT extends AbstractIntegration{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Paciente paciente;
    private Medico medico;

    @BeforeEach
    public void setUp() {
        medico = new Medico("1", "Grenheir", "Radiólogo");
        medico.setId(1);
        paciente = new Paciente("Pedro", 32, "Cita cardiologia", "12345678P", medico);
        paciente.setId(2);
    }

    @Test
    @DisplayName("Test to verify if the patient is found")
    public void test_GetPaciente_ReturnsPaciente() throws Exception {
        // Arrange
        this.mockMvc.perform(post("/paciente")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(paciente)))
                .andExpect(status().isCreated())
                .andExpect(status().is2xxSuccessful());
        
        // Act
        this.mockMvc.perform(get("/paciente/2"))
            .andDo(print())
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.nombre").value(paciente.getNombre()));
    }

    @Test
    @DisplayName("Test to verify if the patient is saved")
    public void test_SavePaciente_ReturnsDNI() throws Exception {
        // Act
        this.mockMvc.perform(post("/paciente")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(paciente)))
                .andExpect(status().isCreated())
                .andExpect(status().is2xxSuccessful());
        
        // Assert
        this.mockMvc.perform(get("/paciente/1"))
         .andDo(print())
         .andExpect(status().is2xxSuccessful())
         .andExpect(content().contentType("application/json"))
         .andExpect(jsonPath("$.dni").value(paciente.getDni()));
    }

    @Test
    @DisplayName("Test to verify if the patient is updated")
    public void testUpdatePaciente_returnsUpdatedPaciente() throws Exception {
      //Arrange
        this.mockMvc.perform(post("/paciente")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(paciente)))
                .andExpect(status().isCreated())
                .andExpect(status().is2xxSuccessful());
        
        this.mockMvc.perform(get("/paciente/1"))
            .andDo(print())
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.dni").value(paciente.getDni()));
        
        //Act
        paciente.setEdad(33);
        this.mockMvc.perform(put("/paciente")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(paciente)))
                .andExpect(status().isNoContent())
                .andExpect(status().is2xxSuccessful());
        
        //Assert
        this.mockMvc.perform(get("/paciente/1"))
         .andDo(print())
         .andExpect(status().is2xxSuccessful())
         .andExpect(content().contentType("application/json"))
         .andExpect(jsonPath("$.edad").value(paciente.getEdad()));
    }

    @Test
    @DisplayName("Test to verify if the patient is deleted")
    public void testDeletePaciente_Returns2xx() throws Exception {
      //Arrange
        this.mockMvc.perform(post("/paciente")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(paciente)))
                .andExpect(status().isCreated())
                .andExpect(status().is2xxSuccessful());
        
        this.mockMvc.perform(get("/paciente/1"))
            .andDo(print())
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.dni").value(paciente.getDni()));
        
        //Act
        this.mockMvc.perform(get("/paciente/1"))
            .andDo(print())
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.dni").value(paciente.getDni()));
    }

    @Test
    @DisplayName("Test to see verify the patients of a doctor")
    public void testGetPacientesMedico_ReturnsListOfPacientes() throws Exception {
        // Arrange
        this.mockMvc.perform(post("/paciente")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(paciente)))
                .andExpect(status().isCreated())
                .andExpect(status().is2xxSuccessful());
        
        // Act
        this.mockMvc.perform(get("/paciente/medico/1"))
            .andDo(print())
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$[0].nombre").value(paciente.getNombre()));
    }
    
}
