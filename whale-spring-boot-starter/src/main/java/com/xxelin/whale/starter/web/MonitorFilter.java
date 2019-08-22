package com.xxelin.whale.starter.web;

import com.xxelin.whale.core.MonitorHolder;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: MonitorFilter.java , v 0.1 2019-08-22 11:17 ElinZhou Exp $
 */
public class MonitorFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) {
        //do nothing
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String requestURI = request.getRequestURI();
        if (requestURI.contains("/whale/monitor/desc")) {
            String desc = MonitorHolder.desc();
            if (StringUtils.isEmpty(desc)) {
                desc = "";
            }
            response.getWriter().write(desc);
        }
    }

    @Override
    public void destroy() {
        //do nothing
    }
}
