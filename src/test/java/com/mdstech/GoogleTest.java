package com.mdstech;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.GeckoDriverService;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue; // Assuming JUnit 5 for assertion

public class GoogleTest {
    public static void main(String[] args) {
        // Configure FirefoxOptions for headless mode
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("-headless"); // Standard argument for Firefox headless
        options.addArguments("--no-sandbox"); // Good practice for Docker environments
        options.addArguments("--disable-dev-shm-usage"); // Recommended for Docker environments

        String osName = System.getProperty("os.name").toLowerCase();
        String geckoDriverPath;
        String firefoxBinaryPath;

        if (osName.contains("mac")) {
            // Paths for macOS
            geckoDriverPath = System.getenv().getOrDefault("GECKODRIVER_BIN",
                    "/Users/srini/Downloads/geckodriver"); // Or your specific local path
            firefoxBinaryPath = System.getenv().getOrDefault("FIREFOX_BIN",
                    "/Applications/Firefox.app/Contents/MacOS/firefox");
        } else {
            // Paths for Linux (Docker)
            geckoDriverPath =
                    System.getenv().getOrDefault("GECKODRIVER_BIN", "/usr/local/bin/geckodriver");
            firefoxBinaryPath = System.getenv().getOrDefault("FIREFOX_BIN", "/usr/bin/firefox");
        }
        options.setBinary(firefoxBinaryPath); // Set the path to the Firefox binary

        // Set logging level for Firefox (optional, but useful for debugging)
        options.setCapability("moz:firefoxOptions", Map.of("log", Map.of("level", "error")));


        // Build GeckoDriverService
        GeckoDriverService service = new GeckoDriverService.Builder()
                .usingDriverExecutable(new File(geckoDriverPath)).build();

        WebDriver driver = null;
        try {
            // Start the Geckodriver service
            service.start();
            // Initialize FirefoxDriver with the service and options
            driver = new FirefoxDriver(service, options);

            driver.get("https://www.google.com");
            System.out.println("Page Title is: " + driver.getTitle());
            assertTrue(driver.getTitle().contains("Google"));

        } catch (IOException e) {
            System.err.println("Error starting Geckodriver service: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
            }
            if (service != null && service.isRunning()) {
                service.stop();
            }
        }
    }
}
