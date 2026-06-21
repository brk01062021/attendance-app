package com.school.attendance.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@Service
public class SmsOtpService {

    private static final Logger log = LoggerFactory.getLogger(SmsOtpService.class);

    private final boolean enabled;
    private final String defaultCountryCode;
    private final String twilioAccountSid;
    private final String twilioAuthToken;
    private final String twilioFromNumber;
    private final HttpClient httpClient;

    public SmsOtpService(@Value("${vidyasetu.sms.enabled:true}") boolean enabled,
                         @Value("${vidyasetu.sms.default-country-code:+91}") String defaultCountryCode,
                         @Value("${vidyasetu.sms.twilio.account-sid:${TWILIO_ACCOUNT_SID:}}") String twilioAccountSid,
                         @Value("${vidyasetu.sms.twilio.auth-token:${TWILIO_AUTH_TOKEN:}}") String twilioAuthToken,
                         @Value("${vidyasetu.sms.twilio.from-number:${TWILIO_FROM_NUMBER:}}") String twilioFromNumber) {
        this.enabled = enabled;
        this.defaultCountryCode = defaultCountryCode == null || defaultCountryCode.isBlank() ? "+91" : defaultCountryCode.trim();
        this.twilioAccountSid = clean(twilioAccountSid);
        this.twilioAuthToken = clean(twilioAuthToken);
        this.twilioFromNumber = toE164(clean(twilioFromNumber));
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public void sendParentOtp(String parentMobile, String otp, String schoolCode, String studentId) {
        if (!enabled) {
            throw new IllegalStateException("SMS OTP delivery is disabled. Enable vidyasetu.sms.enabled and configure Twilio before requesting parent OTP.");
        }

        String destination = toE164(parentMobile);
        String message = "VIDYST: Your VidyaSetu OTP is " + otp + ". Valid for 5 minutes. Do not share this OTP with anyone.";
        sendTwilioSms(destination, message, schoolCode, studentId);
    }

    private void sendTwilioSms(String destination, String message, String schoolCode, String studentId) {
        if (twilioAccountSid.isBlank() || twilioAuthToken.isBlank() || twilioFromNumber.isBlank()) {
            throw new IllegalStateException("Twilio SMS is not configured. Set TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN and TWILIO_FROM_NUMBER.");
        }
        if (samePhoneNumber(destination, twilioFromNumber)) {
            throw new IllegalStateException("Twilio OTP destination is the same as TWILIO_FROM_NUMBER. Verify the parent mobile number stored in VidyaSetu. From/To cannot be identical.");
        }

        log.info("Sending parent OTP via Twilio. schoolCode={}, studentId={}, from={}, to={}",
                safeLog(schoolCode), safeLog(studentId), maskPhone(twilioFromNumber), maskPhone(destination));

        String form = "To=" + encode(destination) + "&From=" + encode(twilioFromNumber) + "&Body=" + encode(message);
        String auth = Base64.getEncoder().encodeToString((twilioAccountSid + ":" + twilioAuthToken).getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.twilio.com/2010-04-01/Accounts/" + twilioAccountSid + "/Messages.json"))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Basic " + auth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();
        sendAndValidate(request);
    }

    private void sendAndValidate(HttpRequest request) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300 || isProviderError(response.body())) {
                throw new IllegalStateException("Twilio rejected OTP request. Please verify Twilio balance/trial status, verified recipient number, credentials, from number and phone number format. Provider response: " + safeProviderResponse(response.body()));
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to reach Twilio for OTP delivery.", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("SMS OTP delivery was interrupted.", ex);
        }
    }

    private boolean isProviderError(String body) {
        if (body == null) return false;
        String lower = body.toLowerCase();
        return lower.contains("\"type\":\"error\"")
                || lower.contains("\"type\": \"error\"")
                || lower.contains("\"status\":\"error\"")
                || lower.contains("\"status\": \"error\"")
                || lower.contains("\"error_code\":")
                || lower.contains("\"code\":");
    }

    private String toE164(String mobile) {
        String cleaned = mobile == null ? "" : mobile.trim().replaceAll("[\\s()-]", "");
        if (cleaned.startsWith("+")) {
            String digits = cleaned.substring(1).replaceAll("\\D", "");
            if (digits.isBlank()) {
                throw new IllegalStateException("Parent mobile number is required for OTP delivery.");
            }
            return "+" + digits;
        }
        cleaned = cleaned.replaceAll("\\D", "");
        if (cleaned.isBlank()) {
            throw new IllegalStateException("Parent mobile number is required for OTP delivery.");
        }
        String country = defaultCountryCode.startsWith("+") ? defaultCountryCode : "+" + defaultCountryCode;
        return country.replaceAll("[^+\\d]", "") + cleaned;
    }

    private boolean samePhoneNumber(String first, String second) {
        String a = first == null ? "" : first.replaceAll("\\D", "");
        String b = second == null ? "" : second.replaceAll("\\D", "");
        return !a.isBlank() && a.equals(b);
    }

    private String safeProviderResponse(String body) {
        if (body == null || body.isBlank()) return "<empty>";
        String cleaned = body.replaceAll("[\\r\\n\\t]+", " ").trim();
        if (cleaned.length() > 500) {
            return cleaned.substring(0, 500) + "...";
        }
        return cleaned;
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private String safeLog(String value) {
        return value == null || value.isBlank() ? "-" : value.trim();
    }

    private String maskPhone(String phone) {
        String digits = phone == null ? "" : phone.replaceAll("\\D", "");
        if (digits.length() <= 4) {
            return "****";
        }
        return "+***" + digits.substring(digits.length() - 4);
    }
}
