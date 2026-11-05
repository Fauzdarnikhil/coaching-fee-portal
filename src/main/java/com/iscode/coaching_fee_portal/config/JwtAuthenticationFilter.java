
package com.iscode.coaching_fee_portal.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        System.out.println("JWT FILTER EXECUTED");

        String authHeader = request.getHeader("Authorization");

        System.out.println(
                "AUTH HEADER PRESENT: " + (authHeader != null)
        );

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {

            boolean valid = jwtService.validateToken(token);

            System.out.println("JWT VALID: " + valid);

            if (valid &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                Long parentId = jwtService.extractParentId(token);

                System.out.println(
                        "AUTHENTICATED PARENT ID: " + parentId
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                parentId,
                                null,
                                Collections.emptyList()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }

        } catch (Exception e) {

            e.printStackTrace();

            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
