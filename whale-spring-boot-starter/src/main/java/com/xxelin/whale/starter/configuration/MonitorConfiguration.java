package com.xxelin.whale.starter.configuration;

import com.xxelin.whale.starter.web.MonitorFilter;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.DispatcherType;
import javax.servlet.Servlet;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: MonitorConfiguration.java , v 0.1 2019-08-22 11:15 ElinZhou Exp $
 */
@Configuration
@AutoConfigureAfter({WebMvcAutoConfiguration.class})
@ConditionalOnClass({Servlet.class, DispatcherServlet.class,
        WebMvcConfigurerAdapter.class})
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "whale", havingValue = "true", name = "enable")
public class MonitorConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "whale", havingValue = "true", name = "monitor")
    public FilterRegistrationBean monitorFilterConfigurer() {
        MonitorFilter filter = new MonitorFilter();
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(filter);
        List<String> urlPatterns = new ArrayList<>();
        urlPatterns.add("/whale/monitor/*");//拦截路径，可以添加多个
        registrationBean.setUrlPatterns(urlPatterns);
        registrationBean.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST));
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);//最高优先级
        return registrationBean;
    }

}
