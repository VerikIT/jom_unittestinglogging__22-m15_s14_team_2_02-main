package com.softserve.itacademy.repository;

import com.softserve.itacademy.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest
public class UserRepositoryTest {
    private final UserRepository userRepository;

    @Autowired
    public UserRepositoryTest(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Test
    public void getUserByEmailExistingUser() {
        String existingEmail = "nick@mail.com";
        User retrievedUser = userRepository.getUserByEmail(existingEmail);
        assertEquals(existingEmail, retrievedUser.getEmail());
    }

    @Test
    public void getUserByEmailNonExistingUser() {
        String email = "nick@testfail.com";
        User retrievedUser = userRepository.getUserByEmail(email);
        assertNull(retrievedUser);
    }
}
