package com.softserve.itacademy.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.softserve.itacademy.model.User;
import com.softserve.itacademy.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    private final List<User> users;

    public UserServiceImpl() {
        users = new ArrayList<>();
    }

    @Override
    public User addUser(User user) {
        if (user == null) return null;
        if (user.getEmail() == null) return null;
        //if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) return null;
        //if (user.getLastName() == null || user.getLastName().trim().isEmpty()) return null;

        for (User existingUser : users) {
            if (existingUser.getEmail().equalsIgnoreCase(user.getEmail())) {
                return null;
            }
        }
        users.add(user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (user == null) return null;
        if (user.getEmail() == null) return null;

        user.setEmail(user.getEmail());

        User existing = null;
        int index = -1;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getEmail().equalsIgnoreCase(user.getEmail())) {
                existing = users.get(i);
                index = i;
                break;
            }
        }
        if (existing == null) return null;


        // validate first/last name: cannot be null or empty
        if (user.getFirstName() != null && !user.getFirstName().trim().isEmpty()) {
            existing.setFirstName(user.getFirstName());
        }
        if (user.getLastName() != null && !user.getLastName().trim().isEmpty()) {
            existing.setLastName(user.getLastName());
        }
        existing.setPassword(user.getPassword());

        if (user.getEmail() != null && !user.getEmail().equals(existing.getEmail())) {
            for (int i = 0; i < users.size(); i++) {
                if (i != index && users.get(i).getEmail().equalsIgnoreCase(user.getEmail())) {
                    return null;
                }
            }
            existing.setEmail(user.getEmail());
        }

        if (user.getMyTodos() != null) {
            existing.setMyTodos(user.getMyTodos());
        }

        return existing;
    }

    @Override
    public void deleteUser(User user) {
        if (user == null) return;
        if (user.getEmail() == null) return;

        users.removeIf(existingUser -> existingUser.getEmail().equalsIgnoreCase(user.getEmail()));
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users);
    }
}
