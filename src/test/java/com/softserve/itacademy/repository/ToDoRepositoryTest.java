package com.softserve.itacademy.repository;

import com.softserve.itacademy.model.ToDo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class ToDoRepositoryTest {
    private final ToDoRepository toDoRepository;

    @Autowired
    public ToDoRepositoryTest(ToDoRepository toDoRepository) {
        this.toDoRepository = toDoRepository;
    }

    @Test
    public void getByUserIdTest() {
        long userId = 5L;
        List<ToDo> expected = toDoRepository.findAll().stream()
                .filter(t -> t.getOwner().getId() == userId || t.getCollaborators().stream()
                        .anyMatch(c -> c.getId() == userId))
                .collect(Collectors.toList());
        assertEquals(expected, toDoRepository.getByUserId(userId));
    }
}
