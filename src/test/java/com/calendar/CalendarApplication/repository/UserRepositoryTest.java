package com.calendar.CalendarApplication.repository;

import com.calendar.CalendarApplication.dtos.user.UserDto;
import com.calendar.CalendarApplication.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;


    @Test
    @DisplayName("Should return user successfully")
    void findByEmailCase1() {

        String dateStr = "11/11/2005";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate localDate = LocalDate.parse(dateStr, formatter);

        UserDto data = new UserDto("Gustavo", "gustavo@gmail.com", "pass", localDate);

        this.createUser(data);

        Optional<User> user = userRepository.findByEmail("gustavo@gmail.com");

        assertTrue(user.isPresent());

    }

    @Test
    @DisplayName("Should not return user successfully")
    void findByEmailCase2() {

        Optional<User> user = userRepository.findByEmail("gustavo@gmail.com");

        assertTrue(user.isEmpty());

    }

    private User createUser(UserDto userDto) {
        User user = new User(userDto.username(), userDto.email(), userDto.password(), userDto.birth_date());

        userRepository.save(user);

        return user;
    }
}