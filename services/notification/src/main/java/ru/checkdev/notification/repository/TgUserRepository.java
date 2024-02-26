package ru.checkdev.notification.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.checkdev.notification.domain.TgUser;

public interface TgUserRepository extends CrudRepository<TgUser, Integer> {

    Optional<TgUser> findByChatId(long chatId);

    boolean existsByChatId(long chatId);

    @Modifying
    @Transactional
    @Query("UPDATE tg_user u SET u.subscribed = ?2 WHERE u.chatId = ?1")
    void updateSubscribedByChatId(long chatId, boolean archive);

}