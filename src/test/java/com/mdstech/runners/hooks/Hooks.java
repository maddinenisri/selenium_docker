package com.mdstech.runners.hooks;

import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.GeckoDriverService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class Hooks {

    public static WebDriver driver;
    private static GeckoDriverService service;

    @Before
    public void setUp() {
        System.out.println("Setting up WebDriver...");
        System.out.println("OS: " + System.getProperty("os.name") + ", Arch: "
                + System.getProperty("os.arch"));

        FirefoxOptions options = new FirefoxOptions();

        // Print environment variables for debugging
        System.out.println("DISPLAY=" + System.getenv("DISPLAY"));
        System.out.println("MOZ_HEADLESS=" + System.getenv("MOZ_HEADLESS"));

        // Set Firefox options for headless mode
        options.addArguments("-headless"); // Use single dash for compatibility
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-software-rasterizer");

        // Additional options that might help in Docker
        options.addArguments("--remote-debugging-port=9222");

        // Accept insecure certificates
        options.setAcceptInsecureCerts(true);

        // Set Firefox binary path based on OS
        String osName = System.getProperty("os.name").toLowerCase();
        String geckoDriverPath;
        String firefoxBinaryPath;

        if (osName.contains("mac")) {
            // Paths for macOS
            geckoDriverPath =
                    System.getenv().getOrDefault("GECKODRIVER_BIN", "/usr/local/bin/geckodriver");
            firefoxBinaryPath = System.getenv().getOrDefault("FIREFOX_BIN",
                    "/Applications/Firefox.app/Contents/MacOS/firefox");
        } else {
            // Paths for Linux (Docker)
            geckoDriverPath =
                    System.getenv().getOrDefault("GECKODRIVER_BIN", "/usr/local/bin/geckodriver");
            firefoxBinaryPath = System.getenv().getOrDefault("FIREFOX_BIN", "/usr/bin/firefox");

            // Set additional Linux-specific options
            options.addArguments("--disable-gpu");
            options.addArguments("--disable-software-rasterizer");
        }

        // Set the Firefox binary path
        options.setBinary(firefoxBinaryPath);

        // Accept insecure certificates
        options.setAcceptInsecureCerts(true);

        // Set logging for debugging
        options.setLogLevel(org.openqa.selenium.firefox.FirefoxDriverLogLevel.TRACE);

        try {
            // Create and start the GeckoDriver service
            System.out.println("Starting GeckoDriver service with executable: " + geckoDriverPath);
            service = new GeckoDriverService.Builder()
                    .usingDriverExecutable(new File(geckoDriverPath))
                    .withLogFile(new File("geckodriver.log")).withEnvironment(System.getenv()) // Pass
                                                                                               // all
                                                                                               // environment
                                                                                               // variables
                    .build();

            service.start();
            System.out.println("GeckoDriver service started successfully");

            // Create the Firefox driver
            System.out.println("Creating Firefox driver with binary: " + firefoxBinaryPath);
            try {
                driver = new FirefoxDriver(service, options);
                System.out.println("Firefox driver created successfully");
            } catch (Exception e) {
                System.err.println(
                        "Failed to create Firefox driver with service. Trying direct approach: "
                                + e.getMessage());
                e.printStackTrace();

                // Try alternative approach without explicit service
                try {
                    System.setProperty("webdriver.gecko.driver", geckoDriverPath);
                    driver = new FirefoxDriver(options);
                    System.out.println("Firefox driver created successfully using direct approach");
                } catch (Exception e2) {
                    System.err.println("Both approaches failed. Final error: " + e2.getMessage());
                    e2.printStackTrace();
                    throw e2;
                }
            }
        } catch (IOException e) {
            System.err.println("Error starting Geckodriver service: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to start Geckodriver service", e);
        }
    }

    @AfterStep
    public void captureScreenshotAfterStep(Scenario scenario) {
        try {
            if (driver instanceof TakesScreenshot) {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", "Step Screenshot");
                saveScreenshotToFile(screenshot, scenario.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveScreenshotToFile(byte[] screenshotBytes, String scenarioName) {
        try {
            String sanitizedScenarioName = scenarioName.replaceAll("[^a-zA-Z0-9]", "_");
            Files.createDirectories(Paths.get("target/screenshots/"));
            String filePath = "target/screenshots/" + sanitizedScenarioName + "_"
                    + System.currentTimeMillis() + ".png";
            Files.write(Paths.get(filePath), screenshotBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        if (service != null && service.isRunning()) {
            service.stop();
        }
    }
}
