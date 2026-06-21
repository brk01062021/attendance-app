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
    private final HttpClient httpClient;

    public SmsOtpService(@Value("${vidyasetu.sms.provider:twilio}") String provider,
                         @Value("${vidyasetu.sms.enabled:true}") boolean enabled,
                         @Value("${vidyasetu.sms.default-country-code:+91}") String defaultCountryCode,
                         @Value("${vidyasetu.sms.twilio.account-sid:}") String twilioAccountSid,
                         @Value("${vidyasetu.sms.twilio.auth-token:}") String twilioAuthToken,
                         @Value("${vidyasetu.sms.twilio.from-number:}") String twilioFromNumber) {
        this.provider = provider == null ? "twilio" : provider.trim().toLowerCase();
        this.enabled = enabled;
        this.defaultCountryCode = defaultCountryCode == null || defaultCountryCode.isBlank() ? "+91" : defaultCountryCode.trim();
        this.twilioAccountSid = twilioAccountSid == null ? "" : twilioAccountSid.trim();
        this.twilioAuthToken = twilioAuthToken == null ? "" : twilioAuthToken.trim();
        this.twilioFromNumber = twilioFromNumber == null ? "" : twilioFromNumber.trim();
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public void sendParentOtp(String parentMobile, String otp, String schoolCode, String studentId) {
        if (!enabled) {
            throw new IllegalStateException("SMS OTP delivery is disabled. Enable vidyasetu.sms.enabled and configure an SMS provider before requesting parent OTP.");
        }
        String destination = toE164(parentMobile);
        String message = "VidyaSetu OTP for parent activation at " + schoolCode + " is " + otp + ". It expires in 10 minutes. Do not share this OTP.";

        if ("twilio".equals(provider)) {
            sendTwilioSms(destination, message);
            return;
        }

        throw new IllegalStateException("Unsupported SMS provider: " + provider + ". Configure vidyasetu.sms.provider=twilio.");
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
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("SMS provider rejected OTP request. Please verify SMS provider credentials and phone number format.");
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to reach SMS provider for OTP delivery.", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("SMS OTP delivery was interrupted.", ex);
        }
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

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
