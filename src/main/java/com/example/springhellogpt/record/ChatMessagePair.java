package com.example.springhellogpt.record;

import com.openai.models.ChatCompletionAssistantMessageParam;
import com.openai.models.ChatCompletionUserMessageParam;

public record ChatMessagePair(ChatCompletionUserMessageParam user, ChatCompletionAssistantMessageParam assistant) {}
