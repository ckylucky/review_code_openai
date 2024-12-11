package com.cky.sdk;

import com.alibaba.fastjson2.JSON;

import com.cky.sdk.utils.BearerTokenUtils;
import com.cky.sdk.utils.WXAccessTokenUtils;
import org.junit.Test;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * @ClassName TestAPi
 * @Description TODO
 * @Author lukcy
 * @Date 2024/12/9 20:59
 * @Version 1.0
 */
public class TestAPi {

    public static void main(String[] args) {
        String apiKeySecret = "c78fbacd3e10118ad5649d7a54a3a163.UunYDBxpzeClvSKZ";
        String token = BearerTokenUtils.getToken(apiKeySecret);
        System.out.println(token);
    }
//    @Test
//    public void weixin_test(){
//        String accessToken = WXAccessTokenUtils.getAccessToken();
//        Message message=new Message();
//        message.put("reviewTime","20241211");
//        message.put("reviewFile","后续加上url地址");
//        String url=String.format("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s",accessToken);
//        sendPostRequest(url, JSON.toJSONString(message));
//    }
//    private static void sendPostRequest(String urlString, String jsonBody) {
//        try {
//            URL url = new URL(urlString);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Content-Type", "application/json; utf-8");
//            conn.setRequestProperty("Accept", "application/json");
//            conn.setDoOutput(true);
//
//            try (OutputStream os = conn.getOutputStream()) {
//                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
//                os.write(input, 0, input.length);
//            }
//
//            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name())) {
//                String response = scanner.useDelimiter("\\A").next();
//                System.out.println(response);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
