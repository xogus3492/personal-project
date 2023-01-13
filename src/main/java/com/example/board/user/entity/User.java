package com.example.board.user.entity;

import com.example.board.board.entity.Board;
import com.example.board.comment.entity.Comment;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "user_table")
public class User {

    @Id @GeneratedValue
    @Column(name = "user_id")
    private Long id;

    @Column(name = "email", length = 45)
    private String email;

    @Column(name = "password", length = 45)
    private String password;
    @Column(name = "name", length = 45)
    private String name;

    @CreatedDate
    @Column(name = "join_date")
    private LocalDateTime joinDate;

    @OneToMany(mappedBy = "user")
    private List<Board> boardList = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Comment> commentList = new ArrayList<>();
}
