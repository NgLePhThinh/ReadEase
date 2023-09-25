package com.ReadEase.ReadEase.Model;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="note")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Note {
    @Id
    @GeneratedValue
    private int ID;
    @Column(nullable = false,length = 1024)
    private String content;
    @Column(nullable = false, length = 1024)
    private String position;
}
