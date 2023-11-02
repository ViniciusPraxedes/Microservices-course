package com.example.usersservice.service;

import com.example.usersservice.model.User;
import com.example.usersservice.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    //This annotation creates a mock of the userRepository
    @Mock
    private UserRepository mockUserRepository;

    //This line of code replaces the userService userRepository by the MockUserRepository
    @InjectMocks
    private UserService userService;

    @Test
    void loadUserByUsername_FindUserByUsername_UserFound() {
        //Given
        String username = "test@hotmail.com";
        User mockUser = new User();
        mockUser.setEmail(username);

        //This line of code makes the mockUserRepository return a user of the same type as the mockUser;
        when(mockUserRepository.findByEmail(username)).thenReturn(Optional.of(mockUser));

        //When
        User savedUser = (User) userService.loadUserByUsername(username);

        //Then
        Assertions.assertThat(mockUserRepository).isNotNull();
        Assertions.assertThat(savedUser).isEqualTo(mockUser);
    }
    @Test
    void loadUserByUsername_FindUserByUsername_UserNotFound(){
        //Given
        String username = "test@hotmail.com";

        //When
        //This line of code makes the mockUserRepository return a user of the same type as the mockUser;
        when(mockUserRepository.findByEmail(username)).thenReturn(Optional.empty());

        //Then
        assertThrows(NoSuchElementException.class, () -> {
            userService.loadUserByUsername(username);
        });
    }
}