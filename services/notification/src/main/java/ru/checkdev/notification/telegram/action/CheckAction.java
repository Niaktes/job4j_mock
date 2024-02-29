package ru.checkdev.notification.telegram.action;

import java.util.Optional;
import lombok.AllArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.TgUser;
import ru.checkdev.notification.telegram.service.TgUserService;

@AllArgsConstructor
public class CheckAction implements Action {

    private final TgUserService tgUserService;

    /**
     * Метод формирует пользователю ответное сообщение
     * Сначала происходит проверка зарегистрирован ли пользователь
     * При положительном результате пользователь получает сообщение
     * с именем пользователя и почтой, привязанными к аккаунту.
     * @param message Сообщение от пользователя.
     * @return BotApiMethod<Message> Ответное сообщение бота.
     */
    @Override
    public BotApiMethod<Message> handle(Message message) {
        long chatId = message.getChatId();
        Optional<TgUser> existedUser = tgUserService.findByChatId(chatId);
        String text;
        String sl = System.lineSeparator();

        if (existedUser.isEmpty()) {
            text = "Вы ещё не зарегистрированы в системе." + sl
                    + "Для регистрации, пожалуйста, используйте команду /new.";
            return new SendMessage(String.valueOf(chatId), text);
        }

        text = "К Вашему аккаунту привязан пользователь со следующими данными: " + sl
                + "имя пользователя: " + existedUser.get().getUsername() + sl
                + "почта: " + existedUser.get().getEmail();
        return new SendMessage(String.valueOf(chatId), text);
    }

    @Override
    public BotApiMethod<Message> callback(Message message) {
        return handle(message);
    }

}