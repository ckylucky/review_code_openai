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
        ChatCompletionRequestDTO chatCompletionRequest = new ChatCompletionRequestDTO();
        chatCompletionRequest.setModel(Model.DEEPSEEK_R1_15B.getCode());
        chatCompletionRequest.setMessages(new ArrayList<ChatCompletionRequestDTO.Prompt>() {
            private static final long serialVersionUID = -7988151926241837899L;
            {
                {
                    add(new ChatCompletionRequestDTO.Prompt("user", String.format(
                            "你是一位资深代码评审专家，请严格按照以下模板分析这段代码差异：\n\n" +
                                    "### 代码评分（0-100）\n" +
                                    "{评分}\n\n" +
                                    "### 主要问题\n" +
                                    "- [问题类型] 问题描述\n\n" +
                                    "### 优化建议\n" +
                                    "- 具体修改建议\n\n" +
                                    "### 示例代码\n" +
                                    "```diff\n%s\n```\n\n" +
                                    "要求：\n" +
                                    "1. 用中文输出\n" +
                                    "2. 使用Markdown格式\n" +
                                    "3. 重点检查安全漏洞和代码坏味道",
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
