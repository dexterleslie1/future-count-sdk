package com.future.count;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "spring.future.count")
@Data
public class CountProperties {
    private String host = "localhost";
    private int port = 8080;
    /*@NotEmpty(message = "请指定服务支持的flag列表")*/
    private List<String> flagList;
}
