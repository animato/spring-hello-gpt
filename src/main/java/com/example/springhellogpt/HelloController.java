package com.example.springhellogpt;

import com.openai.client.OpenAIClient;
import com.openai.models.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
public class HelloController {

    public static final String OPENAI_MODEL_NAME = "gpt-3.5-turbo";

    private final OpenAIClient openAIClient;

    public HelloController(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
    }

    @GetMapping("/hello")
    public String hello(@RequestParam String q) {
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(OPENAI_MODEL_NAME)
                .addSystemMessage("You are a helpful assistant.")
                .addUserMessage(q)
                .build();

        CompletableFuture<ChatCompletion> chatCompletion = openAIClient.async().chat().completions().create(params);

        return chatCompletion.join().choices().getFirst().message().content().orElse("응답이 없습니다.");
    }
}
