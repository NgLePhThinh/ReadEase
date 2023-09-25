package com.ReadEase.ReadEase.Controller.User.Request;

import com.ReadEase.ReadEase.Model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LogoutRequest {
    private String email;
    private String loginTime;

}
