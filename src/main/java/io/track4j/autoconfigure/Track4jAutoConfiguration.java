package io.track4j.autoconfigure;

import io.track4j.annotation.conditions.ConditionalOnTrack4jExternalEnabled;
import io.track4j.annotation.conditions.ConditionalOnTrack4jIncomingEnabled;
import io.track4j.annotation.conditions.ConditionalOnTrack4jInternalEnabled;
import io.track4j.aspect.InternalServiceTrackingAspect;
import io.track4j.beans.RestTemplateBeanPostProcessor;
import io.track4j.filter.IncomingRequestTrackingFilter;
import io.track4j.properties.Track4jProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

public class Track4jAutoConfiguration {

    @PostConstruct
    void initialize() {
        Track4jServiceManager.initialize();
    }

    @PreDestroy
    void shutdown() {
        Track4jServiceManager.shutdown();
    }

    @Bean
    @ConditionalOnTrack4jInternalEnabled
    public InternalServiceTrackingAspect internalServiceTrackingAspect() {
        return new InternalServiceTrackingAspect();
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnTrack4jIncomingEnabled
    public FilterRegistrationBean<IncomingRequestTrackingFilter> incomingRequestFilter() {
        Track4jProperties properties = Track4jServiceManager.getInstance().getProperties();

        FilterRegistrationBean<IncomingRequestTrackingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new IncomingRequestTrackingFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(properties.getFilterOrder());
        registrationBean.setEnabled(properties.isIncomingRequestTrackingEnabled());

        return registrationBean;
    }

    @Bean
    @ConditionalOnClass(RestTemplate.class)
    @ConditionalOnTrack4jExternalEnabled
    public BeanPostProcessor restTemplateExternalRequestFilter() {
        return new RestTemplateBeanPostProcessor();
    }
}