package com.teachingtool.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
@Configuration
public class OrderConfiguration {
    @Bean
    public MessageConverter messageConverter(){

        return new Jackson2JsonMessageConverter();
    }
}

