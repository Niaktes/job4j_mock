package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.telegram.service.TgUserService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnsubscribeActionTest {

    @Mock
    private TgUserService tgUserServiceMock;

    private Message message;
    private UnsubscribeAction action;

    @BeforeEach
    void setUp() {
        action = new UnsubscribeAction(tgUserServiceMock);
        message = new Message();
        message.setChat(new Chat(1L, "private"));
    }

    @Test
    void whenUserNotExistsThenGetNotExistsMessage() {
        when(tgUserServiceMock.checkUserExists(message.getChatId())).thenReturn(false);

        SendMessage actualAnswer = (SendMessage) action.handle(message);

        assertThat(actualAnswer.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(actualAnswer.getText()).contains("Вы ещё не зарегистрированы");
    }

    @Test
    void whenUserExistsThenGetUnsubscribeMessage() {
        when(tgUserServiceMock.checkUserExists(message.getChatId())).thenReturn(true);
        when(tgUserServiceMock.deleteByChatId(any(Long.class))).thenReturn(true);

        SendMessage actualAnswer = (SendMessage) action.handle(message);

        assertThat(actualAnswer.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(actualAnswer.getText()).contains("Аккаунт телеграм успешно отвязан.");
    }

}