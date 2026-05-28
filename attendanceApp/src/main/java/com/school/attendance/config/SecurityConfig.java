package com.school.attendance.config;

import com.school.attendance.security.JwtAuthenticationFilter;
import com.school.attendance.tenant.TenantRequestFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final TenantRequestFilter tenantRequestFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          TenantRequestFilter tenantRequestFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.tenantRequestFilter = tenantRequestFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "http://192.168.*.*:3000",
                "https://portal.vidyasetu.co"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-School-Id", "Accept", "Origin"));
        configuration.setExposedHeaders(List.of("Authorization", "X-School-Id"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/**", "/hello/**", "/pilot-onboarding/**", "/school-registration/**", "/error").permitAll()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(tenantRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
