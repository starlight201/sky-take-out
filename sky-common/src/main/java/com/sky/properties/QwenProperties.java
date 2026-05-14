package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.qwen")
@Data
public class QwenProperties {

    /**
     * DashScope API Key。
     * 配置文件中使用 ${DASHSCOPE_API_KEY:} 读取环境变量，避免泄露密钥。
     */
    private String apiKey;

    /**
     * OpenAI 兼容接口地址，例如 https://dashscope.aliyuncs.com/compatible-mode/v1。
     */
    private String baseUrl;

    /**
     * 千问模型名称，例如 qwen-plus。
     */
    private String model;

    /**
     * 是否启用千问调用。关闭后会走本地降级回答。
     */
    private Boolean enabled;

    /**
     * Boolean 包装类型可能为 null，这里统一转换成安全的 boolean。
     */
    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }
}
