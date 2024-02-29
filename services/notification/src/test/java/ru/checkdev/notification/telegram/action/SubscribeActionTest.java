package ru.checkdev.notification.telegram.action;

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
import ru.checkdev.notification.telegram.service.TgAuthCallWebClient;
import ru.checkdev.notification.telegram.service.TgUserService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
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
        when(tgUserServiceMock.checkUserExists(message.getChatId())).thenReturn(true);

        SendMessage actualAnswer = (SendMessage) action.handle(message);

        assertThat(actualAnswer.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(actualAnswer.getText())
                .contains("Ваш аккаунт телеграм уже привязан к аккаунту сайта.")
                .contains("Чтобы узнать регистрационные данные используйте /check")
                .contains("Чтобы отвязать аккаунт используйте /unsubscribe");
    }

    @Test
    void whenHandleAndUserExistsThenGetEnterPasswordMessage() {
        when(tgUserServiceMock.checkUserExists(message.getChatId())).thenReturn(false);

        SendMessage actualAnswer = (SendMessage) action.handle(message);

        assertThat(actualAnswer.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(actualAnswer.getText())
                .contains("Для привязки аккаунта, введите, пожалуйста, email и пароль через пробел.")
                .contains("Пример: \"example@mail.ru password\" (без кавычек)");
    }

    @Test
    void whenCallbackAndMessageNotCoupleEmailPasswordThenGetIncorrectEmailMessage() {
        message.setText("notPair");

        SendMessage actualAnswer = (SendMessage) action.callback(message);

        assertThat(actualAnswer.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(actualAnswer.getText())
                .contains("Пожалуйста, введите пару email и пароль как указано в примере выше.");
    }

    @Test
    void whenCallbackAndEmailIsWrongThenGetIncorrectEmailMessage() {
        message.setText("notEmail password");

        SendMessage actualAnswer = (SendMessage) action.callback(message);

        assertThat(actualAnswer.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(actualAnswer.getText())
                .contains("Email: notEmail не корректный.")
                .contains("попробуйте снова");
    }

    @Test
    void whenCallbackAndTokenServerNotAvailableThenExceptionLogAndServiceNotAvailableMessage() {
        listAppender.start();
        message.setText("email@mail.ru password");
        when(tgAuthCallWebClientMock.token(anyMap()))
                .thenThrow(WebClientResponseException.class);

        SendMessage actualAnswer = (SendMessage) action.callback(message);

        assertTrue(listAppender.list.stream()
                .anyMatch(event -> event.toString().contains("WebClient token error: "))
        );
        assertTrue(listAppender.list.stream()
                .anyMatch(event -> event.getLevel().equals(Level.ERROR))
        );
        assertThat(actualAnswer.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(actualAnswer.getText()).contains("Сервис авторизации не доступен, попробуйте позже.");
    }

    @Test
    void whenCallbackAndTokenResponseHaveErrorObjectThenGetErrorMessage() {
        message.setText("email@mail.ru password");
        when(tgAuthCallWebClientMock.token(anyMap()))
                .thenReturn(Mono.just(new Object() {
                    public String getError() {
                        return "Error Info";
                    }
                }));

        SendMessage actualAnswer = (SendMessage) action.callback(message);

        assertThat(actualAnswer.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(actualAnswer.getText())
                .contains("Ошибка привязки аккаунтов:")
                .contains("Введенные email или пароль не верны.");
    }

    @Test
    void whenCallbackAndProfileServerNotAvailableThenExceptionLogAndServiceNotAvailableMessage() {
        listAppender.start();
        message.setText("email@mail.ru password");
        when(tgAuthCallWebClientMock.token(anyMap()))
                .thenReturn(Mono.just(new Object() {
                    public String getOk() {
                        return "ok";
                    }
                }));
        when(tgAuthCallWebClientMock.doGet(any(String.class), any()))
                .thenThrow(WebClientResponseException.class);

        SendMessage actualAnswer = (SendMessage) action.callback(message);

        assertTrue(listAppender.list.stream()
                .anyMatch(event -> event.toString().contains("WebClient doGet error: "))
        );
        assertTrue(listAppender.list.stream()
                .anyMatch(event -> event.getLevel().equals(Level.ERROR))
        );
        assertThat(actualAnswer.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(actualAnswer.getText()).contains("Сервис данных о профиле не доступен, попробуйте позже.");
    }

    @Test
    void whenCallbackAndServerResponseHaveNoErrorsThenGetSubscriptionCompletedMessage() {
        message.setText("email@mail.ru password");
        when(tgAuthCallWebClientMock.token(anyMap()))
                .thenReturn(Mono.just(new Object() {
                    public String getOk() {
                        return "ok";
                    }
                }));
        when(tgAuthCallWebClientMock.doGet(any(String.class), any()))
                .thenReturn(Mono.just(new Object() {
                    public int getId() {
                        return 1;
                    }
                    public String getUsername() {
                        return "username";
                    }
                }));

        SendMessage actualAnswer = (SendMessage) action.callback(message);

        assertThat(actualAnswer.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(actualAnswer.getText())
                .contains("Аккаунт с почтой email@mail.ru успешно привязан.");
    }

}