package com.cfs.Ecomm.service;

import com.cfs.Ecomm.model.User;
import com.cfs.Ecomm.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User registerUser(User user){
        User newUser=userRepository.save(user);
        System.out.println("User added...");
        return newUser;
    }

    public User loginUser(String email, String password){
        User user=userRepository.findByEmail(email);
        if(user!=null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

}
