package com.example.springhellogpt;

import com.openai.client.OpenAIClient;
import com.openai.models.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
    public String hello(HttpSession session, @RequestParam String q) {
        SessionId sessionId = new SessionId(session.getId());

        List<ChatMessagePair> list = sessionChatMsg.getOrDefault(sessionId, new ArrayList<>());

        ChatCompletionCreateParams.Builder builder = ChatCompletionCreateParams.builder();
        builder.model(OPENAI_MODEL_NAME).addSystemMessage("You are a helpful assistant.");

        list.forEach(pair -> {
            builder.addMessage(pair.user());
            builder.addMessage(pair.assistant());
        });

        ChatCompletionUserMessageParam user = ChatCompletionUserMessageParam.builder().content(q).build();

        ChatCompletionCreateParams params = builder.addMessage(user).build();

        CompletableFuture<ChatCompletion> chatCompletion = openAIClient.async().chat().completions().create(params);

        ChatCompletionMessage message = chatCompletion.join().choices().getFirst().message();

        ChatCompletionAssistantMessageParam assistant = ChatCompletionAssistantMessageParam.builder().content(message.content().orElse("")).build();

        list.add(new ChatMessagePair(user, assistant));
        sessionChatMsg.put(sessionId, list);

        list.forEach(ChatMessagePair::print);

        return message.content().orElse("응답이 없습니다.");
    }

    public record SessionId(String id) {}
    public record ChatMessagePair(ChatCompletionUserMessageParam user, ChatCompletionAssistantMessageParam assistant) {
        public void print() {
            System.out.println("user: " + user.content() + " -> assistant: " + assistant.content());
        }
    }
}
