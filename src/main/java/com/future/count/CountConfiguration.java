package com.future.count;


import com.future.common.exception.BusinessException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.annotation.Resource;

@EnableConfigurationProperties(CountProperties.class)
public class CountConfiguration {
    @Resource
    CountProperties countProperties;

    @Bean
    public CountService countService() throws BusinessException {
        return new CountService(
                countProperties.getHost(),
                countProperties.getPort(),
                countProperties.getFlagList()
        );
    }
}
