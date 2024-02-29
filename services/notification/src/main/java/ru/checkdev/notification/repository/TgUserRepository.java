package ru.checkdev.notification.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.checkdev.notification.domain.TgUser;

public interface TgUserRepository extends CrudRepository<TgUser, Integer> {

    Optional<TgUser> findByChatId(long chatId);

    boolean existsByChatId(long chatId);

    @Transactional
    Integer deleteByChatId(long chatId);

}