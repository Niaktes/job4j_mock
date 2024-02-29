package ru.checkdev.notification.telegram.action;

import java.util.Map;
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
    private static final String PROFILE_URL = "/person/current";
    private final TgConfig tgConfig = new TgConfig("tg/", 8);
    private final TgUserService tgUserService;
    private final TgAuthCallWebClient authCallWebClient;

    /**
     * Метод проверяет зарегистрирован ли пользователь
     * и подготавливает ответное сообщение.
     * @param message сообщение пользователя
     * @return BotApiMethod<Message> Ответное сообщение бота.
     */
    @Override
    public BotApiMethod<Message> handle(Message message) {
        long chatId = message.getChatId();
        String text;
        String sl = System.lineSeparator();
        if (tgUserService.checkUserExists(chatId)) {
            text = "Ваш аккаунт телеграм уже привязан к аккаунту сайта." + sl
                    + "Чтобы узнать регистрационные данные используйте /check" + sl
                    + "Чтобы отвязать аккаунт используйте /unsubscribe";
            return new SendMessage(String.valueOf(chatId), text);
        }
        text = "Для привязки аккаунта, введите, пожалуйста, email и пароль через пробел." + sl
                + "Пример: \"example@mail.ru password\" (без кавычек)";
        return new SendMessage(String.valueOf(chatId), text);
    }

    /**
     * Метод формирует пользователю ответное сообщение
     * и привязывает ранее созданный аккаунт к аккаунту телеграм.
     * При этом метод проходит следующие этапы:
     * 1. Проверка на соответствие формату Email введенного текста;
     * 2. Попытка получения от сервиса авторизации токена доступа с возможным результатом:
     * 2.1. Сервис авторизации не доступен;
     * 2.2. Логин или пароль не верны;
     * 2.3. Логин и пароль верны - токен получен;
     * 3. Получение от сервиса авторизации профиль ранее зарегистрированного пользователя:
     * 3.1. Сервис авторизации не доступен;
     * 3.2. Профиль получен.
     * 4. Запись в базу данных привязанного аккаунта телеграм с данными пользователя.
     * @param message Сообщение от пользователя.
     * @return BotApiMethod<Message> Ответное сообщение бота.
     */
    @Override
    public BotApiMethod<Message> callback(Message message) {
        String chatId = message.getChatId().toString();
        String[] emailAndPassword = message.getText().split(" ");
        String text;
        if (emailAndPassword.length != 2) {
            text = "Пожалуйста, введите пару email и пароль как указано в примере выше.";
            return new SendMessage(chatId, text);
        }
        String email = emailAndPassword[0];
        String password = emailAndPassword[1];
        String sl = System.lineSeparator();

        if (!tgConfig.isEmail(email)) {
            text = "Email: " + email + " не корректный." + sl
                    + "попробуйте снова /subscribe";
            return new SendMessage(chatId, text);
        }

        Object tokenResult;
        try {
            Map<String, String> params = Map.of(
                    "username", email,
                    "password", password
            );
            tokenResult = authCallWebClient.token(params).block();
        } catch (Exception e) {
            log.error("WebClient token error: {}", e.getMessage());
            text = "Сервис авторизации не доступен, попробуйте позже.";
            return new SendMessage(chatId, text);
        }

        var tokenResultMap = tgConfig.getObjectToMap(tokenResult);
        if (tokenResultMap.containsKey(ERROR_OBJECT)) {
            text = "Ошибка привязки аккаунтов:" + sl
                    + "Введенные email или пароль не верны." + sl
                    + "Попробуйте ещё раз /subscribe";
            return new SendMessage(chatId, text);
        }
        String token = (String) tokenResultMap.get("access_token");

        Object profileResult;
        try {
            profileResult = authCallWebClient.doGet(PROFILE_URL, token).block();
        } catch (Exception e) {
            log.error("WebClient doGet error: {}", e.getMessage());
            text = "Сервис данных о профиле не доступен, попробуйте позже.";
            return new SendMessage(chatId, text);
        }
        var profileResultMap = tgConfig.getObjectToMap(profileResult);
        TgUser tgUser = new TgUser(0, (String) profileResultMap.get("username"), email,
                message.getChatId(), (int) profileResultMap.get("id"));
        tgUserService.save(tgUser);
        text = "Аккаунт с почтой " + email + " успешно привязан.";
        return new SendMessage(chatId, text);
    }

}