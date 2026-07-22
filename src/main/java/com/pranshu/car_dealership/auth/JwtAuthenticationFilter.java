package com.pranshu.car_dealership.auth;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Reads a Bearer token, verifies its signature, then loads the user from the
 * database so that a deleted or demoted account loses access immediately rather
 * than when its token expires.
 *
 * <p>Deliberately not a {@code @Component}: it is constructed by
 * {@code SecurityConfig} so that {@code @WebMvcTest} slices do not try to build
 * it (and its dependencies) for controller tests that disable filters anyway.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith(BEARER_PREFIX)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticateFromToken(header.substring(BEARER_PREFIX.length()), request);
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateFromToken(String token, HttpServletRequest request) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(jwtService.extractUsername(token));

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException | org.springframework.security.core.userdetails.UsernameNotFoundException exception) {
            SecurityContextHolder.clearContext();
        }
    }
}
