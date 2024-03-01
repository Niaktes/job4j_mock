package ru.checkdev.notification.service;

import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.checkdev.notification.domain.SubscribeCategory;
import ru.checkdev.notification.repository.SubscribeCategoryRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class SubscribeCategoryService {
    private final SubscribeCategoryRepository repository;

    public List<SubscribeCategory> findAll() {
        return repository.findAll();
    }

    @KafkaListener(topics = "subscribeCategory_add")
    public void save(SubscribeCategory subscribeCategory) {
        repository.save(subscribeCategory);
    }

    public List<Integer> findCategoriesByUserId(int userId) {
        List<Integer> rsl = new ArrayList<>();
        List<SubscribeCategory> list = repository.findByUserId(userId);
        for (SubscribeCategory subscribeCategory : list) {
            rsl.add(subscribeCategory.getCategoryId());
        }
        return rsl;
    }

    @KafkaListener(topics = "subscribeCategory_delete")
    public void delete(SubscribeCategory subscribeCategory) {
        SubscribeCategory subscribeCategoryRsl = repository
                .findByUserIdAndCategoryId(subscribeCategory.getUserId(), subscribeCategory.getCategoryId());
        repository.delete(subscribeCategoryRsl);
    }
}