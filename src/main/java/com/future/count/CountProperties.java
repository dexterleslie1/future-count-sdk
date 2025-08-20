package com.future.count;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "spring.future.count")
@Data
public class CountProperties {
    private String host = "localhost";
    private int port = 8080;
    /*@NotEmpty(message = "请指定服务支持的flag列表")*/
    private List<String> flagList;
}
