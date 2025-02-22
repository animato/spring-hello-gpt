package com.example.springhellogpt;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfiguration {
    @Value("${openai.key}")
    private String apiKey;

    @Bean
    public OpenAIClient getOpenAIClient() {
        return OpenAIOkHttpClient.builder().apiKey(apiKey).build();
    }
}
