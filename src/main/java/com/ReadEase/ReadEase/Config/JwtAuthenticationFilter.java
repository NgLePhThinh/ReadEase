package com.ReadEase.ReadEase.Config;

import com.ReadEase.ReadEase.Repo.TokenRepo;
import com.ReadEase.ReadEase.Repo.UserRepo;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final TokenRepo tokenRepo;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepo userRepo;

    @Override
    protected void doFilterInternal(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        userEmail = jwtService.extractUserEmail(jwt); //todo extract the userEmail form jwt token
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            //Kiểm tra token trong Authorization có phải là token hiện tại hay không
            String userID = userRepo.findUserIDByEmail(userEmail);
            String isValidToken = tokenRepo.findAccessTokenByUserID(userID);
            // Lỗi token không tồn tại trong csdl.
            if(isValidToken == null){
                response.setStatus(HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED); // 407
                return;
            }
            // Lỗi token của phiên đăng nhập trước đó.
            else if(!isValidToken.equals(jwt)){
                System.out.println("Token is invalid");
//                response.reset();
//                response.sendError(409,"Token is not exist");
                response.setStatus(HttpServletResponse.SC_CONFLICT); // 409
                return;
            }
            else if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request,response);
    }
}
