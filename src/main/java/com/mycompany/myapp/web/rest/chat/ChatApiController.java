package com.mycompany.myapp.web.rest.chat;

import com.mycompany.myapp.service.RAGService;
import com.mycompany.myapp.service.api.dto.*;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2024-01-08T22:40:49.631444+09:00[Asia/Tokyo]")
@RestController
@RequestMapping("${openapi.my-llm-app.base-path:/v1}")
public class ChatApiController implements ChatApi {

    private final StreamingChatLanguageModel chatClient;

    private final RAGService ragService;

    public ChatApiController(StreamingChatLanguageModel chatClient, RAGService ragService) {
        this.chatClient = chatClient;
        this.ragService = ragService;
    }

    @Override
    public SseEmitter createChatCompletion(@Valid @RequestBody CreateChatCompletionRequest createChatCompletionRequest) {
        var messages = createChatCompletionRequest
            .getMessages()
            .stream()
            .map(message -> {
                if (message instanceof ChatCompletionRequestSystemMessage systemMessage) {
                    return new SystemMessage(systemMessage.getContent());
                } else if (message instanceof ChatCompletionRequestUserMessage userMessage) {
                    return new UserMessage(userMessage.getContent());
                } else if (message instanceof ChatCompletionRequestAssistantMessage assistantMessage) {
                    return new AiMessage(assistantMessage.getContent());
                } else {
                    throw new RuntimeException("Unknown message type");
                }
            })
            .toList();

        messages = ragService.retrieveAndGeneratePrompt(messages);

        SseEmitter emitter = new SseEmitter(180000L);
        chatClient.generate(
            messages,
            new StreamingResponseHandler<AiMessage>() {
                @Override
                public void onNext(String token) {
                    try {
                        var responseDelta = new ChatCompletionStreamResponseDelta()
                            .content(token)
                            .role(ChatCompletionStreamResponseDelta.RoleEnum.ASSISTANT);
                        var choices = new CreateChatCompletionStreamResponseChoicesInner()
                            .index(0L)
                            .finishReason(null)
                            .delta(responseDelta);
                        var response = new CreateChatCompletionStreamResponse(
                            "chatcmpl-123",
                            List.of(choices),
                            System.currentTimeMillis(),
                            "gpt-3.5-turbo",
                            CreateChatCompletionStreamResponse.ObjectEnum.CHAT_COMPLETION_CHUNK
                        );
                        emitter.send(SseEmitter.event().data(response));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                }

                @Override
                public void onComplete(Response<AiMessage> response) {
                    try {
                        var streamResponse = new CreateChatCompletionStreamResponse(
                            "chatcmpl-123",
                            List.of(
                                new CreateChatCompletionStreamResponseChoicesInner()
                                    .index(0L)
                                    .finishReason(CreateChatCompletionStreamResponseChoicesInner.FinishReasonEnum.STOP)
                                    .delta(new ChatCompletionStreamResponseDelta())
                            ),
                            System.currentTimeMillis(),
                            "gpt-3.5-turbo",
                            CreateChatCompletionStreamResponse.ObjectEnum.CHAT_COMPLETION_CHUNK
                        );
                        emitter.send(SseEmitter.event().data(streamResponse));
                        emitter.complete();
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                }

                @Override
                public void onError(Throwable error) {
                    emitter.completeWithError(error);
                }
            }
        );

        return emitter;
    }
}
