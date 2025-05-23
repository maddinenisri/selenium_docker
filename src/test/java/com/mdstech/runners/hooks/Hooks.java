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
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.GeckoDriverService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Hooks {

    public static WebDriver driver;
    private static GeckoDriverService service;

    @Before
    public void setUp() {
        try {
            System.out.println("=============== SETUP STARTING ===============");
            System.out.println("Setting up WebDriver...");
            System.out.println("OS: " + System.getProperty("os.name") + ", Arch: "
                    + System.getProperty("os.arch"));
            System.out.println("Java version: " + System.getProperty("java.version"));

            // Print all environment variables for debugging
            System.out.println("Environment variables:");
            System.getenv().forEach((key, value) -> {
                if (key.startsWith("MOZ_") || key.equals("DISPLAY")) {
                    System.out.println(key + "=" + value);
                }
            });

            // Create Firefox options with minimal configuration for maximum compatibility
            FirefoxOptions options = new FirefoxOptions();

            // Use Firefox preferences instead of command-line arguments for better stability
            options.addPreference("browser.headless", true);
            options.addPreference("browser.tabs.remote.autostart", false);
            options.addPreference("browser.tabs.remote.autostart.2", false);

            // Disable multi-process mode (e10s) which can cause issues in Docker
            options.addPreference("browser.tabs.remote.force-disable", true);
            options.addPreference("dom.ipc.processCount", 1);

            // Reduce sandbox level for better compatibility in Docker
            options.addPreference("security.sandbox.content.level", 2);

            // Add minimal command-line arguments
            options.addArguments("-headless");
            options.addArguments("--no-sandbox");

            // Accept insecure certificates
            options.setAcceptInsecureCerts(true);

            // Set Firefox binary path based on OS
            String osName = System.getProperty("os.name").toLowerCase();
            String geckoDriverPath;
            String firefoxBinaryPath;

            if (osName.contains("mac")) {
                // Paths for macOS
                geckoDriverPath = System.getenv().getOrDefault("GECKODRIVER_BIN",
                        "/usr/local/bin/geckodriver");
                firefoxBinaryPath = System.getenv().getOrDefault("FIREFOX_BIN",
                        "/Applications/Firefox.app/Contents/MacOS/firefox");
            } else {
                // Paths for Linux (Docker)
                geckoDriverPath = System.getenv().getOrDefault("GECKODRIVER_BIN",
                        "/usr/local/bin/geckodriver");
                firefoxBinaryPath = System.getenv().getOrDefault("FIREFOX_BIN", "/usr/bin/firefox");
            }

            // Check if Firefox binary exists
            File firefoxBinary = new File(firefoxBinaryPath);
            if (!firefoxBinary.exists()) {
                System.err.println("WARNING: Firefox binary not found at: " + firefoxBinaryPath);
            } else {
                System.out.println("Firefox binary found at: " + firefoxBinaryPath);
            }

            // Check if geckodriver exists
            File geckoDriver = new File(geckoDriverPath);
            if (!geckoDriver.exists()) {
                System.err.println("WARNING: Geckodriver not found at: " + geckoDriverPath);
            } else {
                System.out.println("Geckodriver found at: " + geckoDriverPath);
            }

            // Set the Firefox binary path
            options.setBinary(firefoxBinaryPath);

            // Set logging for debugging
            options.setLogLevel(FirefoxDriverLogLevel.TRACE);

            // Create environment variables map with additional settings
            Map<String, String> env = new HashMap<>(System.getenv());
            env.put("MOZ_HEADLESS", "1");

            if (System.getenv("DISPLAY") == null) {
                env.put("DISPLAY", ":99");
                System.out.println("DISPLAY environment variable was not set, setting to :99");
            }

            // Add Firefox-specific environment variables to disable sandboxing
            env.put("MOZ_DISABLE_GMP_SANDBOX", "1");
            env.put("MOZ_DISABLE_CONTENT_SANDBOX", "1");
            env.put("MOZ_DISABLE_RDD_SANDBOX", "1");
            env.put("MOZ_DISABLE_SOCKET_PROCESS_SANDBOX", "1");
            env.put("MOZ_DISABLE_UTILITY_SANDBOX", "1");
            env.put("MOZ_FORCE_DISABLE_E10S", "1");

            // Start Xvfb if DISPLAY is :99 and we're on Linux
            if (osName.contains("linux") && ":99".equals(env.get("DISPLAY"))) {
                try {
                    System.out.println("Attempting to start Xvfb...");
                    Runtime.getRuntime().exec("Xvfb :99 -screen 0 1920x1080x24 -ac");
                    System.out.println("Xvfb started, waiting 2 seconds...");
                    Thread.sleep(2000);
                } catch (Exception e) {
                    System.err.println("Failed to start Xvfb: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // Create and start the GeckoDriver service
            System.out.println("Starting GeckoDriver service with executable: " + geckoDriverPath);
            service = new GeckoDriverService.Builder()
                    .usingDriverExecutable(new File(geckoDriverPath))
                    .withLogFile(new File("geckodriver.log")).withEnvironment(env).build();

            service.start();
            System.out.println("GeckoDriver service started successfully");

            // Try multiple approaches to create the Firefox driver
            Exception lastException = null;

            // Approach 1: Using service and options
            try {
                System.out.println("Approach 1: Creating Firefox driver with service and options");
                driver = new FirefoxDriver(service, options);
                System.out.println("Firefox driver created successfully using approach 1");
                System.out.println("=============== SETUP COMPLETED ===============");
                return;
            } catch (Exception e) {
                System.err.println("Approach 1 failed: " + e.getMessage());
                e.printStackTrace();
                lastException = e;
            }

            // Approach 2: Using system property and options
            try {
                System.out.println(
                        "Approach 2: Creating Firefox driver with system property and options");
                System.setProperty("webdriver.gecko.driver", geckoDriverPath);
                driver = new FirefoxDriver(options);
                System.out.println("Firefox driver created successfully using approach 2");
                System.out.println("=============== SETUP COMPLETED ===============");
                return;
            } catch (Exception e) {
                System.err.println("Approach 2 failed: " + e.getMessage());
                e.printStackTrace();
                lastException = e;
            }

            // If we get here, all approaches failed
            if (lastException != null) {
                throw new RuntimeException("All approaches to create Firefox driver failed",
                        lastException);
            }

        } catch (Throwable t) {
            System.err.println("=============== SETUP FAILED ===============");
            System.err.println("Fatal error in setUp method: " + t.getMessage());
            t.printStackTrace();

            // Try to create a simple error report file
            try {
                Files.write(Paths.get("selenium-error-report.txt"),
                        ("Error in setUp: " + t.getMessage() + "\n\nStack trace:\n"
                                + java.util.Arrays.toString(t.getStackTrace())).getBytes());
            } catch (IOException ioe) {
                System.err.println("Failed to write error report: " + ioe.getMessage());
            }

            throw new RuntimeException("Failed to set up WebDriver", t);
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
            System.err.println("Failed to capture screenshot: " + e.getMessage());
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
            System.err.println("Failed to save screenshot: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() {
        System.out.println("=============== TEARDOWN STARTING ===============");
        try {
            if (driver != null) {
                driver.quit();
                System.out.println("WebDriver quit successfully");
            }
            if (service != null && service.isRunning()) {
                service.stop();
                System.out.println("GeckoDriver service stopped successfully");
            }
            System.out.println("=============== TEARDOWN COMPLETED ===============");
        } catch (Exception e) {
            System.err.println("Error during tearDown: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
