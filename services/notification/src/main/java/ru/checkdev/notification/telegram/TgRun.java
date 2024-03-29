package ru.checkdev.notification.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.checkdev.notification.telegram.action.*;
import ru.checkdev.notification.telegram.service.TgAuthCallWebClient;
import ru.checkdev.notification.telegram.service.TgUserService;

import java.util.List;
import java.util.Map;

/**
 * 3. Мидл
 * Инициализация телеграм бот,
 * username = берем из properties
 * token = берем из properties
 *
 * @author Dmitry Stepanov, user Dmitry
 * @since 12.09.2023
 */
@Component
@Slf4j
public class TgRun {
    private final TgAuthCallWebClient tgAuthCallWebClient;
    private final TgUserService tgUserService;
    @Value("${tg.username}")
    private String username;
    @Value("${tg.token}")
    private String token;
    @Value("${server.site.url.login}")
    private String urlSiteAuth;

    public TgRun(TgAuthCallWebClient tgAuthCallWebClient, TgUserService tgUserService) {
        this.tgAuthCallWebClient = tgAuthCallWebClient;
        this.tgUserService = tgUserService;
    }

    @Bean
    public void initTg() {
        Map<String, Action> actionMap = Map.of(
                "/start", new InfoAction(List.of(
                        "/start - для получения списка доступных команд;",
                        "/new - для регистрации нового пользователя;",
                        "/check - для получения привязанных имени и почты;",
                        "/forget - для получения нового пароля;",
                        "/subscribe - для привязки номера Телеграм к аккаунту;",
                        "/unsubscribe - для отвязывания номера Телеграм от аккаунта;")),
                "/new", new RegAction(tgUserService, tgAuthCallWebClient, urlSiteAuth),
                "/check", new CheckAction(tgUserService),
                "/forget", new ForgetAction(tgUserService, tgAuthCallWebClient, urlSiteAuth),
                "/subscribe", new SubscribeAction(tgUserService, tgAuthCallWebClient),
                "/unsubscribe", new UnsubscribeAction(tgUserService)
        );
        try {
            BotMenu menu = new BotMenu(actionMap, username, token);

            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(menu);
        } catch (TelegramApiException e) {
            log.error("Telegram bot: {}, ERROR {}", username, e.getMessage());
        }
    }
}
