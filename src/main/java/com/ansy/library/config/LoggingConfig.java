package com.ansy.library.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import java.io.IOException;

@Configuration
public class LoggingConfig {

    private static final Logger log = LoggerFactory.getLogger(LoggingConfig.class);

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludePayload(true);
        loggingFilter.setMaxPayloadLength(10000);
        loggingFilter.setIncludeHeaders(true);
        loggingFilter.setAfterMessagePrefix("HTTP REQUEST: ");
        return loggingFilter;
    }

    @Bean
    public Filter responseLoggingFilter() {
        return new Filter() {
            @Override
            public void init(FilterConfig filterConfig) throws ServletException {
                // Initialization code if needed
            }

            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {

                if (!(request instanceof HttpServletRequest httpRequest)) {
                    chain.doFilter(request, response);
                    return;
                }

                long startTime = System.currentTimeMillis();
                String method = httpRequest.getMethod();
                String uri = httpRequest.getRequestURI();

                try {
                    chain.doFilter(request, response);
                } finally {
                    if (response instanceof HttpServletResponse httpResponse) {
                        int status = httpResponse.getStatus();
                        long duration = System.currentTimeMillis() - startTime;

                        log.info("{} {} - Status: {} ({}ms)", method, uri, status, duration);
                    }
                }
            }

            @Override
            public void destroy() {
                // Cleanup code if needed
            }
        };
    }
}
