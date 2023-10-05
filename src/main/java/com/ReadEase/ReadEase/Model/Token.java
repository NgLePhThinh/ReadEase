package com.ReadEase.ReadEase.Model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(
     name = "token", indexes = {
             @Index(name = "idx_token", columnList = "token")
     }
)
public class Token {
    @Id
    @GeneratedValue
    private int id;
    private String token;
    private Date expriedAt;
    @Enumerated(EnumType.STRING)
    private TokenType type;
    @ManyToOne
    @JoinColumn(name = "userID", nullable = false)
    private User user;


}
