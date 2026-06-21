package com.school.attendance.service.notification;

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

    private final String provider;
    private final boolean enabled;
    private final String defaultCountryCode;
    private final String twilioAccountSid;
    private final String twilioAuthToken;
    private final String twilioFromNumber;
    private final String msg91AuthKey;
    private final String msg91WidgetId;
    private final String msg91WidgetToken;
    private final String msg91SenderId;
    private final String msg91TemplateId;
    private final HttpClient httpClient;

    public SmsOtpService(@Value("${vidyasetu.sms.provider:msg91}") String provider,
                         @Value("${vidyasetu.sms.enabled:true}") boolean enabled,
                         @Value("${vidyasetu.sms.default-country-code:+91}") String defaultCountryCode,
                         @Value("${vidyasetu.sms.twilio.account-sid:}") String twilioAccountSid,
                         @Value("${vidyasetu.sms.twilio.auth-token:}") String twilioAuthToken,
                         @Value("${vidyasetu.sms.twilio.from-number:}") String twilioFromNumber,
                         @Value("${vidyasetu.sms.msg91.auth-key:${MSG91_AUTH_KEY:}}") String msg91AuthKey,
                         @Value("${vidyasetu.sms.msg91.widget-id:${MSG91_WIDGET_ID:}}") String msg91WidgetId,
                         @Value("${vidyasetu.sms.msg91.widget-token:${MSG91_WIDGET_TOKEN:}}") String msg91WidgetToken,
                         @Value("${vidyasetu.sms.msg91.sender-id:${MSG91_SENDER_ID:VIDYST}}") String msg91SenderId,
                         @Value("${vidyasetu.sms.msg91.template-id:${MSG91_TEMPLATE_ID:}}") String msg91TemplateId) {
        this.provider = provider == null ? "msg91" : provider.trim().toLowerCase();
        this.enabled = enabled;
        this.defaultCountryCode = defaultCountryCode == null || defaultCountryCode.isBlank() ? "+91" : defaultCountryCode.trim();
        this.twilioAccountSid = clean(twilioAccountSid);
        this.twilioAuthToken = clean(twilioAuthToken);
        this.twilioFromNumber = clean(twilioFromNumber);
        this.msg91AuthKey = clean(msg91AuthKey);
        this.msg91WidgetId = clean(msg91WidgetId);
        this.msg91WidgetToken = clean(msg91WidgetToken);
        this.msg91SenderId = clean(msg91SenderId).isBlank() ? "VIDYST" : clean(msg91SenderId);
        this.msg91TemplateId = clean(msg91TemplateId);
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public void sendParentOtp(String parentMobile, String otp, String schoolCode, String studentId) {
        if (!enabled) {
            throw new IllegalStateException("SMS OTP delivery is disabled. Enable vidyasetu.sms.enabled and configure an SMS provider before requesting parent OTP.");
        }
        String destination = toE164(parentMobile);
        String message = "VIDYST: Your VidyaSetu OTP is " + otp + ". Valid for 5 minutes. Do not share this OTP with anyone.";

        if ("msg91".equals(provider)) {
            sendMsg91Otp(destination, otp);
            return;
        }
        if ("twilio".equals(provider)) {
            sendTwilioSms(destination, message);
            return;
        }

        throw new IllegalStateException("Unsupported SMS provider: " + provider + ". Configure vidyasetu.sms.provider=msg91.");
    }

    private void sendMsg91Otp(String destination, String otp) {
        if (msg91AuthKey.isBlank()) {
            throw new IllegalStateException("MSG91 SMS is not configured. Set MSG91_AUTH_KEY before requesting parent OTP.");
        }

        String normalizedMobile = toMsg91Mobile(destination);
        StringBuilder uri = new StringBuilder("https://control.msg91.com/api/v5/otp?")
                .append("mobile=").append(encode(normalizedMobile))
                .append("&otp=").append(encode(otp))
                .append("&otp_expiry=5")
                .append("&otp_length=6");

        if (!msg91TemplateId.isBlank()) {
            uri.append("&template_id=").append(encode(msg91TemplateId));
        }
        if (!msg91SenderId.isBlank()) {
            uri.append("&sender=").append(encode(msg91SenderId));
        }
        if (!msg91WidgetId.isBlank()) {
            uri.append("&widget_id=").append(encode(msg91WidgetId));
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(uri.toString()))
                .timeout(Duration.ofSeconds(20))
                .header("authkey", msg91AuthKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{}"));

        if (!msg91WidgetToken.isBlank()) {
            builder.header("tokenauth", msg91WidgetToken);
        }

        sendAndValidate(builder.build(), "MSG91");
    }

    private void sendTwilioSms(String destination, String message) {
        if (twilioAccountSid.isBlank() || twilioAuthToken.isBlank() || twilioFromNumber.isBlank()) {
            throw new IllegalStateException("Twilio SMS is not configured. Set TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN and TWILIO_FROM_NUMBER.");
        }

        String form = "To=" + encode(destination) + "&From=" + encode(twilioFromNumber) + "&Body=" + encode(message);
        String auth = Base64.getEncoder().encodeToString((twilioAccountSid + ":" + twilioAuthToken).getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.twilio.com/2010-04-01/Accounts/" + twilioAccountSid + "/Messages.json"))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Basic " + auth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();
        sendAndValidate(request, "Twilio");
    }

    private void sendAndValidate(HttpRequest request, String providerName) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300 || isProviderError(response.body())) {
                throw new IllegalStateException(providerName + " rejected OTP request. Please verify SMS provider balance, template/widget status, whitelisted IP, credentials and phone number format. Provider response: " + safeProviderResponse(response.body()));
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to reach SMS provider for OTP delivery.", ex);
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
                || lower.contains("error");
    }

    private String toE164(String mobile) {
        String cleaned = mobile == null ? "" : mobile.trim().replaceAll("[\\s()-]", "");
        if (cleaned.startsWith("+")) {
            return cleaned;
        }
        cleaned = cleaned.replaceAll("\\D", "");
        if (cleaned.isBlank()) {
            throw new IllegalStateException("Parent mobile number is required for OTP delivery.");
        }
        String country = defaultCountryCode.startsWith("+") ? defaultCountryCode : "+" + defaultCountryCode;
        return country + cleaned;
    }

    private String toMsg91Mobile(String e164Mobile) {
        String digits = e164Mobile == null ? "" : e164Mobile.replaceAll("\\D", "");
        if (digits.isBlank()) {
            throw new IllegalStateException("Parent mobile number is required for OTP delivery.");
        }
        return digits;
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
}
