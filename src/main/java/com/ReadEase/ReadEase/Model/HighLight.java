package com.ReadEase.ReadEase.Model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name="highlight")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HighLight {
    @Id
    @GeneratedValue
    private int ID;
    @Column(nullable = false, length = 1024)
    private String position;

    @ManyToOne
    @JoinColumn(name = "COLOR_ID",nullable = false)
    @OnDelete(action =  OnDeleteAction.CASCADE)
    private Color color;

}
