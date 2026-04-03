package com.kmbeast.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SubmissionThrottleUtils {

    private static final Map<String, Long> LAST_SUBMIT_TIME = new ConcurrentHashMap<>();
    private static final long DEFAULT_INTERVAL_MILLIS = 30_000L;

    private SubmissionThrottleUtils() {
    }

    public static void ensureAllowed(String scene, Integer userId) {
        AssertUtils.notNull(userId, "用户信息异常");
        String key = scene + ":" + userId;
        long now = System.currentTimeMillis();
        Long last = LAST_SUBMIT_TIME.get(key);
        AssertUtils.isFalse(last != null && now - last < DEFAULT_INTERVAL_MILLIS, "操作过于频繁，请30秒后再试");
        LAST_SUBMIT_TIME.put(key, now);
    }
}
