package ru.checkdev.notification.telegram.service;

import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.checkdev.notification.domain.TgUser;
import ru.checkdev.notification.repository.TgUserRepository;

@Service
@AllArgsConstructor
public class TgUserService {

    private final TgUserRepository tgUsers;

    public TgUser save(TgUser user) {
        return tgUsers.save(user);
    }

    public boolean checkUserExists(long chatId) {
        return tgUsers.existsByChatId(chatId);
    }

    public Optional<TgUser> findByChatId(long chatId) {
        return tgUsers.findByChatId(chatId);
    }

    public boolean deleteByChatId(long chatId) {
        return tgUsers.deleteByChatId(chatId) > 0;
    }

}