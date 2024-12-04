package org.example.tasktrackerserver.controllers;

import org.example.tasktrackerserver.models.User;
import org.example.tasktrackerserver.security.JwtTokenProvider;
import org.example.tasktrackerserver.security.PasswordUtil;
import org.example.tasktrackerserver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AuthenticationController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody User user) throws NoSuchAlgorithmException {
        Optional<User> existingUserOpt = userService.findUserByUsername(user.getUsername());

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();  // Извлекаем объект User из Optional
            String hashedPassword = PasswordUtil.hashPassword(user.getPassword(), existingUser.getSalt());

            if (hashedPassword.equals(existingUser.getPassword())) {
                // Передаем правильные данные в метод generateToken
                String token = jwtTokenProvider.generateToken(existingUser.getUsername(), existingUser.getRole().name(), existingUser.getId());

                return ResponseEntity.ok()
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build();
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
    }


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) throws NoSuchAlgorithmException {
        // Проверка, что пользователь с таким логином не существует
        if (userService.findUserByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username is already taken");
        }

        // Создание пользователя с захешированным паролем
        User newUser = userService.createUser(user);

        // Генерация токена для нового пользователя
        String token = jwtTokenProvider.generateToken(newUser.getUsername(), String.valueOf(newUser.getRole()), newUser.getId());
        return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, "Bearer " + token).build();
    }

}
