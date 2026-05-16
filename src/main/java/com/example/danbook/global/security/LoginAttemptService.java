package com.example.danbook.global.security;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 로그인 실패 횟수를 메모리에 저장하고, 임계치 초과 시 일정 시간 차단한다.
 */
@Service
public class LoginAttemptService {

    private static final int MAX_FAILURES = 5;
    private static final long WINDOW_MILLIS = 10 * 60 * 1000L;
    private static final long BLOCK_MILLIS = 10 * 60 * 1000L;

    private final Map<String, AttemptRecord> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String username, String clientIp) {
        long now = System.currentTimeMillis();
        return isBlockedKey(userKey(username), now);
    }

    public void onFailure(String username, String clientIp) {
        long now = System.currentTimeMillis();
        updateFailure(userKey(username), now);
    }

    public void onSuccess(String username, String clientIp) {
        attempts.remove(userKey(username));
    }

    private boolean isBlockedKey(String key, long now) {
        AttemptRecord record = attempts.get(key);
        if (record == null) return false;
        if (record.blockedUntil > now) return true;

        // 차단 만료 + 윈도우 만료 시 정리
        if (record.firstFailureAt + WINDOW_MILLIS < now) {
            attempts.remove(key);
        }
        return false;
    }

    private void updateFailure(String key, long now) {
        if (key == null) return;
        attempts.compute(key, (k, existing) -> {
            AttemptRecord record = (existing == null) ? new AttemptRecord(now) : existing;

            if (record.blockedUntil > now) return record;

            if (record.firstFailureAt + WINDOW_MILLIS < now) {
                record = new AttemptRecord(now);
            }

            record.failures += 1;
            if (record.failures >= MAX_FAILURES) {
                record.blockedUntil = now + BLOCK_MILLIS;
            }
            return record;
        });
    }

    private String userKey(String username) {
        if (username == null) return null;
        String normalized = username.trim().toLowerCase();
        if (normalized.isEmpty()) return null;
        return "USER:" + normalized;
    }

    private static class AttemptRecord {
        int failures;
        long firstFailureAt;
        long blockedUntil;

        AttemptRecord(long now) {
            this.failures = 0;
            this.firstFailureAt = now;
            this.blockedUntil = 0L;
        }
    }
}
