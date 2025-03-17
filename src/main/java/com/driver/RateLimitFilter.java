package com.driver;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bandwidth;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;

@Component
public class RateLimitFilter implements Filter {
    // In-memory rate limiters (per IP address)
    private final Bucket bucket;

    public RateLimitFilter() {
        this.bucket = Bucket.builder()
                .addLimit(Bandwidth.simple(5, Duration.ofMinutes(1))) // Replaced deprecated methods
                .build();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();

        // Apply rate limiting only to specific endpoints
        if (requestURI.startsWith("/admin") || requestURI.startsWith("/customer") || requestURI.startsWith("/driver")) {
            if (bucket.tryConsume(1)) {
                chain.doFilter(request, response);
            } else {
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                httpResponse.getWriter().write("Rate limit exceeded. Try again later.");
            }
        } else {
            chain.doFilter(request, response);  // No rate limiting for other URLs
        }
    }
}
