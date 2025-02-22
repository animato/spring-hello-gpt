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
        /*
         * ChatCompletion 에서 Role의 이해
         *
         * system: 개발자가 지시하는 전역적 지침, 새로운 모델에서는 system 대신 developer 로 대체될 수 있다.
         * user: 사용자가 전달하는 메시지
         * assistant: 모델이 이전에 어떤 답변을 했는지(또는 어떻게 답변해야 하는지)를 참고하게 하는 메시지.
         *            현재 예제는 한 번의 요청/응답만 처리하므로 assistant 메시지는 사용하지 않음.
         *            대화 맥락을 유지하려면 이전 응답을 assistant 메시지로 추가해야 한다.
         */
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(OPENAI_MODEL_NAME)
                .addSystemMessage("You are a helpful assistant.")
                .addUserMessage(q)
                .build();

        CompletableFuture<ChatCompletion> chatCompletion = openAIClient.async().chat().completions().create(params);

        return chatCompletion.join().choices().getFirst().message().content().orElse("응답이 없습니다.");
    }
}
