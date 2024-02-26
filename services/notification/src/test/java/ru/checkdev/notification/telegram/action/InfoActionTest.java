package ru.checkdev.notification.telegram.action;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class InfoActionTest {

    private List<String> actions;
    private Message message;
    private InfoAction action;

    @BeforeEach
    void setUp() {
        actions = List.of("/first", "/second", "/third");
        action = new InfoAction(actions);
        message = new Message();
        message.setChat(new Chat(1L, "private"));
    }

    @Test
    void whenInfoActionThenGetActionsListMessage() {
        SendMessage actualAnswer = (SendMessage) action.handle(message);

        assertThat(actualAnswer.getChatId()).isEqualTo(String.valueOf(message.getChatId()));
        assertThat(actualAnswer.getText())
                .contains("Выберите действие")
                .contains(actions.get(0))
                .contains(actions.get(2));
    }

}