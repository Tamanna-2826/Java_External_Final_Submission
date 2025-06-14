/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Filter.java to edit this template
 */
package Filters;

import Entities.Users;
import Services.JwtService;
import Services.UserService;
import jakarta.inject.Inject;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.LogRecord;

/**
 *
 * @author LENOVO
 */
//@WebFilter(urlPatterns = {"/admin/*", "/uploader/*", "/user/*"})

public class JwtAuthenticationFilter implements Filter {

    @Inject
    private JwtService jwtService;

    @Inject
    private UserService userService;

    // Public paths that don't require authentication
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/login.xhtml",
            "/register.xhtml",
            "/index.xhtml",
            "/public",
            "/resources",
            "/javax.faces.resource"
    );

    public void init(FilterConfig filterConfig) throws ServletException {
        // Filter initialization
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();

        // Remove context path from URI
        String path = requestURI.substring(contextPath.length());

        // Check if path is public (doesn't require authentication)
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Extract JWT token from request
        String token = extractJwtToken(httpRequest);

        if (token == null || token.trim().isEmpty()) {
            // No token found, redirect to login
            redirectToLogin(httpResponse, contextPath);
            return;
        }

        // Validate JWT token
        if (!jwtService.isTokenValid(token)) {
            // Invalid token, redirect to login
            redirectToLogin(httpResponse, contextPath);
            return;
        }

        // Extract user information from token
        String email = jwtService.getEmailFromToken(token);
        String role = jwtService.getRoleFromToken(token);
        Long userId = jwtService.getUserIdFromToken(token);

        if (email == null || role == null || userId == null) {
            redirectToLogin(httpResponse, contextPath);
            return;
        }

        // Verify user still exists and is active
        Users user = userService.findById(userId);
        if (user == null || !user.getIsActive() || !user.getEmail().equals(email)) {
            redirectToLogin(httpResponse, contextPath);
            return;
        }

        // Check role-based access
        if (!hasRoleAccess(path, role)) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
            return;
        }

        // Set user info into request
        httpRequest.setAttribute("currentUser", user);
        httpRequest.setAttribute("userRole", role);
        httpRequest.setAttribute("jwtToken", token);

        // Update session if needed
        updateSession(httpRequest, user, token);

        // Check if token needs refresh
        if (jwtService.needsRefresh(token)) {
            refreshTokenInResponse(httpResponse, user);
        }

        chain.doFilter(request, response);
    }

    public void destroy() {
        // Filter cleanup
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(publicPath
                -> path.startsWith(publicPath) || path.equals("/") || path.isEmpty());
    }

    private String extractJwtToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            Object token = session.getAttribute("jwtToken");
            return token != null ? token.toString() : null;
        }

        return null;
    }

    private boolean hasRoleAccess(String path, String role) {
        if (path.startsWith("/admin/")) {
            return "ADMIN".equals(role) || "SUPER_ADMIN".equals(role);
        }

        if (path.startsWith("/uploader/")) {
            return "UPLOADER".equals(role) || "ADMIN".equals(role) || "SUPER_ADMIN".equals(role);
        }

        if (path.startsWith("/api/")) {
            return role != null && !role.isEmpty();
        }

        return true;
    }

    private void redirectToLogin(HttpServletResponse response, String contextPath) throws IOException {
        response.sendRedirect(contextPath + "/login.xhtml");
    }

    private void updateSession(HttpServletRequest request, Users user, String token) {
        HttpSession session = request.getSession(true);
        session.setAttribute("currentUser", user);
        session.setAttribute("jwtToken", token);
        session.setAttribute("isAuthenticated", true);
        session.setAttribute("userRole", user.getRole().name());
    }

    private void refreshTokenInResponse(HttpServletResponse response, Users user) {
        try {
            java.util.Map<String, Object> claims = new java.util.HashMap<>();
            claims.put("userId", user.getUserID());
            claims.put("email", user.getEmail());
            claims.put("role", user.getRole().name());
            claims.put("name", user.getFullName());
            claims.put("loginTime", System.currentTimeMillis());

            String newToken = jwtService.generateToken(user.getEmail(), claims);

            if (newToken != null) {
                response.setHeader("Authorization", "Bearer " + newToken);
                response.setHeader("Access-Control-Expose-Headers", "Authorization");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
