package com.ReadEase.ReadEase.Model;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
//@Entity
@Builder
public class AccessToken {
    private String accessToken;
    private Long expriedAt;
    private Date createAt;
}
