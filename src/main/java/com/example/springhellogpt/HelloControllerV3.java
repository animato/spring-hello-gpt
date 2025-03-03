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
public class HelloControllerV3 {

    public static final String OPENAI_MODEL_NAME = "gpt-3.5-turbo";
    public static final int CONTEXT_WINDOW_LENGTH = 3;

    private final OpenAIClient openAIClient;

    private final ConcurrentHashMap<SessionId, List<ChatMessagePair>> sessionChatMsg = new ConcurrentHashMap<>();

    public HelloControllerV3(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
    }

    @GetMapping("/multiturn-v1")
    public String hello(HttpSession session, @RequestParam String q) {
        SessionId sessionId = new SessionId(session.getId());

        List<ChatMessagePair> list = sessionChatMsg.getOrDefault(sessionId, new ArrayList<>());
        List<ChatMessagePair> recentList = new ArrayList<>(list.subList(Math.max(list.size() - CONTEXT_WINDOW_LENGTH, 0), list.size()));

        ChatCompletionCreateParams.Builder builder = ChatCompletionCreateParams.builder();
        builder.model(OPENAI_MODEL_NAME).addSystemMessage("You are a helpful assistant.");

        recentList.forEach(pair -> {
            builder.addMessage(pair.user());
            builder.addMessage(pair.assistant());
        });

        recentList.forEach(System.out::println);

        ChatCompletionUserMessageParam user = ChatCompletionUserMessageParam.builder().content(q).build();

        ChatCompletionCreateParams params = builder.addMessage(user).build();

        ChatCompletion completion = openAIClient.async().chat().completions().create(params).join();
        String message = completion.choices().getFirst().message().content().orElse("");

        ChatCompletionAssistantMessageParam assistant = ChatCompletionAssistantMessageParam.builder().content(message).build();

        list.add(new ChatMessagePair(user, assistant));
        sessionChatMsg.put(sessionId, list);

        System.out.println("==========================");
        return message;
    }

    public record SessionId(String id) {}
    public record ChatMessagePair(ChatCompletionUserMessageParam user, ChatCompletionAssistantMessageParam assistant) {}
}
