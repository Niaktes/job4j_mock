package ru.checkdev.notification.telegram.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;

import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.PersonDTO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testing TgAuthCallWebClient
 *
 * @author Sergei Zakharenko
 * @since 20.02.2024
 */
class TgAuthCallWebClientTest {

    private TgAuthCallWebClient tgAuthCallWebClient;

    private static MockWebServer mockServer;

    private Logger logger;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeAll
    static void setUp() throws Exception {
        mockServer = new MockWebServer();
        mockServer.start();
    }

    @BeforeEach
    void initialize() {
        String url = String.format("http://localhost:%s", mockServer.getPort());
        tgAuthCallWebClient = new TgAuthCallWebClient(url);

        logger = (Logger) LoggerFactory.getLogger(TgAuthCallWebClient.class);
        listAppender = new ListAppender<>();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void disableLogger() {
        logger.detachAndStopAllAppenders();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockServer.shutdown();
    }

    @Test
    void whenDoGetThenReturnObject() throws JsonProcessingException {
        var created = Calendar.getInstance();
        PersonDTO personDto = new PersonDTO("username", "mail", "password", true, new ArrayList<>(), created);
        ObjectMapper objectMapper = new ObjectMapper();
        mockServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(personDto))
                .addHeader("Content-Type", "application/json"));

        Mono<Object> objectMono = tgAuthCallWebClient.doGet("/person/");
        Map<String, Object> receivedObject = objectMapper.convertValue(objectMono.block(), Map.class);

        assertThat(receivedObject)
                .containsEntry("email", "mail")
                .containsEntry("password", "password")
                .containsEntry("created", created.getTimeInMillis());
    }

    @Test
    void whenDoGetThenReturnExceptionError() {
        listAppender.start();
        mockServer.enqueue(new MockResponse().setResponseCode(500));

        assertThatThrownBy(() -> tgAuthCallWebClient.doGet("/person/").block())
                .isInstanceOf(WebClientResponseException.class);
        assertTrue(listAppender.list.stream()
                .anyMatch(event -> event.toString().contains("API not found: 500 Internal Server Error"))
        );
    }

    @Test
    void whenDoPostSavePersonThenReturnNewPerson() throws JsonProcessingException {
        var created = Calendar.getInstance();
        PersonDTO personDto = new PersonDTO("username", "mail", "password", true, new ArrayList<>(), created);
        ObjectMapper objectMapper = new ObjectMapper();
        mockServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(personDto))
                .addHeader("Content-Type", "application/json"));

        Mono<Object> objectMono = tgAuthCallWebClient.doPost("/person/", personDto);
        Map<String, Object> receivedObject = objectMapper.convertValue(objectMono.block(), Map.class);

        assertThat(receivedObject)
                .containsEntry("email", "mail")
                .containsEntry("password", "password")
                .containsEntry("created", created.getTimeInMillis());
    }

    @Test
    void whenDoPostThenReturnExceptionError() {
        listAppender.start();
        var created = Calendar.getInstance();
        PersonDTO personDto = new PersonDTO("username", "mail", "password", true, null, created);
        mockServer.enqueue(new MockResponse().setResponseCode(500));

        assertThatThrownBy(() -> tgAuthCallWebClient.doPost("/person/", personDto).block())
                .isInstanceOf(WebClientResponseException.class);
        assertTrue(listAppender.list.stream()
                .anyMatch(event -> event.toString().contains("API not found: 500 Internal Server Error"))
        );
    }

    @Test
    void whenTokenThenReturnTokenObject() {
        Map<String, String> params = new HashMap<>();
        params.put("username", "username");
        params.put("password", "password");
        ObjectMapper objectMapper = new ObjectMapper();
        mockServer.enqueue(new MockResponse()
                .setBody("{"
                        + "\"access_token\": \"token\","
                        + "\"token_type\": \"bearer\""
                        +"}")
                .addHeader("Content-Type", "application/json"));

        Mono<Object> objectMono = tgAuthCallWebClient.token(params);
        Map<String, Object> receivedObject = objectMapper.convertValue(objectMono.block(), Map.class);

        assertThat(receivedObject)
                .containsEntry("access_token", "token")
                .containsEntry("token_type", "bearer");
    }

    @Test
    void whenTokenThenReturnExceptionError() {
        listAppender.start();
        Map<String, String> params = new HashMap<>();
        params.put("username", "username");
        params.put("password", "password");
        mockServer.enqueue(new MockResponse().setResponseCode(500));

        assertThatThrownBy(() -> tgAuthCallWebClient.token(params).block())
                .isInstanceOf(WebClientResponseException.class);
        assertTrue(listAppender.list.stream()
                .anyMatch(event -> event.toString().contains("API not found: 500 Internal Server Error"))
        );
    }

    @Test
    void whenDoGetWithTokenThenReturnObject() throws JsonProcessingException {
        var created = Calendar.getInstance();
        PersonDTO personDto = new PersonDTO("username", "mail", "password", true, new ArrayList<>(), created);
        ObjectMapper objectMapper = new ObjectMapper();
        mockServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(personDto))
                .addHeader("Content-Type", "application/json"));

        Mono<Object> objectMono = tgAuthCallWebClient.doGet("/person/", "token");
        Map<String, Object> receivedObject = objectMapper.convertValue(objectMono.block(), Map.class);

        assertThat(receivedObject)
                .containsEntry("email", "mail")
                .containsEntry("password", "password")
                .containsEntry("created", created.getTimeInMillis());
    }

    @Test
    void whenDoGetWithTokenThenReturnExceptionError() {
        listAppender.start();
        mockServer.enqueue(new MockResponse().setResponseCode(500));

        assertThatThrownBy(() -> tgAuthCallWebClient.doGet("/person/", "token").block())
                .isInstanceOf(WebClientResponseException.class);
        assertTrue(listAppender.list.stream()
                .anyMatch(event -> event.toString().contains("API not found: 500 Internal Server Error"))
        );
    }

}