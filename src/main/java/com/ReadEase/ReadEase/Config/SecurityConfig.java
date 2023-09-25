package com.ReadEase.ReadEase.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http.csrf( csrf -> csrf.disable());
        http
                .authorizeHttpRequests(
                        (requests) -> requests
                                .requestMatchers("/**")
                                .permitAll()
                                .anyRequest()
                                .authenticated()
                )
//            .sessionManagement(
//                    (session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        ;

        return http.build();


    }
}
