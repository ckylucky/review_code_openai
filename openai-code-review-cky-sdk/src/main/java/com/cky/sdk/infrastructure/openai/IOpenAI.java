package com.cky.sdk.infrastructure.openai;


import com.cky.sdk.infrastructure.openai.dto.ChatCompletionRequestDTO;
import com.cky.sdk.infrastructure.openai.dto.ChatCompletionSyncResponseDTO;

public interface IOpenAI {

    ChatCompletionSyncResponseDTO completions(ChatCompletionRequestDTO requestDTO) throws Exception;

}
