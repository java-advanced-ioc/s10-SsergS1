package com.softserve.itacademy.service.impl;

import com.softserve.itacademy.model.Task;
import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.service.TaskService;
import com.softserve.itacademy.service.ToDoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class TaskServiceImpl implements TaskService {

    private final ToDoService toDoService;

    private final Map<ToDo, List<Task>> tasksByToDo = new HashMap<>();

    @Autowired
    public TaskServiceImpl(ToDoService toDoService) {
        this.toDoService = toDoService;
    }

    public Task addTask(Task task, ToDo todo) {
        if (task == null || todo == null) {
            return null;
        }
        List<Task> list = tasksByToDo.computeIfAbsent(todo, t -> new ArrayList<>());
        for (Task existingTask : list) {
            if (existingTask.getName().equalsIgnoreCase(task.getName())) {
                return null;
            }
        }
        list.add(task);
        return task;
    }


    @Override
    public Task updateTask(ToDo todo, Task task) {
        if (todo == null || task == null) return null;
        List<Task> list = tasksByToDo.get(todo);
        if (list == null) return null;
        for (int i = 0; i < list.size(); i++) {
            Task existing = list.get(i);
            if (existing.equals(task)) {
                list.set(i, task);
                return task;
            }
        }
        return null;
    }

    @Override
    public void deleteTask(ToDo todo, String name) {
        if (todo == null || name == null) return;
        List<Task> list = tasksByToDo.get(todo);
        if (list == null) return;
        list.removeIf(t -> name.equals(t.getName()));
    }

    public List<Task> getAll() {
        List<Task> result = new ArrayList<>();
        for (List<Task> list : tasksByToDo.values()) {
            result.addAll(list);
        }
        return result;
    }

    public List<Task> getByToDo(ToDo todo) {
        if (todo == null) return Collections.emptyList();
        return tasksByToDo.getOrDefault(todo, Collections.emptyList());
    }

    public Task getByToDoName(ToDo todo, String name) {
        if (todo == null || name == null) return null;
        List<Task> list = tasksByToDo.get(todo);
        if (list == null) return null;
        for (Task t : list) {
            if (name.equals(t.getName())) return t;
        }
        return null;
    }

    public Task getByUserName(User user, String name) {
        if (user == null || name == null) return null;
        for (Map.Entry<ToDo, List<Task>> entry : tasksByToDo.entrySet()) {
            ToDo todo = entry.getKey();
            if (todo.getOwner() != null && todo.getOwner().equals(user)) {
                for (Task t : entry.getValue()) {
                    if (name.equals(t.getName())) return t;
                }
            }
        }
        return null;
    }
}
