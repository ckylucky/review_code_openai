package com.cky.sdk;

import com.alibaba.fastjson2.JSONObject;
import com.cky.sdk.infrastructure.openai.dto.ChatCompletionSyncResponseDTO;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @ClassName TEST1
 * @Description
 * @Author lukcy
 * @Date 2025/3/24 11:44
 * @Version 1.0
 */
public class TEST1 {
    @Test
    public void testChoiceInitialization() {
        ChatCompletionSyncResponseDTO.Choice choice = new ChatCompletionSyncResponseDTO.Choice();
        assertNotNull(choice.getMessage()); // ✅ 验证 message 字段已初始化
    }
    @Test(expected = RuntimeException.class)
    public void testInvalidApiResponse() {
        // 模拟缺失 role 字段的响应
        JSONObject invalidResponse = new JSONObject();
        invalidResponse.put("message", new JSONObject().put("content", "test"));
        // 应抛出 RuntimeException
    }

}
