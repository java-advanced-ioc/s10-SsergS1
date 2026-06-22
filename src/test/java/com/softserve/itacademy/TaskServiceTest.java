package com.softserve.itacademy;

import com.softserve.itacademy.model.Priority;
import com.softserve.itacademy.model.Task;
import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.service.TaskService;
import com.softserve.itacademy.service.impl.TaskServiceImpl;
import com.softserve.itacademy.service.impl.ToDoServiceImpl;
import com.softserve.itacademy.service.impl.UserServiceImpl;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


class TaskServiceTest {

    private TaskService taskService;
    private AnnotationConfigApplicationContext context;

    @BeforeEach
    void setUp() {
        // Build a fresh Spring context per test to ensure isolation
        context = new AnnotationConfigApplicationContext();
        // register the services under test
        context.register(TaskServiceImpl.class);
        context.register(ToDoServiceImpl.class);
        context.register(UserServiceImpl.class);
        context.refresh();
        taskService = context.getBean(TaskService.class);
    }

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void addTask_shouldAddTaskToGivenToDo_andReturnSameTask() {
        // Arrange

        ToDo todo = new ToDo();
        Task task = new Task("Task 1", Priority.LOW);

        // Act
        Task added = taskService.addTask(task, todo);

        // Assert
        assertEquals(task, added);
        assertTrue(taskService.getByToDo(todo).contains(task));
    }

    @Test
    void addTask_shouldReturnNull_whenTaskOrTodoIsNull() {
        // Act + Assert
        assertNull(taskService.addTask(null, new ToDo()));
        assertNull(taskService.addTask(new Task("T", Priority.LOW), null));
        assertNull(taskService.addTask(null, null));
    }

    @Test
    void updateTask_shouldReplaceEqualTaskByName_andReturnUpdated() {
        // Arrange

        ToDo todo = new ToDo();
        taskService.addTask(new Task("Task 2", Priority.MEDIUM), todo);
        Task updated = new Task("Task 2", Priority.HIGH); // equals by name

        // Act
        Task result = taskService.updateTask(todo, updated);

        // Assert
        assertEquals(updated, result);
        assertTrue(taskService.getByToDo(todo).contains(updated));
    }

    @Test
    void updateTask_shouldReturnNull_whenTaskIsNullOrNotFound() {
        // Arrange

        ToDo todo = new ToDo();
        taskService.addTask(new Task("Existing", Priority.LOW), todo);

        // Act + Assert
        assertNull(taskService.updateTask(null, null));
        assertNull(taskService.updateTask(todo, null));
        assertNull(taskService.updateTask(null, new Task("Absent", Priority.HIGH)));
        assertNull(taskService.updateTask(todo, new Task("Absent", Priority.HIGH)));
    }

    @Test
    void deleteTask_inContext_shouldRemoveOnlyFromThatToDo() {
        // Arrange

        ToDo t1 = new ToDo();
        ToDo t2 = new ToDo();
        Task task = new Task("Task 3", Priority.HIGH);
        taskService.addTask(task, t1);
        taskService.addTask(task, t2);

        assertTrue(taskService.getByToDo(t1).contains(task));
        assertTrue(taskService.getByToDo(t2).contains(task));

        taskService.deleteTask(t1, task.getName());

        assertFalse(taskService.getByToDo(t1).contains(task));
        assertTrue(taskService.getByToDo(t2).contains(task));
    }

    @Test
    void getAll_shouldReturnTasksFromAllToDos() {
        // Arrange

        ToDo todo1 = new ToDo();
        ToDo todo2 = new ToDo();
        Task t1 = new Task("A", Priority.LOW);
        Task t2 = new Task("B", Priority.MEDIUM);
        Task t3 = new Task("C", Priority.HIGH);
        taskService.addTask(t1, todo1);
        taskService.addTask(t2, todo1);
        taskService.addTask(t3, todo2);

        // Act
        List<Task> all = taskService.getAll();

        // Assert
        assertEquals(3, all.size());
        assertTrue(all.containsAll(Arrays.asList(t1, t2, t3)));
    }

    @Test
    void getByToDo_shouldReturnTasksOnlyForThatToDo_orEmptyWhenNone() {
        // Arrange

        ToDo todo1 = new ToDo();
        ToDo todo2 = new ToDo();
        Task t1 = new Task("A", Priority.LOW);
        Task t2 = new Task("B", Priority.MEDIUM);
        taskService.addTask(t1, todo1);
        taskService.addTask(t2, todo1);

        // Act + Assert
        assertTrue(taskService.getByToDo(todo1).containsAll(Arrays.asList(t1, t2)));
        assertTrue(taskService.getByToDo(todo2).isEmpty());
        assertTrue(taskService.getByToDo(null).isEmpty());
    }

    @Test
    void getByToDoName_shouldFindByNameWithinToDo_orReturnNull() {
        // Arrange

        ToDo todo = new ToDo();
        Task t1 = new Task("A", Priority.LOW);
        taskService.addTask(t1, todo);

        // Act + Assert
        assertEquals(t1, taskService.getByToDoName(todo, "A"));
        assertNull(taskService.getByToDoName(todo, "B"));
        assertNull(taskService.getByToDoName(null, "A"));
        assertNull(taskService.getByToDoName(todo, null));
    }

    @Test
    void addTask_shouldNotAddDuplicateNameWithinSameToDo() {
        // Arrange

        ToDo todo = new ToDo();
        Task first = new Task("SameName", Priority.LOW);
        Task secondWithSameName = new Task("SameName", Priority.HIGH); // equals by name

        // Act
        Task added1 = taskService.addTask(first, todo);
        Task added2 = taskService.addTask(secondWithSameName, todo);

        // Assert
        assertNotNull(added1);
        assertNull(added2, "Не повинні додавати дублікат імені у межах одного ToDo");
        List<Task> list = taskService.getByToDo(todo);
        assertEquals(1, list.size());
        assertTrue(list.contains(first));
    }

    @Test
    
    void getByUserName_shouldSearchAcrossUserTodos_orReturnNull() {
        // Arrange
        User u1 = user("u1@example.com");
        User u2 = user("u2@example.com");
        ToDo t1 = new ToDo();
        ToDo t2 = new ToDo();
        t1.setOwner(u1);
        t2.setOwner(u1);
        u1.getMyTodos().add(t1);
        u1.getMyTodos().add(t2);
        Task a = new Task("Alpha", Priority.LOW);
        Task b = new Task("Beta", Priority.MEDIUM);
        taskService.addTask(a, t1);
        taskService.addTask(b, t2);

        // Act + Assert
        assertEquals(a, taskService.getByUserName(u1, "Alpha"));
        assertEquals(b, taskService.getByUserName(u1, "Beta"));
        assertNull(taskService.getByUserName(u1, "Gamma"));
        assertNull(taskService.getByUserName(u2, "Alpha"));
        assertNull(taskService.getByUserName(null, "Alpha"));
        assertNull(taskService.getByUserName(u1, null));
    }

    private static User user(String email) {
        User u = new User();
        u.setEmail(email);
        return u;
    }

}
