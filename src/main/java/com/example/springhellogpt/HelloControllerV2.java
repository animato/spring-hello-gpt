package com.example.springhellogpt;

import com.openai.client.OpenAIClient;
import com.openai.models.ChatCompletion;
import com.openai.models.ChatCompletionAssistantMessageParam;
import com.openai.models.ChatCompletionCreateParams;
import com.openai.models.ChatCompletionUserMessageParam;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class HelloControllerV2 {

    public static final String OPENAI_MODEL_NAME = "gpt-3.5-turbo";

    private final OpenAIClient openAIClient;

    private final ConcurrentHashMap<SessionId, List<ChatMessagePair>> sessionChatMsg = new ConcurrentHashMap<>();

    public HelloControllerV2(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
    }

    @GetMapping("/hello-multiturn")
    public String hello(HttpSession session, @RequestParam String q, @RequestParam(defaultValue = "true") Boolean multiTurn) {
        SessionId sessionId = new SessionId(session.getId());

        List<ChatMessagePair> list = (multiTurn) ? sessionChatMsg.getOrDefault(sessionId, new ArrayList<>()) : new ArrayList<>();

        ChatCompletionCreateParams.Builder builder = ChatCompletionCreateParams.builder();
        builder.model(OPENAI_MODEL_NAME).addSystemMessage("You are a helpful assistant.");

        list.forEach(pair -> {
            builder.addMessage(pair.user());
            builder.addMessage(pair.assistant());
        });

        ChatCompletionUserMessageParam user = ChatCompletionUserMessageParam.builder().content(q).build();

        ChatCompletionCreateParams params = builder.addMessage(user).build();

        ChatCompletion completion = openAIClient.async().chat().completions().create(params).join();
        String message = completion.choices().getFirst().message().content().orElse("");

        ChatCompletionAssistantMessageParam assistant = ChatCompletionAssistantMessageParam.builder().content(message).build();

        if (multiTurn) {
            list.add(new ChatMessagePair(user, assistant));
            sessionChatMsg.put(sessionId, list);
            list.forEach(System.out::println);
        }

        return message;
    }

    public record SessionId(String id) {}
    public record ChatMessagePair(ChatCompletionUserMessageParam user, ChatCompletionAssistantMessageParam assistant) {}
}
