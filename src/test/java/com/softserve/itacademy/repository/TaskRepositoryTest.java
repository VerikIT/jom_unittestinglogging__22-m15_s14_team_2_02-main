package com.softserve.itacademy.repository;


import com.softserve.itacademy.model.Priority;
import com.softserve.itacademy.model.State;
import com.softserve.itacademy.model.Task;
import com.softserve.itacademy.model.ToDo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ToDoRepository toDoRepository;
    @Autowired
    private StateRepository stateRepository;


    @Test
    void getByTodoIdTest() {
        ToDo todo = toDoRepository.getOne(13L);
        State state = stateRepository.getOne(7L);
        Task task = new Task();
        task.setName("TestTask");
        task.setPriority(Priority.LOW);
        task.setTodo(todo);
        task.setState(state);
        taskRepository.save(task);

        List<Task> actualList = taskRepository.getByTodoId(todo.getId());
        Task actual = actualList.stream().findFirst().get();

        assertEquals(1, actualList.size());
        assertEquals(task, actual);
    }
}