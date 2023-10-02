package com.ReadEase.ReadEase.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@Entity
@Builder
public class GGAccessToken {
    @Id
    @GeneratedValue
    private int id;
    private String accessToken;
    private Date expriedAt;
}
