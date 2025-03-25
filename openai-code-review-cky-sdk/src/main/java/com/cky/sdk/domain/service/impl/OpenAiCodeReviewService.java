package com.cky.sdk.domain.service.impl;




import com.cky.sdk.domain.service.AbstractOpenAiCodeReviewService;
import com.cky.sdk.infrastructure.git.GitCommand;
import com.cky.sdk.infrastructure.openai.IOpenAI;
import com.cky.sdk.infrastructure.openai.dto.ChatCompletionRequestDTO;
import com.cky.sdk.infrastructure.openai.dto.ChatCompletionSyncResponseDTO;
import com.cky.sdk.infrastructure.weixin.WeiXin;
import com.cky.sdk.infrastructure.weixin.dto.TemplateMessageDTO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.cky.sdk.domain.model.Model;
public class OpenAiCodeReviewService extends AbstractOpenAiCodeReviewService {

    public OpenAiCodeReviewService(GitCommand gitCommand, IOpenAI openAI, WeiXin weiXin) {
        super(gitCommand, openAI, weiXin);
    }

    @Override
    protected String getDiffCode() throws IOException, InterruptedException {
        return gitCommand.diff();
    }

    @Override
    protected String codeReview(String diffCode) throws Exception {
        System.out.println(diffCode);
        ChatCompletionRequestDTO chatCompletionRequest = new ChatCompletionRequestDTO();
        chatCompletionRequest.setModel(Model.DEEPSEEK_R1_15B.getCode());
        chatCompletionRequest.setMessages(new ArrayList<ChatCompletionRequestDTO.Prompt>() {
            private static final long serialVersionUID = -7988151926241837899L;
            {
                {
                    add(new ChatCompletionRequestDTO.Prompt("user", String.format(
                            "你是一位资深的编程架构师，精通架构设计、最佳实践以及各种编程语言。请根据以下git diff记录，对代码进行全面评审，重点关注以下几点：" +
                                    "1. 代码是否符合最佳实践和设计模式？" +
                                    "2. 是否存在潜在的bug或安全隐患？" +
                                    "3. 代码的可读性和可维护性如何？" +
                                    "4. 是否有需要优化的地方，如性能或逻辑简化？代码变更如下：%s",
                            diffCode
                    )));
                    add(new ChatCompletionRequestDTO.Prompt("user", diffCode));
                }}
            });

            ChatCompletionSyncResponseDTO completions = openAI.completions(chatCompletionRequest);
            ChatCompletionSyncResponseDTO.Message message = completions.getChoices().get(0).getMessage();
            return message.getContent();
        }

        @Override
        protected String recordCodeReview(String recommend) throws Exception {
            return gitCommand.commitAndPush(recommend);
        }

        @Override
        protected void pushMessage(String logUrl) throws Exception {
            Map<String, Map<String, String>> data = new HashMap<>();
            TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.REPO_NAME, gitCommand.getProject());
            TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.BRANCH_NAME, gitCommand.getBranch());
            TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.COMMIT_AUTHOR, gitCommand.getAuthor());
            TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.COMMIT_MESSAGE, gitCommand.getMessage());
            weiXin.sendTemplateMessage(logUrl, data);
        }

    }
