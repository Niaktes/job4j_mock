package ru.checkdev.notification.telegram.action;

import java.util.Calendar;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.checkdev.notification.domain.PersonDTO;
import ru.checkdev.notification.domain.TgUser;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClient;
import ru.checkdev.notification.telegram.service.TgUserService;

@AllArgsConstructor
@Slf4j
public class ForgetAction implements Action {

    private static final String ERROR_OBJECT = "error";
    private static final String URL_FORGOT_PASSWORD = "/forgot";
    private final TgConfig tgConfig = new TgConfig("tg/", 8);
    private final TgUserService tgUserService;
    private final TgAuthCallWebClient authCallWebClient;
    private final String urlSiteAuth;

    /**
     * Метод создает новый пароль пользователя
     * и формирует пользователю ответное сообщение с логином и новым паролем.
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

        String username = existedUser.get().getUsername();
        String email = existedUser.get().getEmail();
        String newPassword = tgConfig.getPassword();
        PersonDTO person = new PersonDTO(username, email, newPassword, true, null,
                Calendar.getInstance());
        Object result;
        try {
            result = authCallWebClient.doPost(URL_FORGOT_PASSWORD, person).block();
        } catch (Exception e) {
            log.error("WebClient doPost error: {}", e.getMessage());
            text = "Сервис не доступен попробуйте позже" + sl
                    + "/start";
            return new SendMessage(String.valueOf(chatId), text);
        }

        var mapObject = tgConfig.getObjectToMap(result);
        if (mapObject.containsKey(ERROR_OBJECT)) {
            text = "Ошибка при сбросе пароля: " + mapObject.get(ERROR_OBJECT);
            return new SendMessage(String.valueOf(chatId), text);
        }

        text = "Ваши новые данные для входа: " + sl
                + "Логин: " + email + sl
                + "Пароль: " + newPassword + sl
                + urlSiteAuth;
        return new SendMessage(String.valueOf(chatId), text);
    }

    @Override
    public BotApiMethod<Message> callback(Message message) {
        return handle(message);
    }

}