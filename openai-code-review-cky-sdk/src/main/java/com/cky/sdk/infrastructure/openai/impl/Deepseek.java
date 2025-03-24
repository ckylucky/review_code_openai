package com.cky.sdk.infrastructure.openai.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cky.sdk.domain.model.Model;
import com.cky.sdk.infrastructure.openai.IOpenAI;
import com.cky.sdk.infrastructure.openai.dto.ChatCompletionRequestDTO;
import com.cky.sdk.infrastructure.openai.dto.ChatCompletionSyncResponseDTO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * @ClassName Deepseek
 * @Description
 * @Author lukcy
 * @Date 2025/3/24 8:57
 * @Version 1.0
 */
public class Deepseek implements IOpenAI {
    @Override
    public ChatCompletionSyncResponseDTO completions(ChatCompletionRequestDTO requestDTO) throws Exception {
        // 📞 联系街边小吃摊（不需要会员卡）
        URL url = new URL("http://http://02dedbf1af8e41799911-deepseek-r1-llm-api.gcs-xy1a.jdcloud.com/api/chat"); // ▶️ 修改点1：换地址
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json"); // ▶️ 移除认证头
        connection.setConnectTimeout(30000);   // ▶️ 新增：30秒连接超时
        connection.setReadTimeout(120000);    // ▶️ 新增：2分钟等待超时
        connection.setDoOutput(true);

        // 📝 填写小吃摊专用订单单
        JSONObject ollamaOrder = new JSONObject();
        ollamaOrder.put("model", Model.DEEPSEEK_R1_15B.getCode());             // ▶️ 新增：必须指定模型名称
        ollamaOrder.put("messages", requestDTO.getMessages()); // 复用原有消息
        ollamaOrder.put("stream", false);                // ▶️ 新增：关闭流式传输

        try (OutputStream os = connection.getOutputStream()) {
            os.write(ollamaOrder.toString().getBytes(StandardCharsets.UTF_8)); // ▶️ 修改请求体结构
        }

        // 🚨 处理可能出错的订单
        if (connection.getResponseCode() >= 400) { // ▶️ 新增：错误处理
            throw new IOException("订单出错：" + connection.getResponseMessage());
        }

        // 🛵 接收小吃摊的餐盒
        StringBuilder content = new StringBuilder();
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) { // ▶️ 明确编码
            String line;
            while ((line = in.readLine()) != null) {
                content.append(line);
            }
        }

        // 📦 拆解小吃摊的特殊包装
        JSONObject ollamaResponse = JSON.parseObject(content.toString());
        ChatCompletionSyncResponseDTO response = new ChatCompletionSyncResponseDTO();

        // 🥡 把小吃摊的饭盒装进高级餐盘
        ChatCompletionSyncResponseDTO.Choice choice = new ChatCompletionSyncResponseDTO.Choice();
        JSONObject message = ollamaResponse.getJSONObject("message"); // ▶️ 响应结构不同
        choice.getMessage().setRole(message.getString("role"));
        choice.getMessage().setContent(message.getString("content"));

        response.setChoices(Collections.singletonList(choice)); // 保持原有返回结构
        return response;
    }

}
