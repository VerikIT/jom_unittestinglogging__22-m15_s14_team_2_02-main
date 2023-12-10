package com.softserve.itacademy.controller;

import com.softserve.itacademy.model.User;
import com.softserve.itacademy.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_CLASS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = BEFORE_CLASS)
public class UserControllerTest {
    private static final String BASE_URL = "/users";

    private final MockMvc mockMvc;
    private final UserService userService;

    @Autowired
    public UserControllerTest(MockMvc mockMvc, UserService userService) {
        this.mockMvc = mockMvc;
        this.userService = userService;
    }

    @Test
    public void getCreateUserPageTest() throws Exception {
        mockMvc.perform(get(BASE_URL + "/create"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user"));
    }

    @Test
    public void createValidUserTest() throws Exception {
        mockMvc.perform(post(BASE_URL + "/create")
                        .param("firstName", "Oleg")
                        .param("lastName", "Olegovych")
                        .param("email", "olegovych@example.com")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void createInvalidUserTest() throws Exception {
        mockMvc.perform(post(BASE_URL + "/create")
                        .param("firstName", "")
                        .param("lastName", "")
                        .param("email", "")
                        .param("password", "password"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode("user", "firstName", "Pattern"))
                .andExpect(model().attributeHasFieldErrorCode("user", "lastName", "Pattern"))
                .andExpect(model().attributeHasFieldErrorCode("user", "email", "Pattern"));
    }

    @Test
    public void getReadUserPageTest() throws Exception {
        long userId = 6L;
        mockMvc.perform(get(BASE_URL + "/{id}/read", userId))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("user", userService.readById(userId)));
    }

    @Test
    public void getUpdateUserPageTest() throws Exception {
        long userId = 6L;
        mockMvc.perform(get(BASE_URL + "/{id}/update", userId))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user", "roles"))
                .andExpect(model().attribute("user", userService.readById(userId)));
    }

    @Test
    public void updateValidUserTest() throws Exception {
        long userId = 6L;
        String newFirstName = "Oleg";
        String newLastName = "Nikolayevich";
        String newEmail = "newemail@example.com";
        String newPassword = "newpassword";
        long newRoleId = 3L;

        mockMvc.perform(post(BASE_URL + "/{id}/update", userId)
                        .param("firstName", newFirstName)
                        .param("lastName", newLastName)
                        .param("email", newEmail)
                        .param("password", newPassword)
                        .param("roleId", String.valueOf(newRoleId)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern(BASE_URL + "/*/read"));

        User updatedUser = userService.readById(userId);
        assertEquals(updatedUser.getFirstName(), newFirstName);
        assertEquals(updatedUser.getLastName(), newLastName);
        assertEquals(updatedUser.getEmail(), newEmail);
        assertEquals(updatedUser.getPassword(), newPassword);
    }

    @Test
    public void updateInvalidUserTest() throws Exception {
        long userId = 6L;
        User originalUser = userService.readById(userId);

        mockMvc.perform(post(BASE_URL + "/{id}/update", userId)
                        .param("firstName", "Oleg")
                        .param("lastName", "Nikolayevich")
                        .param("email", "nikolayevichexample.com")
                        .param("password", "password")
                        .param("roleId", "2"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode("user", "email", "Pattern"));

        User updatedUser = userService.readById(userId);

        assertEquals(updatedUser.getFirstName(), originalUser.getFirstName());
        assertEquals(updatedUser.getLastName(), originalUser.getLastName());
        assertEquals(updatedUser.getEmail(), originalUser.getEmail());
        assertEquals(updatedUser.getPassword(), originalUser.getPassword());
        assertEquals(updatedUser.getRole(), originalUser.getRole());
    }

    @Test
    public void deleteUserTest() throws Exception {
        long userId = 6L;
        mockMvc.perform(get(BASE_URL + "/{id}/delete", userId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(BASE_URL + "/all"));
        assertThrows(EntityNotFoundException.class, () -> userService.readById(userId));
    }

    @Test
    public void getAllUsersTest() throws Exception {
        mockMvc.perform(get(BASE_URL + "/all"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("users"));
    }
}