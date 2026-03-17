package com.kmbeast.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.kmbeast.config.AlipayProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 支付宝基础能力
 */
@Component
public class AlipaySupport {

    @Resource
    private AlipayProperties alipayProperties;

    public String buildPagePayForm(String outTradeNo, BigDecimal totalAmount, String subject,
                                   String body, String returnUrl) {
        AssertUtils.hasText(alipayProperties.getAppId(), "支付宝APPID未配置");
        AssertUtils.hasText(alipayProperties.getPrivateKey(), "支付宝应用私钥未配置");
        Map<String, String> bizContent = new LinkedHashMap<>();
        bizContent.put("out_trade_no", outTradeNo);
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        bizContent.put("total_amount", totalAmount.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString());
        bizContent.put("subject", subject);
        if (body != null && !body.trim().isEmpty()) {
            bizContent.put("body", body);
        }

        Map<String, String> params = buildCommonParams("alipay.trade.page.pay", JSON.toJSONString(bizContent));
        params.put("notify_url", alipayProperties.getNotifyUrl());
        params.put("return_url", returnUrl);
        params.put("sign", sign(params));

        StringBuilder builder = new StringBuilder();
        builder.append("<form id='alipaySubmit' name='alipaySubmit' action='")
                .append(escapeHtml(alipayProperties.getGatewayUrl()))
                .append("?charset=")
                .append(escapeHtml(alipayProperties.getCharset()))
                .append("' method='POST'>");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.append("<input type='hidden' name='")
                    .append(escapeHtml(entry.getKey()))
                    .append("' value='")
                    .append(escapeHtml(entry.getValue()))
                    .append("'/>");
        }
        builder.append("<input type='submit' value='ok' style='display:none;'></form>");
        builder.append("<script>document.forms['alipaySubmit'].submit();</script>");
        return builder.toString();
    }

    public TradeQueryResult queryTrade(String outTradeNo) {
        Map<String, String> bizContent = new LinkedHashMap<>();
        bizContent.put("out_trade_no", outTradeNo);
        Map<String, String> params = buildCommonParams("alipay.trade.query", JSON.toJSONString(bizContent));
        params.put("sign", sign(params));
        String responseText = doPost(alipayProperties.getGatewayUrl(), params);
        JSONObject responseJson = JSON.parseObject(responseText);
        JSONObject queryResponse = responseJson.getJSONObject("alipay_trade_query_response");
        if (queryResponse == null) {
            return new TradeQueryResult(false, null, null, responseText);
        }
        String code = queryResponse.getString("code");
        String tradeStatus = queryResponse.getString("trade_status");
        String alipayTradeNo = queryResponse.getString("trade_no");
        return new TradeQueryResult("10000".equals(code), tradeStatus, alipayTradeNo, responseText);
    }

    public boolean verifySignature(Map<String, String> params) {
        try {
            String sign = params.get("sign");
            AssertUtils.hasText(sign, "签名为空");
            String content = buildSignContent(params, true);
            PublicKey publicKey = getPublicKey(alipayProperties.getPublicKey());
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(content.getBytes(StandardCharsets.UTF_8));
            return signature.verify(Base64.getDecoder().decode(sign));
        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, String> buildCommonParams(String method, String bizContent) {
        Map<String, String> params = new HashMap<>();
        params.put("app_id", alipayProperties.getAppId());
        params.put("method", method);
        params.put("format", alipayProperties.getFormat());
        params.put("charset", alipayProperties.getCharset());
        params.put("sign_type", alipayProperties.getSignType());
        params.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        params.put("version", "1.0");
        params.put("biz_content", bizContent);
        return params;
    }

    private String sign(Map<String, String> params) {
        try {
            String content = buildSignContent(params, false);
            PrivateKey privateKey = getPrivateKey(alipayProperties.getPrivateKey());
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            throw new RuntimeException("支付宝签名失败: " + e.getMessage(), e);
        }
    }

    private String buildSignContent(Map<String, String> params, boolean verifyMode) {
        TreeMap<String, String> sorted = new TreeMap<>(params);
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value == null || value.trim().isEmpty()) {
                continue;
            }
            if ("sign".equals(key)) {
                continue;
            }
            if (verifyMode && "sign_type".equals(key)) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(key).append("=").append(value);
        }
        return builder.toString();
    }

    private String doPost(String url, Map<String, String> params) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            String body = buildRequestBody(params);
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(body.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } catch (Exception e) {
            throw new RuntimeException("调用支付宝接口失败: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String buildRequestBody(Map<String, String> params) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(urlEncode(entry.getKey()))
                    .append("=")
                    .append(urlEncode(entry.getValue()));
        }
        return builder.toString();
    }

    private String urlEncode(String text) {
        try {
            return URLEncoder.encode(text, alipayProperties.getCharset());
        } catch (Exception e) {
            throw new RuntimeException("参数编码失败", e);
        }
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private PrivateKey getPrivateKey(String key) throws Exception {
        String formatKey = key.replaceAll("\\s+", "");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(formatKey));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    private PublicKey getPublicKey(String key) throws Exception {
        String formatKey = key.replaceAll("\\s+", "");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(formatKey));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    public String buildReturnUrl(Integer billId) {
        String baseUrl = alipayProperties.getReturnUrl();
        if (baseUrl.contains("?")) {
            return baseUrl + "&billId=" + billId;
        }
        return baseUrl + "?billId=" + billId;
    }

    public String buildRepairReturnUrl(Integer repairOrderId) {
        String baseUrl = alipayProperties.getReturnUrl();
        if (baseUrl.contains("/payment-result")) {
            baseUrl = baseUrl.replace("/payment-result", "/my-repair-order");
        }
        if (baseUrl.contains("?")) {
            return baseUrl + "&repairOrderId=" + repairOrderId;
        }
        return baseUrl + "?repairOrderId=" + repairOrderId;
    }

    @Data
    @AllArgsConstructor
    public static class TradeQueryResult {
        private boolean success;
        private String tradeStatus;
        private String alipayTradeNo;
        private String rawResponse;
    }
}
