package ru.checkdev.notification.telegram.action;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.TgUser;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClient;
import ru.checkdev.notification.telegram.service.TgUserService;

@AllArgsConstructor
@Slf4j
public class SubscribeAction implements Action {

    private static final String ERROR_OBJECT = "error";
    private static final String PERSON_CHECK = "/person/checkUser";
    private final TgConfig tgConfig = new TgConfig("tg/", 8);
    private final TgUserService tgUserService;
    private final TgAuthCallWebClient authCallWebClient;

    /**
     * Метод формирует пользователю ответное сообщение
     * и меняет статус пользователя на "подписан".
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
        text = "Введите пароль:";
        return new SendMessage(String.valueOf(chatId), text);
    }

    @Override
    public BotApiMethod<Message> callback(Message message) {
        long chatId = message.getChatId();
        TgUser tgUser = tgUserService.findByChatId(chatId).get();
        String password = message.getText();
        String text;
        String sl = System.lineSeparator();

        Object result;
        try {
            result = authCallWebClient.doGet(PERSON_CHECK
                    + String.format("?email=%s&password=%s", tgUser.getEmail(), password)
            ).block();
        } catch (Exception e) {
            log.error("WebClient doGet error: {}", e.getMessage());
            text = "Сервис не доступен попробуйте позже" + sl
                    + "/start";
            return new SendMessage(String.valueOf(chatId), text);
        }

        var mapObject = tgConfig.getObjectToMap(result);
        if (mapObject.containsKey(ERROR_OBJECT)) {
            text = "Ошибка: " + mapObject.get(ERROR_OBJECT) + sl
                    + "Попробуйте ещё раз: /subscribe";
            return new SendMessage(String.valueOf(chatId), text);
        }

        tgUserService.updateSubscribedByChatId(chatId, true);
        text = "Подписка оформлена.";
        return new SendMessage(String.valueOf(chatId), text);
    }

}