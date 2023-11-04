package com.ReadEase.ReadEase.Config;

import jakarta.servlet.*;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ConfigFilterChain implements Filter {
    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain filterChain) throws IOException, ServletException {
        System.out.println(response.toString());
        filterChain.doFilter(request,response);
    }
}
