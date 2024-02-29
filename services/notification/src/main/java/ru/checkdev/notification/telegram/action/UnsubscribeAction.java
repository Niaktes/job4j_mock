package ru.checkdev.notification.telegram.action;

import lombok.AllArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.telegram.service.TgUserService;

@AllArgsConstructor
public class UnsubscribeAction implements Action {

    private final TgUserService tgUserService;

    /**
     * Метод формирует пользователю ответное сообщение
     * и удаляет привязку аккаунта телеграм к аккаунту.
     * @param message Сообщение от пользователя.
     * @return BotApiMethod<Message> Ответное сообщение бота.
     */
    @Override
    public BotApiMethod<Message> handle(Message message) {
        long chatId = message.getChatId();
        String text;
        String sl = System.lineSeparator();

        if (!tgUserService.checkUserExists(chatId)) {
            text = "Вы ещё не зарегистрированы в системе." + sl
                    + "Для регистрации, пожалуйста, используйте команду /new.";
            return new SendMessage(String.valueOf(chatId), text);
        }
        tgUserService.deleteByChatId(chatId);
        text = "Аккаунт телеграм успешно отвязан.";
        return new SendMessage(String.valueOf(chatId), text);
    }

    @Override
    public BotApiMethod<Message> callback(Message message) {
        return handle(message);
    }

}