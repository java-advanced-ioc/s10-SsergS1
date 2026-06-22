package com.softserve.itacademy.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.service.ToDoService;
import com.softserve.itacademy.service.UserService;

@Service
public class ToDoServiceImpl implements ToDoService {

    private final UserService userService;

    @Autowired
    public ToDoServiceImpl(UserService userService) {
        this.userService = userService;
    }

    public ToDo addTodo(User user, ToDo todo) {
        if (user == null || todo == null) return null;
        if (todo.getTitle() == null || todo.getTitle().trim().isEmpty()) return null;

        List<User> allUsers = userService.getAll();
        if (!allUsers.contains(user)) return null;

        if (user.getMyTodos() != null) {
            for (ToDo existingTodo : user.getMyTodos()) {
                if (existingTodo.getTitle().equalsIgnoreCase(todo.getTitle())) {
                    return null;
                }
            }
        }

        todo.setCreatedAt(LocalDateTime.now());
        todo.setOwner(user);

        if (user.getMyTodos() == null) {
            user.setMyTodos(new ArrayList<>());
        }

        user.getMyTodos().add(todo);
        return todo;
    }

    public ToDo updateTodo(ToDo todo) {
        if (todo == null) return null;
        if (todo.getOwner() == null
                || todo.getTitle() == null
                || todo.getTitle().trim().isEmpty()) return null;

        String normalizedTitle = todo.getTitle().trim();

        User existUser = null;
        if (userService.getAll().contains(todo.getOwner())) {
            existUser = todo.getOwner();
        }
        if (existUser == null) return null;

        if (existUser.getMyTodos() != null) {
            for (ToDo existingTodo : existUser.getMyTodos()) {
                if (existingTodo.getTitle().equalsIgnoreCase(normalizedTitle)) {
                    return existingTodo;
                }
            }
        }
        return null;

    }

    public void deleteTodo(ToDo todo) {
        if (todo == null) return;
        if (todo.getOwner() == null || todo.getTitle() == null) return;

        User existUser = null;
        if (userService.getAll().contains(todo.getOwner())) {
            existUser = todo.getOwner();
        }
        if (existUser == null) return;

        if (existUser.getMyTodos() != null) {
            existUser.getMyTodos().removeIf(existTodo ->
                    existTodo.getTitle().equalsIgnoreCase(todo.getTitle())
            );
        }
    }

    public List<ToDo> getAll() {
        List<ToDo> allTodos = new ArrayList<>();
        List<User> allUsers = userService.getAll();

        for(User as : allUsers){
            if(as.getMyTodos() != null){
                allTodos.addAll(as.getMyTodos());
            }
        }
        return allTodos;
    }

    public List<ToDo> getByUser(User user) {
        if (user == null) return Collections.emptyList();
        if (user.getMyTodos() == null) return Collections.emptyList();
        return new ArrayList<>(user.getMyTodos());
    }

    public ToDo getByUserTitle(User user, String title) {
        if(user == null
                || title == null
                || title.isEmpty()) return null;

        if (userService.getAll().contains(user)) {
            List<ToDo> usersTodo = user.getMyTodos();
            if (usersTodo == null) return null;
            for(ToDo t : usersTodo){
                if(t.getTitle().equalsIgnoreCase(title.trim())){
                    return t;
                }
            }
        }
        return null;
    }
}
