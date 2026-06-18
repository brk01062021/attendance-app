package com.school.attendance.security;

import com.school.attendance.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String username = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token);
            String schoolId = jwtUtil.extractSchoolId(token);

            if (username != null && jwtUtil.isTokenValid(token)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                TenantContext.setSchoolId(schoolId);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + SecurityAccess.normalizeRole(role)))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception e) {
            /*
             * Do not hard-fail permitAll/local MVP endpoints when the browser has an
             * old or invalid JWT in localStorage. Some Day 4 web calls, including
             * multipart finance uploads, may still include a stale Authorization
             * header after credentials/role changes. Clearing the security context and
             * continuing lets controller-level tenant validation handle the request
             * instead of returning an empty 401 before the controller is reached.
             */
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
