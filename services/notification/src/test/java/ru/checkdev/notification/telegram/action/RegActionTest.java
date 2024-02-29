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
import org.telegram.telegrambots.meta.api.objects.User;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClient;
import ru.checkdev.notification.telegram.service.TgUserService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegActionTest {

    @Mock
    private TgUserService tgUserServiceMock;
    @Mock
    private TgAuthCallWebClient tgAuthCallWebClientMock;

    private final String siteUrlMock = "site.com";
    private RegAction action;
    private Message message;
    private Logger logger;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        action = new RegAction(tgUserServiceMock, tgAuthCallWebClientMock, siteUrlMock);
        message = new Message();
        message.setChat(new Chat(1L, "private"));
        message.setFrom(new User(1L, "username", false));

        logger = (Logger) LoggerFactory.getLogger(RegAction.class);
        listAppender = new ListAppender<>();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void disableLogger() {
        logger.detachAndStopAllAppenders();
    }

    @Test
    void whenHandleAndUserAlreadyExistsThenGetAlreadyExistsMessage() {
        when(tgUserServiceMock.checkUserExists(message.getChatId())).thenReturn(true);

        SendMessage actualAnswer = (SendMessage) action.handle(message);

        assertThat(actualAnswer.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(actualAnswer.getText())
                .contains("Вы уже зарегистрированы в системе.")
                .contains("Чтобы узнать регистрационные данные используйте /check");
    }

    @Test
    void whenHandleAndUserNotExistsThenGetEnterEmailMessage() {
        when(tgUserServiceMock.checkUserExists(message.getChatId())).thenReturn(false);

        SendMessage actualAnswer = (SendMessage) action.handle(message);

        assertThat(actualAnswer.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(actualAnswer.getText()).contains("Введите email для регистрации:");
    }

    @Test
    void whenCallbackAndMessageIsNotEmailThenGetIncorrectEmailMessage() {
        message.setText("not email");

        SendMessage actualAnswer = (SendMessage) action.callback(message);

        assertThat(actualAnswer.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(actualAnswer.getText())
                .contains("Email: " + message.getText() + " не корректный.")
                .contains("попробуйте снова.");
    }

    @Test
    void whenCallbackAndAuthServerNotAvailableThenExceptionLogAndServiceNotAvailableMessage() {
        listAppender.start();
        message.setText("email@mail.ru");
        when(tgAuthCallWebClientMock.doPost(any(String.class), any(PersonDTO.class)))
                .thenThrow(WebClientResponseException.class);

        SendMessage actualAnswer = (SendMessage) action.callback(message);

        assertTrue(listAppender.list.stream()
                .anyMatch(event -> event.toString().contains("WebClient doPost error: "))
        );
        assertTrue(listAppender.list.stream()
                .anyMatch(event -> event.getLevel().equals(Level.ERROR))
        );
        assertThat(actualAnswer.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(actualAnswer.getText()).contains("Сервис авторизации не доступен попробуйте позже");
    }

    @Test
    void whenCallbackAndServerResponseHaveErrorObjectThenGetErrorMessage() {
        message.setText("email@mail.ru");
        when(tgAuthCallWebClientMock.doPost(any(String.class), any(PersonDTO.class)))
                .thenReturn(Mono.just(new Object() {
                    public String getError() {
                        return "Error Info";
                    }
                }));

        SendMessage actualAnswer = (SendMessage) action.callback(message);

        assertThat(actualAnswer.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(actualAnswer.getText())
                .contains("Error Info")
                .contains("Если Вы владелец существующего аккаунта с указанной почтой,")
                .contains("воспользуйтесь командой /subscribe для привязки аккаунта.");
    }

    @Test
    void whenCallbackAndServerResponseHaveNoErrorThenGetSubscriptionCompletedMessage() {
        message.setText("email@mail.ru");
        when(tgAuthCallWebClientMock.doPost(any(String.class), any(PersonDTO.class)))
                .thenReturn(Mono.just(new Object() {
                    public Object getPerson() {
                        return new Object() {
                            public int getId() {
                                return 1;
                            }
                        };
                    }
                }));

        SendMessage actualAnswer = (SendMessage) action.callback(message);

        assertThat(actualAnswer.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(actualAnswer.getText())
                .contains("Вы зарегистрированы: ")
                .contains("Логин: email@mail.ru")
                .contains("Пароль: tg/")
                .contains("site.com");
    }


}