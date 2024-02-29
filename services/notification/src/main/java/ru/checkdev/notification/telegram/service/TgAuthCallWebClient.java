package ru.checkdev.notification.telegram.service;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.PersonDTO;

/**
 * 3. Мидл
 * Класс реализует методы get и post для отправки сообщений через WebClient
 *
 * @author Dmitry Stepanov, user Dmitry
 * @since 12.09.2023
 */
@Service
@Slf4j
public class TgAuthCallWebClient {
    private static final String API_NOT_FOUND = "API not found: {}";
    private static final String TOKEN_PATH = "/oauth/token";
    private WebClient webClient;

    public TgAuthCallWebClient(@Value("${server.auth}") String urlAuth) {
        this.webClient = WebClient.create(urlAuth);
    }

    /**
     * Метод get
     *
     * @param url URL http
     * @return Mono<Object>
     */
    public Mono<Object> doGet(String url) {
        return webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(Object.class)
                .doOnError(err -> log.error(API_NOT_FOUND, err.getMessage()));
    }

    /**
     * Метод POST
     *
     * @param url       URL http
     * @param personDTO Body PersonDTO.class
     * @return Mono<Object>
     */
    public Mono<Object> doPost(String url, PersonDTO personDTO) {
        return webClient
                .post()
                .uri(url)
                .bodyValue(personDTO)
                .retrieve()
                .bodyToMono(Object.class)
                .doOnError(err -> log.error(API_NOT_FOUND, err.getMessage()));
    }

    public Mono<Object> token(Map<String, String> params) {
        var map = new LinkedMultiValueMap<String, String>();
        params.forEach(map::add);
        map.add("scope", "any");
        map.add("grant_type", "password");
        return webClient
                .post()
                .uri(TOKEN_PATH)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("Authorization", "Basic am9iNGo6cGFzc3dvcmQ=")
                .bodyValue(map)
                .retrieve()
                .bodyToMono(Object.class)
                .doOnError(err -> log.error(API_NOT_FOUND, err.getMessage()));
    }

    public Mono<Object> doGet(String url, String token) {
        return webClient
                .get()
                .uri(url)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(Object.class)
                .doOnError(err -> log.error(API_NOT_FOUND, err.getMessage()));
    }

    public void setWebClient(WebClient webClient) {
        this.webClient = webClient;
    }
}
