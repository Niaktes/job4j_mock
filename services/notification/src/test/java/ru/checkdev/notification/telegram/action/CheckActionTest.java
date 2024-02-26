package ru.checkdev.notification.telegram.action;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.TgUser;
import ru.checkdev.notification.telegram.service.TgUserService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckActionTest {

    @Mock
    private TgUserService tgUserServiceMock;

    private Message message;
    private CheckAction action;

    @BeforeEach
    void setUp() {
        action = new CheckAction(tgUserServiceMock);
        message = new Message();
        message.setChat(new Chat(1L, "private"));
    }

    @Test
    void whenUserNotExistsThenGetNotExistsMessage() {
        when(tgUserServiceMock.findByChatId(message.getChatId())).thenReturn(Optional.empty());

        SendMessage actualAnswer = (SendMessage) action.handle(message);

        assertThat(actualAnswer.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(actualAnswer.getText()).contains("Вы ещё не зарегистрированы");
    }

    @Test
    void whenUserExistsThenGetMessageWithUsernameAndEmail() {
        TgUser user = new TgUser(1, "username", "e@mail", message.getChatId(), false, 1);
        when(tgUserServiceMock.findByChatId(message.getChatId())).thenReturn(Optional.of(user));

        SendMessage actualAnswer = (SendMessage) action.handle(message);

        assertThat(actualAnswer.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(actualAnswer.getText())
                .contains("имя пользователя: " + user.getUsername())
                .contains("почта: " + user.getEmail());
    }

}