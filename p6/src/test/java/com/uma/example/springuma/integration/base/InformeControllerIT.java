package com.uma.example.springuma.integration.base;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.io.File;
import java.time.Duration;

import com.uma.example.springuma.model.Imagen;
import com.uma.example.springuma.model.Informe;
import com.uma.example.springuma.model.Medico;
import com.uma.example.springuma.model.Paciente;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class InformeControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    @LocalServerPort
    private Integer port;

    private WebTestClient client;

    private Informe informe;
    private Imagen imagen;
    private Medico medico;
    private Paciente paciente;

    @PostConstruct
    public void init() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:"+port)
                .responseTimeout(Duration.ofMillis(30000)).build();


                medico = new Medico("1", "Grenheir", "Radiólogo");
                medico.setId(1);
        
                paciente = new Paciente("Pedro", 32, "Cita cardiologia", "12345678P", medico);
                paciente.setId(1);

                File uploadFile = new File("./src/test/resources/no_healthty.png");

                imagen = new Imagen();
                imagen.setId(1);
                imagen.setNombre("no_healthty");
                imagen.setPaciente(paciente);

                informe = new Informe("Cancer", "Foto del cancer", imagen);
                informe.setId(1);
        
                // Crear un medico
                client.post().uri("/medico")
                .body(Mono.just(medico), Medico.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult();
        
                // Crear un paciente
                client.post().uri("/paciente")
                .body(Mono.just(paciente), Paciente.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult();

                // Construir el cuerpo de la solicitud multipart
                MultipartBodyBuilder builder = new MultipartBodyBuilder();
                builder.part("image", new FileSystemResource(uploadFile));
                builder.part("paciente", paciente);

                // Enviar el archivo usando WebTestClient
                client.post().uri("/imagen")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody().returnResult();
    }

    @Test
    @DisplayName("Test to verify if the informe is saved")
    void Test_SaveInforme_ReturnCreated() {

        webTestClient.post()
                .uri("/informe")
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromValue(informe))
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    @DisplayName("Test to verify if the informe is returned")
    public void test_getInforme_ReturnsInforme() throws Exception {
        webTestClient.post().uri("/informe")
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromValue(informe))
                .exchange()
                .expectStatus().isCreated();

        client.get().uri("/informe/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.imagen.nombre").isEqualTo("no_healthty.png")
                .jsonPath("$.prediccion").isEqualTo("Cancer (label 1), score: 0.6412607431411743")
                .jsonPath("$.contenido").isEqualTo("Foto del cancer");
    }

    @Test
    @DisplayName("Test to verify if the informes are returned")
    public void test_getInformes_ReturnsListOfInforme() throws Exception {

        webTestClient.post().uri("/informe")
                .contentType(APPLICATION_JSON)
                .body(BodyInserters.fromValue(informe))
                .exchange()
                .expectStatus().isCreated();

        client.get().uri("/informe/imagen//1")
            .accept(MediaType.APPLICATION_JSON)
            .exchange() // hace la peticion
            .expectStatus().isOk() // comprueba que el codigo es OK
            .expectHeader().valueEquals("Content-Type", "application/json") // comprueba que el content type es json
            .expectBody().jsonPath("$", hasSize(1)); // comprueba que la respuesta tenga un array con tamanyo 0
    }


    @Test
    @DisplayName("Test to verify the informe is deleted")
    void deleteInforme() {

        webTestClient.delete()
                .uri("/informe/1")
                .exchange()
                .expectStatus().isNoContent();
    }
}
