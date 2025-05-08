package com.mdstech;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chrome.ChromeDriverService;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GoogleTest {
    public static void main(String[] args) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        // Manually provide Chromedriver path
        String chromeDriverPath =
                System.getenv().getOrDefault("CHROMEDRIVER_BIN", "/usr/bin/chromedriver");

        ChromeDriverService service = new ChromeDriverService.Builder()
                .usingDriverExecutable(new File(chromeDriverPath)).build();

        WebDriver driver = new ChromeDriver(service, options);

        try {
            driver.get("https://www.google.com");
            System.out.println("Page Title is: " + driver.getTitle());
            assertTrue(driver.getTitle().contains("Google"));
        } finally {
            driver.quit();
        }
    }
}
