package com.softserve.itacademy.controller;

import com.softserve.itacademy.dto.TaskDto;
import com.softserve.itacademy.dto.TaskTransformer;
import com.softserve.itacademy.model.Priority;
import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.service.StateService;
import com.softserve.itacademy.service.TaskService;
import com.softserve.itacademy.service.ToDoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = BEFORE_CLASS)
class TaskControllerTest {

    private final MockMvc mockMvc;

    private final TaskService taskService;

    private final ToDoService todoService;

    private final StateService stateService;


    @Autowired
    TaskControllerTest(MockMvc mockMvc, TaskService taskService, ToDoService todoService, StateService stateService) {
        this.mockMvc = mockMvc;
        this.taskService = taskService;
        this.todoService = todoService;
        this.stateService = stateService;
    }

    @Test
    void testCreateTaskGetMapping() throws Exception {
        long toDoId = 13L;
        ToDo expectedTodo = todoService.readById(toDoId);
        mockMvc.perform(get("/tasks/create/todos/{todo_id}", toDoId))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("task", "todo", "priorities"))
                .andExpect(model().attribute("task", new TaskDto()))
                .andExpect(model().attribute("todo", expectedTodo))
                .andExpect(model().attribute("priorities", Priority.values()))
        ;
    }

    @Test
    void testCorrectCreateTaskPostMapping() throws Exception {
        long todoId = 13L;
        mockMvc.perform(post("/tasks/create/todos/{todo_id}", todoId)
                        .param("name", "TestTaskName")
                        .param("priority", "LOW")
                        .param("todoId", Long.toString(todoId)))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/todos/" + todoId + "/tasks"));
    }

    @Test
    void testInvalidCreateTaskPostMapping() throws Exception {
        long todoId = 13L;
        mockMvc.perform(post("/tasks/create/todos/{todo_id}", todoId)
                        .param("name", "")
                        .param("priority", "LOW")
                        .param("todoId", Long.toString(todoId)))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode("task", "name", "NotBlank"));
    }

    @Test
    void testUpdateTaskGetMapping() throws Exception {

        long taskId = 5L;
        long todoId = 7L;
        TaskDto expectedTaskDto = TaskTransformer.convertToDto(taskService.readById(taskId));
        mockMvc.perform(get("/tasks/{task_id}/update/todos/{todo_id}", taskId, todoId))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("task", "priorities", "states"))
                .andExpect(model().attribute("task", expectedTaskDto))
                .andExpect(model().attribute("states", stateService.getAll()))
                .andExpect(model().attribute("priorities", Priority.values()))
        ;
    }

    @Test
    void testCorrectUpdateTaskPostMapping() throws Exception {
        long stateId = 6L;
        long taskId = 5L;
        long todoId = 7L;
        mockMvc.perform(post("/tasks/{task_id}/update/todos/{todo_id}", taskId, todoId)
                        .param("name", "TestTaskName")
                        .param("priority", "LOW")
                        .param("stateId", Long.toString(stateId))
                        .param("id", Long.toString(taskId))
                        .param("todoId", Long.toString(todoId))
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/todos/" + todoId + "/tasks"));
    }

    @Test
    void testInvalidUpdateTaskPostMapping() throws Exception {
        long stateId = 6L;
        long taskId = 5L;
        long todoId = 7L;
        mockMvc.perform(post("/tasks/{task_id}/update/todos/{todo_id}", taskId, todoId)
                        .param("name", "")
                        .param("priority", "LOW")
                        .param("stateId", Long.toString(stateId))
                        .param("id", Long.toString(taskId))
                        .param("todoId", Long.toString(todoId))
                )
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode("task", "name", "NotBlank"));
    }


    @Test
    void delete() throws Exception {

        long taskId = 5L;
        long todoId = 7L;
        mockMvc.perform(get("/tasks/{task_id}/delete/todos/{todo_id}", taskId, todoId))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/todos/" + todoId + "/tasks"));
    }
}