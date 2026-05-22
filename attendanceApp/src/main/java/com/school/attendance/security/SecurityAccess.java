package com.school.attendance.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public final class SecurityAccess {

    private SecurityAccess() { }

    public static boolean hasAnyRole(Authentication authentication, String... roles) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        for (String role : roles) {
            String required = "ROLE_" + normalizeRole(role);
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                if (required.equalsIgnoreCase(authority.getAuthority())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return RoleNames.TEACHER;
        }
        return role.trim().toUpperCase();
    }
}
