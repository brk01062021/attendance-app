package com.school.attendance.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1)
public class TenantRequestFilter extends OncePerRequestFilter {

    public static final String SCHOOL_ID_HEADER = "X-School-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String requestedSchoolId = request.getHeader(SCHOOL_ID_HEADER);
            if (requestedSchoolId == null || requestedSchoolId.isBlank()) {
                requestedSchoolId = request.getParameter("school_id");
            }
            TenantContext.setSchoolId(requestedSchoolId);
            response.setHeader(SCHOOL_ID_HEADER, TenantContext.getSchoolId());
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
