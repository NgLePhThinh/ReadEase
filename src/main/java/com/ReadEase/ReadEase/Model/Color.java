package com.ReadEase.ReadEase.Model;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="color", indexes = {
        @Index(name = "colorHexCode", columnList = "colorHexCode")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Color {
    @Id
    @GeneratedValue
    private int ID;
    @Column(nullable = false, length = 7)
    private String colorHexCode;
}
