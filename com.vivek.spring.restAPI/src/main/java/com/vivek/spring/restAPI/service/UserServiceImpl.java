package com.vivek.spring.restAPI.service;

import com.vivek.spring.restAPI.Entities.Session;
import com.vivek.spring.restAPI.Entities.User;
import com.vivek.spring.restAPI.Repositories.UserRepository;
import com.vivek.spring.restAPI.Responses.SessionDetails;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class UserServiceImpl implements UserService
{
    private UserRepository userRepository;
    @Override
    public void registerUser(User user)
    {
        userRepository.save(user);
    }
    @Override
    public boolean authenticate(String username,String password)
    {
        User user = userRepository.findAllByUsername(username);
        if(user==null||(!user.getPassword().equals(password)))
            return false;
        return true;
    }
    @Override
    public User getUserDetails(String username)
    {
        return userRepository.findAllByUsername(username);
    }
    @Override
    public boolean updateContact(String username,String newContact)
    {
        userRepository.updateContact(newContact,username);
        return true;
    }

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
