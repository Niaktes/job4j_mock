package ru.checkdev.notification.web;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.checkdev.notification.service.SubscribeCategoryService;

@RestController
@RequestMapping("/subscribeCategory")
@AllArgsConstructor
public class SubscribeCategoriesController {
    private final SubscribeCategoryService service;

    @GetMapping("/{id}")
    public ResponseEntity<List<Integer>> findCategoriesByUserId(@PathVariable int id) {
        List<Integer> list = service.findCategoriesByUserId(id);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

}