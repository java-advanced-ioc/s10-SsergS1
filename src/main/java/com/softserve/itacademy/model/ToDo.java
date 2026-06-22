package com.softserve.itacademy.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
//foreign key title + owner
@Getter
@Setter
@NoArgsConstructor
@ToString
//@EqualsAndHashCode(of = {"title", "owner"})
public class ToDo {

    private String title;
    private LocalDateTime createdAt;
    private User owner;
    private List<Task> tasks;
}
