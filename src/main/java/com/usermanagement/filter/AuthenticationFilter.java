package com.usermanagement.filter;

import com.usermanagement.model.ApiResponse;
import com.usermanagement.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Enforces session-based authentication across all protected pages and API resources.
 */
public class AuthenticationFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Whitelisted URL prefixes accessible without an active session
    private static final List<String> PUBLIC_PATH_PREFIXES = Arrays.asList(
            "/login.jsp",
            "/register.jsp",
            "/api/auth/login",
            "/api/auth/register",
            "/css/",
            "/js/",
            "/images/",
            "/favicon.ico"
    );

    @Override
    public void init(FilterConfig filterConfig) {
        logger.info("AuthenticationFilter initialized.");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String contextPath = httpRequest.getContextPath();
        String requestUri = httpRequest.getRequestURI();
        String relativePath = requestUri.substring(contextPath.length());

        // Redirect root context "/" to login.jsp
        if (relativePath.equals("") || relativePath.equals("/")) {
            HttpSession rootSession = httpRequest.getSession(false);
            if (rootSession != null && rootSession.getAttribute(AuthService.SESSION_USER_KEY) != null) {
                httpResponse.sendRedirect(contextPath + "/home.jsp");
            } else {
                httpResponse.sendRedirect(contextPath + "/login.jsp");
            }
            return;
        }

        // Allow public resources through without checking session
        if (isPublicPath(relativePath)) {
            // If already logged in and visiting login.jsp, redirect straight to home.jsp
            if (relativePath.equals("/login.jsp") || relativePath.equals("/register.jsp")) {
                HttpSession session = httpRequest.getSession(false);
                if (session != null && session.getAttribute(AuthService.SESSION_USER_KEY) != null) {
                    httpResponse.sendRedirect(contextPath + "/home.jsp");
                    return;
                }
            }
            chain.doFilter(request, response);
            return;
        }

        // Verify active session
        HttpSession session = httpRequest.getSession(false);
        boolean isLoggedIn = (session != null && session.getAttribute(AuthService.SESSION_USER_KEY) != null);

        if (isLoggedIn) {
            chain.doFilter(request, response);
        } else {
            logger.debug("Unauthorized access attempted to: {}", relativePath);
            boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(httpRequest.getHeader("X-Requested-With"))
                    || relativePath.startsWith("/api/");

            if (isAjax) {
                // Return HTTP 401 Unauthorized for Ajax / REST calls
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setContentType("application/json;charset=UTF-8");
                ApiResponse<Void> apiResp = ApiResponse.error("Session expired or unauthorized. Please log in.");
                httpResponse.getWriter().write(objectMapper.writeValueAsString(apiResp));
            } else {
                // Redirect standard web browser requests cleanly to login.jsp
                String redirectUrl = contextPath + "/login.jsp";
                if (httpRequest.getRequestedSessionId() != null && !httpRequest.isRequestedSessionIdValid()) {
                    redirectUrl += "?sessionExpired=true";
                }
                httpResponse.sendRedirect(redirectUrl);
            }
        }
    }

    private boolean isPublicPath(String path) {
        for (String prefix : PUBLIC_PATH_PREFIXES) {
            if (path.startsWith(prefix) || path.equalsIgnoreCase(prefix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {
    }
}
