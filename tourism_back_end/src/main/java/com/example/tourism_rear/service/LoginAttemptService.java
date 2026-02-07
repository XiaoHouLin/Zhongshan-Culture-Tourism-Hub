package com.example.tourism_rear.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {
    private static final int MAX_FAILURES = 3;
    private static final long CAPTCHA_TTL_SECONDS = 300;

    private final SecureRandom random = new SecureRandom();
    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public boolean requiresCaptcha(String username) {
        if (!StringUtils.hasText(username)) {
            return false;
        }
        Attempt attempt = attempts.get(username);
        return attempt != null && attempt.getFailures() >= MAX_FAILURES;
    }

    public CaptchaChallenge createCaptcha(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        Attempt attempt = attempts.computeIfAbsent(username, key -> new Attempt());
        CaptchaChallenge challenge = CaptchaChallenge.generate(random, CAPTCHA_TTL_SECONDS);
        attempt.setCaptcha(challenge);
        return challenge;
    }

    public boolean verifyCaptcha(String username, String token, String input) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(token) || !StringUtils.hasText(input)) {
            return false;
        }
        Attempt attempt = attempts.get(username);
        if (attempt == null || attempt.getCaptcha() == null) {
            return false;
        }
        CaptchaChallenge captcha = attempt.getCaptcha();
        if (captcha.isExpired() || !captcha.getToken().equals(token)) {
            attempt.setCaptcha(null);
            return false;
        }
        String trimmed = input.trim();
        boolean matched = "pass".equalsIgnoreCase(trimmed) || trimmed.equals(String.valueOf(captcha.getAnswer()));
        if (matched) {
            attempt.setCaptcha(null);
        }
        return matched;
    }

    public int recordFailure(String username) {
        if (!StringUtils.hasText(username)) {
            return 0;
        }
        Attempt attempt = attempts.computeIfAbsent(username, key -> new Attempt());
        return attempt.incrementFailures();
    }

    public void reset(String username) {
        if (!StringUtils.hasText(username)) {
            return;
        }
        attempts.remove(username);
    }

    public static class CaptchaChallenge {
        private final String token;
        private final String question;
        private final int answer;
        private final Instant expiresAt;

        private CaptchaChallenge(String token, String question, int answer, Instant expiresAt) {
            this.token = token;
            this.question = question;
            this.answer = answer;
            this.expiresAt = expiresAt;
        }

        public static CaptchaChallenge generate(SecureRandom random, long ttlSeconds) {
            int left = random.nextInt(9) + 1;
            int right = random.nextInt(9) + 1;
            String question = left + " + " + right + " = ?";
            int answer = left + right;
            String token = UUID.randomUUID().toString().replace("-", "");
            Instant expiresAt = Instant.now().plusSeconds(ttlSeconds);
            return new CaptchaChallenge(token, question, answer, expiresAt);
        }

        public String getToken() {
            return token;
        }

        public String getQuestion() {
            return question;
        }

        public int getAnswer() {
            return answer;
        }

        public long getExpiresInSeconds() {
            return Math.max(0, Duration.between(Instant.now(), expiresAt).getSeconds());
        }

        public boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    private static class Attempt {
        private int failures;
        private CaptchaChallenge captcha;

        synchronized int incrementFailures() {
            failures += 1;
            return failures;
        }

        int getFailures() {
            return failures;
        }

        CaptchaChallenge getCaptcha() {
            return captcha;
        }

        void setCaptcha(CaptchaChallenge captcha) {
            this.captcha = captcha;
        }
    }
}
