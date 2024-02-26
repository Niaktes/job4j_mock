package ru.checkdev.notification.telegram.action;

import java.util.Optional;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.TgUser;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClient;
import ru.checkdev.notification.telegram.service.TgUserService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscribeActionTest {

    @Mock
    private TgUserService tgUserServiceMock;
    @Mock
    private TgAuthCallWebClient tgAuthCallWebClientMock;

    private SubscribeAction action;
    private Message message;
    private Logger logger;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        action = new SubscribeAction(tgUserServiceMock, tgAuthCallWebClientMock);
        message = new Message();
        message.setChat(new Chat(1L, "private"));

        logger = (Logger) LoggerFactory.getLogger(SubscribeAction.class);
        listAppender = new ListAppender<>();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void disableLogger() {
        logger.detachAndStopAllAppenders();
    }

    @Test
    void whenHandleAndUserNotExistsThenGetNotExistsMessage() {
        when(tgUserServiceMock.checkUserExists(message.getChatId())).thenReturn(false);

        SendMessage actualAnswer = (SendMessage) action.handle(message);

        assertThat(actualAnswer.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(actualAnswer.getText()).contains("Вы ещё не зарегистрированы");
    }

    @Test
    void whenHandleAndUserExistsThenGetEnterPasswordMessage() {
        when(tgUserServiceMock.checkUserExists(message.getChatId())).thenReturn(true);

        SendMessage actualAnswer = (SendMessage) action.handle(message);

        assertThat(actualAnswer.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(actualAnswer.getText()).contains("Введите пароль:");
    }

    @Test
    void whenCallbackAndAuthServerNotAvailableThenExceptionLogAndServiceNotAvailableMessage() {
        listAppender.start();
        message.setText("password");
        TgUser user = new TgUser(1, "username", "e@mail", message.getChatId(), false, 1);
        when(tgUserServiceMock.findByChatId(message.getChatId())).thenReturn(Optional.of(user));
        when(tgAuthCallWebClientMock.doGet(any(String.class)))
                .thenThrow(WebClientResponseException.class);

        SendMessage actualAnswer = (SendMessage) action.callback(message);

        assertTrue(listAppender.list.stream()
                .anyMatch(event -> event.toString().contains("WebClient doGet error: "))
        );
        assertTrue(listAppender.list.stream()
                .anyMatch(event -> event.getLevel().equals(Level.ERROR))
        );
        assertThat(actualAnswer.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(actualAnswer.getText()).contains("Сервис не доступен попробуйте позже");
    }

    @Test
    void whenCallbackAndServerResponseHaveErrorObjectThenGetErrorMessage() {
        TgUser user = new TgUser(1, "username", "e@mail", message.getChatId(), false, 1);
        when(tgUserServiceMock.findByChatId(message.getChatId())).thenReturn(Optional.of(user));
        when(tgAuthCallWebClientMock.doGet(any(String.class)))
                .thenReturn(Mono.just(new Object() {
                    public String getError() {
                        return "Error Info";
                    }
                }));

        SendMessage actualAnswer = (SendMessage) action.callback(message);

        assertThat(actualAnswer.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(actualAnswer.getText())
                .contains("Ошибка: Error Info")
                .contains("Попробуйте ещё раз: /subscribe");
    }

    @Test
    void whenCallbackAndServerResponseHaveNoErrorThenGetSubscriptionCompletedMessage() {
        TgUser user = new TgUser(1, "username", "e@mail", message.getChatId(), false, 1);
        when(tgUserServiceMock.findByChatId(message.getChatId())).thenReturn(Optional.of(user));
        when(tgAuthCallWebClientMock.doGet(any(String.class)))
                .thenReturn(Mono.just(new Object() {
                    public String getOk() {
                        return "ok";
                    }
                }));

        SendMessage actualAnswer = (SendMessage) action.callback(message);

        assertThat(actualAnswer.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(actualAnswer.getText())
                .contains("Подписка оформлена");
    }

}