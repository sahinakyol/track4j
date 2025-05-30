package io.track4j.beans;

import io.track4j.autoconfigure.Track4jServiceManager;
import io.track4j.interceptor.RestTemplateTrackingInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class RestTemplateBeanPostProcessor implements BeanPostProcessor {

    private final RestTemplateTrackingInterceptor interceptor = Track4jServiceManager.getInstance().getRestTemplateTrackingInterceptor();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof RestTemplate) {
            RestTemplate restTemplate = (RestTemplate) bean;
            List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(restTemplate.getInterceptors());
            if (!interceptors.contains(interceptor)) {
                interceptors.add(interceptor);
                restTemplate.setInterceptors(interceptors);
            }
        }
        return bean;
    }
}