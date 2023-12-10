package com.softserve.itacademy.controller;

import com.softserve.itacademy.model.Task;
import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.service.TaskService;
import com.softserve.itacademy.service.ToDoService;
import com.softserve.itacademy.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = BEFORE_CLASS)
public class ToDoControllerTest {
    private static final String BASE_URL = "/todos";

    private final MockMvc mockMvc;
    private final ToDoService todoService;
    private final TaskService taskService;
    private final UserService userService;

    @Autowired
    public ToDoControllerTest(MockMvc mockMvc, ToDoService todoService,
                              TaskService taskService, UserService userService) {
        this.mockMvc = mockMvc;
        this.todoService = todoService;
        this.taskService = taskService;
        this.userService = userService;
    }

    @Test
    public void getCreateToDoPageTest() throws Exception {
        long expected = 5L;
        mockMvc.perform(get(BASE_URL + "/create/users/{owner_id}", expected))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("todo", "ownerId"))
                .andExpect(model().attribute("ownerId", expected));
    }

    @Test
    public void createValidToDoTest() throws Exception {
        long ownerId = 5L;
        String titleFieldValue = "title";
        mockMvc.perform(post(BASE_URL + "/create/users/{owner_id}", ownerId)
                        .param("title", titleFieldValue))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", BASE_URL + "/all/users/" + ownerId));

        assertTrue(
                todoService.getByUserId(ownerId).stream()
                        .anyMatch(t -> t.getTitle().equals(titleFieldValue))
        );
    }

    @Test
    public void createInvalidToDoTest() throws Exception {
        long ownerId = 5L;
        String titleField = "title";
        mockMvc.perform(post(BASE_URL + "/create/users/{owner_id}", ownerId)
                        .param(titleField, "  "))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode("todo", titleField, "NotBlank"));
    }

    @Test
    public void getReadToDoTasksPageTest() throws Exception {
        long toDoId = 7L;
        ToDo expectedToDo = todoService.readById(toDoId);
        List<Task> expectedTasks = taskService.getByTodoId(toDoId);
        List<User> expectedUsers = userService.getAll().stream()
                .filter(user -> user.getId() != expectedToDo.getOwner().getId())
                .collect(Collectors.toList());

        mockMvc.perform(get(BASE_URL + "/{id}/tasks", toDoId))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("todo", "tasks", "users"))
                .andExpect(model().attribute("todo", expectedToDo))
                .andExpect(model().attribute("tasks", expectedTasks))
                .andExpect(model().attribute("users", expectedUsers));
    }

    @Test
    public void getUpdateToDoPageTest() throws Exception {
        long toDoId = 7L;
        long ownerId = 4L;
        ToDo expected = todoService.readById(toDoId);
        mockMvc.perform(get(BASE_URL + "/{todo_id}/update/users/{owner_id}", toDoId, ownerId))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("todo"))
                .andExpect(model().attribute("todo", expected));
    }

    @Test
    public void updateValidToDoTest() throws Exception {
        long toDoId = 7L;
        ToDo toDo = todoService.readById(toDoId);
        String titleFieldValue = "title2";
        mockMvc.perform(post(BASE_URL + "/{todo_id}/update/users/{owner_id}", toDoId, toDo.getOwner().getId())
                        .param("id", Long.toString(toDo.getId()))
                        .param("title", titleFieldValue)
                        .param("createdAt", toDo.getCreatedAt().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", BASE_URL + "/all/users/" + toDo.getOwner().getId()));

        assertEquals(todoService.readById(toDoId).getTitle(), titleFieldValue);
    }

    @Test
    public void updateInvalidToDoTest() throws Exception {
        long toDoId = 7L;
        ToDo toDo = todoService.readById(toDoId);
        String expected = "title";
        mockMvc.perform(post(BASE_URL + "/{todo_id}/update/users/{owner_id}", toDoId, toDo.getOwner().getId())
                        .param("id", Long.toString(toDo.getId()))
                        .param(expected, " ")
                        .param("createdAt", toDo.getCreatedAt().toString()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode("todo", expected, "NotBlank"));
    }

    @Test
    public void deleteToDoTest() throws Exception {
        long toDoId = 8L;
        long ownerId = 4L;
        mockMvc.perform(get(BASE_URL + "/{todo_id}/delete/users/{owner_id}", toDoId, ownerId))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", BASE_URL + "/all/users/" + ownerId));

        assertFalse(
                todoService.getByUserId(ownerId).stream()
                        .anyMatch(t -> t.getId() == toDoId)
        );
    }

    @Test
    public void getAllToDosTest() throws Exception {
        long ownerId = 4L;
        User expectedUser = userService.readById(ownerId);
        List<ToDo> expectedToDos = todoService.getByUserId(expectedUser.getId());
        mockMvc.perform(get(BASE_URL + "/all/users/{user_id}", expectedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("todos", "user"))
                .andExpect(model().attribute("todos", expectedToDos))
                .andExpect(model().attribute("user", expectedUser));
    }

    @Test
    @Transactional
    public void addCollaboratorToToDoTest() throws Exception {
        long toDoId = 10L;
        long userId = 5L;
        mockMvc.perform(get(BASE_URL + "/{id}/add", toDoId)
                        .param("user_id", Long.toString(userId)))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", BASE_URL + "/" + toDoId + "/tasks"));

        assertTrue(isCollaboratorAdded(toDoId, userId));
    }

    @Test
    @Transactional
    public void removeCollaboratorFromToDoTest() throws Exception {
        long toDoId = 10L;
        long userId = 4L;
        mockMvc.perform(get(BASE_URL + "/{id}/remove", toDoId)
                        .param("user_id", Long.toString(userId)))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", BASE_URL + "/" + toDoId + "/tasks"));

        assertFalse(isCollaboratorAdded(toDoId, userId));
    }

    private boolean isCollaboratorAdded(long toDoId, long collaboratorId) {
        return todoService.readById(toDoId).getCollaborators().stream()
                .anyMatch(c -> c.getId() == collaboratorId);
    }
}
