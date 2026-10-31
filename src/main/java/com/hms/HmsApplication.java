package com.hms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class HmsApplication {

    public static void main(String[] args) {
        loadDotEnvIfPresent();
        SpringApplication.run(HmsApplication.class, args);
    }

    /**
     * Tự động nạp các biến môi trường từ file .env nếu file tồn tại ở thư mục gốc workspace.
     * Giúp dev chạy trực tiếp qua `./mvnw spring-boot:run` hoặc IDE mà không cần export thủ công.
     */
    private static void loadDotEnvIfPresent() {
        File envFile = new File(".env");
        if (envFile.exists() && envFile.isFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(envFile, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    int eqIndex = line.indexOf('=');
                    if (eqIndex > 0) {
                        String key = line.substring(0, eqIndex).trim();
                        String val = line.substring(eqIndex + 1).trim();
                        if (System.getProperty(key) == null && System.getenv(key) == null) {
                            System.setProperty(key, val);
                        }
                    }
                }
            } catch (Exception ignored) {
                // Fallback im lặng nếu không đọc được file .env
            }
        }
    }
}


