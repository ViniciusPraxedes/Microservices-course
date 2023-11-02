package com.example.usersservice.user;

import com.example.usersservice.model.Role;
import com.example.usersservice.model.User;
import com.example.usersservice.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserRepositoryTest {
    @Autowired
    private UserRepository testRepo;

    @AfterEach
    void deleteAll(){
        testRepo.deleteAll();
    }

    @Test
    void findByEmail_FindUserByEmail_ReturnUser() {

        //Given
        String email = "test@hotmail.com";
        User testUser = new User();
        testUser.setFirstname("Test");
        testUser.setLastname("Test");
        testUser.setEmail(email);
        testUser.setPassword("Test");
        testUser.setRole(Role.USER);

        testRepo.save(testUser);

        //When
        User savedUser = testRepo.findByEmail(email).get();

        //Then
        Assertions.assertThat(savedUser).isEqualTo(testUser);

    }
    @Test
    void findByEmail_FindUserByEmail_ReturnNull(){

        //Given
        String email = "test@hotmail.com";
        User testUser = new User();
        testUser.setFirstname("Test");
        testUser.setLastname("Test");
        testUser.setEmail(email);
        testUser.setPassword("Test");
        testUser.setRole(Role.USER);

        //When
        boolean exists = testRepo.findByEmail(email).isPresent();

        //Then
        Assertions.assertThat(exists).isFalse();
    }
}
