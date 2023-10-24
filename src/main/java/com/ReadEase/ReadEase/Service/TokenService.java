package com.ReadEase.ReadEase.Service;

import com.ReadEase.ReadEase.Model.Token;
import com.ReadEase.ReadEase.Repo.TokenRepo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final TokenRepo tokenRepo;

    public String getUserID(HttpServletRequest req){
        final String authHeader = req.getHeader("Authorization");
        final String jwt;
        jwt = authHeader.substring(7);

        return tokenRepo.findUserIDByToken(jwt);
    }

}
