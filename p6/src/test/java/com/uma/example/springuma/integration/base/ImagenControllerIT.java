package com.uma.example.springuma.integration.base;

import java.io.File;
import java.nio.file.Files;
import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.hasSize;

import com.uma.example.springuma.model.Medico;
import com.uma.example.springuma.model.Paciente;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;

public class ImagenControllerIT extends AbstractIntegration {

    @LocalServerPort
    private Integer port;

    private WebTestClient client;

    private Paciente paciente;
    private Medico medico;

    @PostConstruct
    public void setUp() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:"+port)
        .responseTimeout(Duration.ofMillis(30000)).build();

        medico = new Medico("1", "Grenheir", "Radiólogo");
        medico.setId(1);

        paciente = new Paciente("Pedro", 32, "Cita cardiologia", "12345678P", medico);
        paciente.setId(1);

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
    }

    @Test
    @DisplayName("Test to verify if the imagen is uploaded")
    public void test_UploadImage_ReturnsString() throws Exception {
        //ARRANGE
        // Leer el archivo de imagen
        File uploadFile = new File("./src/test/resources/healthy.png");

        // Construir el cuerpo de la solicitud multipart
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("image", new FileSystemResource(uploadFile));
        builder.part("paciente", paciente);

        //ACT
        // Enviar el archivo usando WebTestClient
        FluxExchangeResult<String> responseBody = client.post()
        .uri("/imagen")
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(BodyInserters.fromMultipartData(builder.build()))
        .exchange()
        .expectStatus().is2xxSuccessful().returnResult(String.class);

        String result = responseBody.getResponseBody().blockFirst();

        //ASSERT
        // Verificar que la respuesta sea la esperada
        assertEquals("{\"response\" : \"file uploaded successfully : healthy.png\"}", result);
    }

    @Test
    @DisplayName("Test to verify if the imagen is uploaded")
    public void test_DownloadImage_ReturnsString() throws Exception {
        //ARRANGE
        // Leer el archivo de imagen
        File uploadFile = new File("./src/test/resources/healthy.png");

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

        byte[] fileContent = Files.readAllBytes(uploadFile.toPath());

        //ACT-ASSERT
        // obtiene la imagen con ID 1
        client.get().uri("/imagen/1")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.IMAGE_PNG)
            .expectBody(byte[].class)
            .consumeWith(response -> {
                byte[] arrayBytesObtained = response.getResponseBody();
                assertArrayEquals(fileContent, arrayBytesObtained);
            });
    }

    @Test
    @DisplayName("Test to verify if the imagen is returned")
    public void test_getImage_ReturnsImage() throws Exception {
        //ARRANGE
        // Leer el archivo de imagen
        File uploadFile = new File("./src/test/resources/healthy.png");

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

        //ACT-ASSERT
        client.get().uri("/imagen/info/1")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.id").isEqualTo(1)
        .jsonPath("$.nombre").isEqualTo("healthy.png")
        .jsonPath("$.paciente.id").isEqualTo(1);
    }

    @Test
    @DisplayName("Test to verify if the imagen is returned")
    public void test_getImagenPrediction_ReturnsImage() throws Exception {
        //ARRANGE
        // Leer el archivo de imagen
        File uploadFile = new File("./src/test/resources/healthy.png");

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

        String expectedPrediction = "Not cancer (label 0),  score: 0.984481368213892";  // Replace with the actual expected prediction

        //ACT-ASSERT
        client.get().uri("/imagen/predict/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.prediction").isEqualTo(expectedPrediction);
    }

    @Test
    @DisplayName("Test to verify if the imagen is returned")
    public void test_getImagenes_ReturnsListOfImagen() throws Exception {
        //ARRANGE
        // Leer el archivo de imagen
        File uploadFile = new File("./src/test/resources/healthy.png");

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

        //ACT-ASSERT
        client.get().uri("/imagen/paciente/1")
            .accept(MediaType.APPLICATION_JSON)
            .exchange() // hace la peticion
            .expectStatus().isOk() // comprueba que el codigo es OK
            .expectHeader().valueEquals("Content-Type", "application/json") // comprueba que el content type es json
            .expectBody().jsonPath("$", hasSize(1)); // comprueba que la respuesta tenga un array con tamanyo 0
    }

    @Test
    @DisplayName("Test to verify if the imagen is deleted")
    public void test_deleteImagen_Success() throws Exception {
        //ARRANGE
        // Leer el archivo de imagen
        File uploadFile = new File("./src/test/resources/healthy.png");

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

        //ACT-ASSERT
        client.delete().uri("/imagen/1")
        .exchange()
        .expectStatus().isNoContent();
    }

}
