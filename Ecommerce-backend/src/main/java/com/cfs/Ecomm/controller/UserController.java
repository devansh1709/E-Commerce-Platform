package com.cfs.Ecomm.controller;

import com.cfs.Ecomm.dto.AuthResponse;
import com.cfs.Ecomm.dto.UserDTO;
import com.cfs.Ecomm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private  UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@RequestBody UserDTO userDTO) {
        // Map DTO to entity to avoid exposing User model directly
        com.cfs.Ecomm.model.User user = new com.cfs.Ecomm.model.User();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());

        AuthResponse response = userService.registerUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@RequestBody UserDTO userDTO) {
        AuthResponse response = userService.loginUser(userDTO.getEmail(), userDTO.getPassword());
        return ResponseEntity.ok(response);
    }

}
