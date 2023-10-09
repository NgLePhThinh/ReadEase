package com.ReadEase.ReadEase.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="role")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Role {
    @Id
    private int ID;
    @Column(nullable = false)
    private String role;

}
