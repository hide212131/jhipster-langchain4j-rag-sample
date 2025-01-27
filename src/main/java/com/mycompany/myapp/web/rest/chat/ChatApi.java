package com.mycompany.myapp.web.rest.chat;

import com.mycompany.myapp.service.api.dto.CreateChatCompletionRequest;
import com.mycompany.myapp.service.api.dto.CreateChatCompletionStreamResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2024-01-08T22:40:49.631444+09:00[Asia/Tokyo]")
@Validated
@Tag(name = "Chat", description = "Given a list of messages comprising a conversation, the model will return a response.")
public interface ChatApi {
    /**
     * POST /chat/completions : Creates a model response for the given chat conversation.
     *
     * @param createChatCompletionRequest  (required)
     * @return OK (status code 200)
     */
    @Operation(
        operationId = "createChatCompletion",
        summary = "Creates a model response for the given chat conversation.",
        tags = { "Chat" },
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "OK",
                content = {
                    @Content(mediaType = "text/event-stream", schema = @Schema(implementation = CreateChatCompletionStreamResponse.class)),
                }
            ),
        },
        security = { @SecurityRequirement(name = "ApiKeyAuth") }
    )
    @RequestMapping(
        method = RequestMethod.POST,
        value = "/chat/completions",
        produces = MediaType.TEXT_EVENT_STREAM_VALUE,
        consumes = { "application/json" }
    )
    default SseEmitter createChatCompletion(
        @Parameter(
            name = "CreateChatCompletionRequest",
            description = "",
            required = true
        ) @Valid @RequestBody CreateChatCompletionRequest createChatCompletionRequest
    ) {
        return new SseEmitter();
    }
}
