package com.usermanagement.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter that prevents browsers and intermediary proxies from caching authenticated pages.
 * Essential for preventing back-button information disclosure after user logout.
 */
public class NoCacheFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // HTTP 1.1 Cache-Control
        httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0");
        // HTTP 1.0 Pragma
        httpResponse.setHeader("Pragma", "no-cache");
        // Proxies
        httpResponse.setDateHeader("Expires", 0);

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
