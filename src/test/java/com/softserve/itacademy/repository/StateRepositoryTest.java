package com.softserve.itacademy.repository;

import com.softserve.itacademy.model.State;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class StateRepositoryTest {
    private final StateRepository stateRepository;

    @Autowired
    public StateRepositoryTest(StateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }

    @Test
    public void getByNameTest() {
        String stateName = "New";
        State expected = stateRepository.getAll().stream()
                .filter(s -> s.getName().equals(stateName))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        assertEquals(expected, stateRepository.getByName(stateName));
    }

    @Test
    public void getAllTest() {
        List<State> expected = stateRepository.findAll();
        assertEquals(expected, stateRepository.getAll());
    }
}
