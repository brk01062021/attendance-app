package com.school.attendance.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String env = "local";
    private Cors cors = new Cors();
    private Jwt jwt = new Jwt();
    private Storage storage = new Storage();
    private Aws aws = new Aws();

    public String getEnv() { return env; }
    public void setEnv(String env) { this.env = env; }

    public Cors getCors() { return cors; }
    public void setCors(Cors cors) { this.cors = cors; }

    public Jwt getJwt() { return jwt; }
    public void setJwt(Jwt jwt) { this.jwt = jwt; }

    public Storage getStorage() { return storage; }
    public void setStorage(Storage storage) { this.storage = storage; }

    public Aws getAws() { return aws; }
    public void setAws(Aws aws) { this.aws = aws; }

    public boolean isStaging() {
        return "staging".equalsIgnoreCase(env);
    }

    public static class Cors {
        private List<String> allowedOrigins = new ArrayList<>();

        public List<String> getAllowedOrigins() {
            if (allowedOrigins == null || allowedOrigins.isEmpty()) {
                return List.of();
            }
            if (allowedOrigins.size() == 1 && allowedOrigins.get(0) != null && allowedOrigins.get(0).contains(",")) {
                return Arrays.stream(allowedOrigins.get(0).split(","))
                        .map(String::trim)
                        .filter(value -> !value.isBlank())
                        .toList();
            }
            return allowedOrigins.stream()
                    .filter(value -> value != null && !value.isBlank())
                    .map(String::trim)
                    .toList();
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }

    public static class Jwt {
        private String secret;
        private Integer expiryMinutes = 60;

        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }

        public Integer getExpiryMinutes() { return expiryMinutes; }
        public void setExpiryMinutes(Integer expiryMinutes) { this.expiryMinutes = expiryMinutes; }
    }

    public static class Storage {
        private String mode = "local";
        private String localUploadDir = "./uploads";

        public String getMode() { return mode; }
        public void setMode(String mode) { this.mode = mode; }

        public String getLocalUploadDir() { return localUploadDir; }
        public void setLocalUploadDir(String localUploadDir) { this.localUploadDir = localUploadDir; }
    }

    public static class Aws {
        private String region;
        private String s3UploadBucket;

        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }

        public String getS3UploadBucket() { return s3UploadBucket; }
        public void setS3UploadBucket(String s3UploadBucket) { this.s3UploadBucket = s3UploadBucket; }
    }
}
