package ru.job4j.site.handler;

import java.io.IOException;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import ru.job4j.site.exception.*;

@Component
@Slf4j
public class RestTemplateResponseErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return (response.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR
                || response.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR);
    }

    @Override
    public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
        if (method.matches(HttpMethod.GET.name()) && response.getStatusCode().value() != 401) {
            log.error("Call: " + url.toString(), response.getStatusText());
        }
        this.handleError(response);
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        if (response.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR) {
            throw new AppException("Server error. Check the logs.");
        }
        if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
            throw new BadRequestException(
                    "Request parameters incorrect."
                            + System.lineSeparator()
                            + response.getBody());
        }
        if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
            throw new ForbiddenException("Attempted execution by incorrect role user.");
        }
        if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            throw new NotFoundException("Content with specified id not found.");
        }
    }

}