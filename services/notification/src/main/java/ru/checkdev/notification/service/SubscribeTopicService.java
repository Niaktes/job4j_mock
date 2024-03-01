package ru.checkdev.notification.service;

import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.checkdev.notification.domain.SubscribeTopic;
import ru.checkdev.notification.repository.SubscribeTopicRepository;
import java.util.List;

@Service
@AllArgsConstructor
public class SubscribeTopicService {
    private final SubscribeTopicRepository repository;

    public List<SubscribeTopic> findAll() {
        return repository.findAll();
    }

    @KafkaListener(topics = "subscribeTopic_add")
    public void save(SubscribeTopic subscribeTopic) {
        repository.save(subscribeTopic);
    }

    public List<Integer> findTopicByUserId(int userId) {
        return repository.findByUserId(userId).stream()
                .map(SubscribeTopic::getTopicId)
                .toList();
    }

    @KafkaListener(topics = "subscribeTopic_delete")
    public void delete(SubscribeTopic subscribeTopic) {
        SubscribeTopic rsl = repository
                .findByUserIdAndTopicId(subscribeTopic.getUserId(), subscribeTopic.getTopicId());
        repository.delete(rsl);
    }
}