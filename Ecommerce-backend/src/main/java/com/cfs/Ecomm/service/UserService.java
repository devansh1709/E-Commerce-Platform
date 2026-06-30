package com.cfs.Ecomm.service;

import com.cfs.Ecomm.dto.AuthResponse;
import com.cfs.Ecomm.exception.BadRequestException;
import com.cfs.Ecomm.model.User;
import com.cfs.Ecomm.repo.UserRepository;
import com.cfs.Ecomm.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;


    public AuthResponse registerUser(User user) {

        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new BadRequestException("Email already registered");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User saved = userRepository.save(user);

        String token = jwtUtil.generateToken(saved.getEmail());

        return new AuthResponse(token, saved.getId(), saved.getName(), saved.getEmail());
    }

    public AuthResponse loginUser(String email, String password) {

        User user = userRepository.findByEmail(email);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail());
    }

}
