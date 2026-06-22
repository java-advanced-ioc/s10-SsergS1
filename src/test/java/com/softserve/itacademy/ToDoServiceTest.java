package com.softserve.itacademy;

import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.service.ToDoService;
import com.softserve.itacademy.service.UserService;
import com.softserve.itacademy.service.impl.ToDoServiceImpl;
import com.softserve.itacademy.service.impl.UserServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class ToDoServiceTest {

    private AnnotationConfigApplicationContext context;
    private ToDoService toDoService;
    private UserService userService;

    private User createAndAddUser(String email, String firstName, String lastName) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        userService.addUser(user);
        return user;
    }

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext();
        context.register(UserServiceImpl.class);
        context.register(ToDoServiceImpl.class);
        context.refresh();
        userService = context.getBean(UserService.class);
        toDoService = context.getBean(ToDoService.class);
    }

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void addTodo_shouldAttachToOwner_andBeReturnedByGetByUser() {
        User user = new User();
        createAndAddUser("test@test.com","Test","User");
        user.setEmail("test@test.com");
        user.setFirstName("Test");
        user.setLastName("User");
        userService.addUser(user);

        ToDo todo = new ToDo();
        todo.setTitle("Work");

        ToDo added = toDoService.addTodo(user, todo);

        assertNotNull(added);
        assertTrue(toDoService.getByUser(user).contains(added));
        assertNotNull(added.getCreatedAt());
        assertEquals(user, added.getOwner());
    }

    @Test
    void addTodo_shouldReturnNull_whenInvalid() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setFirstName("Test");
        user.setLastName("User");
        userService.addUser(user);

        ToDo todo = new ToDo();
        todo.setTitle("Work");


        assertNull(toDoService.addTodo(null, todo));
        assertNull(toDoService.addTodo(user, null));

        ToDo todoNullTitle = new ToDo();
        todoNullTitle.setTitle(null);
        assertNull(toDoService.addTodo(user, todoNullTitle));

        ToDo todoEmptyTitle = new ToDo();
        todoEmptyTitle.setTitle("");
        assertNull(toDoService.addTodo(user, todoEmptyTitle));

        ToDo todoSpacesTitle = new ToDo();
        todoSpacesTitle.setTitle("   ");
        assertNull(toDoService.addTodo(user, todoSpacesTitle));

        User nonExistedUser = new User();
        nonExistedUser.setEmail("nonexistent@test.com");
        nonExistedUser.setFirstName("Non");
        nonExistedUser.setLastName("Existent");

        assertNull(toDoService.addTodo(nonExistedUser, todo));
    }

    @Test
    void addTodo_shouldEnforceUniqueTitle_perOwner() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setFirstName("Test");
        user.setLastName("User");
        userService.addUser(user);

        ToDo todo1 = new ToDo();
        todo1.setTitle("Work");
        ToDo added1 = toDoService.addTodo(user, todo1);
        assertNotNull(added1);

        ToDo todo2 = new ToDo();
        todo2.setTitle("Work");
        ToDo added2 = toDoService.addTodo(user, todo2);
        assertNull(added2);

        assertEquals(1, user.getMyTodos().size());

        User user2 = new User();
        user2.setEmail("test2@test.com");
        user2.setFirstName("Test2");
        user2.setLastName("User2");
        userService.addUser(user2);

        ToDo todo3 = new ToDo();
        todo3.setTitle("Work");
        ToDo added3 = toDoService.addTodo(user2, todo3);
        assertNotNull(added3);

        assertEquals(1, user2.getMyTodos().size());
    }

    @Test
    void updateTodo_shouldReplaceExistingByTitleWithinOwner() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setFirstName("Test");
        user.setLastName("User");
        userService.addUser(user);

        ToDo todo1 = new ToDo();
        todo1.setTitle("Old");
        ToDo addedTodo = toDoService.addTodo(user, todo1);
        assertNotNull(addedTodo);

        ToDo updateTodo = new ToDo();
        updateTodo.setOwner(user);
        updateTodo.setTitle("Old");

        ToDo result = toDoService.updateTodo(updateTodo);

        assertNotNull(result);
        assertEquals("Old", result.getTitle());
        assertNotNull(toDoService.getByUserTitle(user, "Old"));
    }

    @Test
    void updateTodo_shouldReturnNull_whenNotFound_orInvalid() {

        assertNull(toDoService.updateTodo(null));

        ToDo todoNoOwner = new ToDo();
        todoNoOwner.setTitle("Some");
        assertNull(toDoService.updateTodo(todoNoOwner));

        ToDo todoNullTitle = new ToDo();
        todoNullTitle.setOwner(new User());
        todoNullTitle.setTitle(null);
        assertNull(toDoService.updateTodo(todoNullTitle));

        ToDo todoEmptyTitle = new ToDo();
        todoEmptyTitle.setOwner(new User());
        todoEmptyTitle.setTitle("");
        assertNull(toDoService.updateTodo(todoEmptyTitle));

        ToDo todoSpacesTitle = new ToDo();
        todoSpacesTitle.setOwner(new User());
        todoSpacesTitle.setTitle("   ");
        assertNull(toDoService.updateTodo(todoSpacesTitle));

        User user = new User();
        user.setEmail("test@test.com");
        user.setFirstName("Test");
        user.setLastName("User");
        userService.addUser(user);

        ToDo absentTodo = new ToDo();
        absentTodo.setOwner(user);
        absentTodo.setTitle("Absent");

        assertNull(toDoService.updateTodo(absentTodo));
    }

    @Test
    void deleteTodo_shouldRemoveFromOwnerList() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setFirstName("Test");
        user.setLastName("User");
        userService.addUser(user);

        ToDo todo = new ToDo();
        todo.setTitle("Work");
        ToDo added1 = toDoService.addTodo(user, todo);
        assertEquals(1, user.getMyTodos().size());

        toDoService.deleteTodo(todo);

        assertEquals(0, user.getMyTodos().size());
        assertNull(toDoService.getByUserTitle(user, "Work"));
    }

    @Test
    void getAll_shouldReturnCopy() {
        User user1 = new User();
        user1.setEmail("test1@test.com");
        user1.setFirstName("Test1");
        user1.setLastName("User1");
        userService.addUser(user1);

        User user2 = new User();
        user2.setEmail("test2@test.com");
        user2.setFirstName("Test2");
        user2.setLastName("User2");
        userService.addUser(user2);

        ToDo todo1 = new ToDo();
        todo1.setTitle("Work1");
        toDoService.addTodo(user1, todo1);

        ToDo todo2 = new ToDo();
        todo2.setTitle("Work2");
        toDoService.addTodo(user2, todo2);

        List<ToDo> allTodos = toDoService.getAll();
        int sizeBefore = allTodos.size();
        assertEquals(2, sizeBefore);

        allTodos.clear();

        List<ToDo> allTodosAgain = toDoService.getAll();
        assertEquals(sizeBefore, allTodosAgain.size());
        assertEquals(2, allTodosAgain.size());
    }

    @Test
    void getByUser_shouldReturnCopy_orEmptyWhenNone() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setFirstName("Test");
        user.setLastName("User");
        userService.addUser(user);

        ToDo todo1 = new ToDo();
        todo1.setTitle("Work1");
        toDoService.addTodo(user, todo1);

        ToDo todo2 = new ToDo();
        todo2.setTitle("Work2");
        toDoService.addTodo(user, todo2);

        List<ToDo> firstList = toDoService.getByUser(user);
        int sizeBefore = firstList.size();
        assertEquals(2, sizeBefore);

        firstList.clear();

        List<ToDo> secondList = toDoService.getByUser(user);
        assertEquals(sizeBefore, secondList.size());
        assertEquals(2, secondList.size());

        assertTrue(toDoService.getByUser(null).isEmpty());

        User user2 = new User();
        user2.setEmail("test2@test2.com");
        user2.setFirstName("Test2");
        user2.setLastName("User2");
        userService.addUser(user2);

        assertTrue(toDoService.getByUser(user2).isEmpty());
    }

    @Test
    void getByUserTitle_shouldReturnToDo_orNull() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setFirstName("Test");
        user.setLastName("User");
        userService.addUser(user);

        ToDo todo = new ToDo();
        todo.setTitle("Work");
        ToDo added = toDoService.addTodo(user, todo);
        assertNotNull(added);

        assertEquals(todo, toDoService.getByUserTitle(user, "Work"));
        assertEquals(todo, toDoService.getByUserTitle(user, "WORK"));
        assertNull(toDoService.getByUserTitle(user, "Absent"));

        assertNull(toDoService.getByUserTitle(null, "Work"));
        assertNull(toDoService.getByUserTitle(user, null));
        assertNull(toDoService.getByUserTitle(user, ""));
        assertNull(toDoService.getByUserTitle(user, "  "));

    }
}
