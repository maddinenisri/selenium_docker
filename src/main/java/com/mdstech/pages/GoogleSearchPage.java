package com.mdstech.pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;

public class GoogleSearchPage {

    private WebDriver driver;
    private WebDriverWait wait;

    // Search input field - works for both Google and DuckDuckGo
    @FindBy(name = "q")
    private WebElement searchInput;

    // Google search results container
    @FindBy(id = "search")
    private WebElement googleSearchResults;

    // DuckDuckGo search results container
    @FindBy(id = "links")
    private WebElement duckDuckGoSearchResults;

    public GoogleSearchPage(WebDriver driver) {
        this.driver = driver;
        // Increase timeout for Docker environment
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(60));
        PageFactory.initElements(driver, this);
    }

    public void openGoogle() {
        try {
            System.out.println("Opening search page...");
            // Try DuckDuckGo first as it's more reliable in Docker environments
            driver.get("https://duckduckgo.com");

            // Wait for page to load completely
            wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                    .executeScript("return document.readyState").equals("complete"));

            System.out.println("Search page loaded successfully. Title: " + driver.getTitle());
        } catch (Exception e) {
            System.err.println("Error opening search page: " + e.getMessage());
            e.printStackTrace();

            // Fallback to Google if DuckDuckGo fails
            try {
                driver.get("https://www.google.com");
                wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                        .executeScript("return document.readyState").equals("complete"));
                System.out.println("Fallback to Google successful");
            } catch (Exception fallbackEx) {
                System.err.println("Fallback to Google also failed: " + fallbackEx.getMessage());
                throw fallbackEx;
            }
        }
    }

    public void searchFor(String query) {
        try {
            System.out.println("Searching for: " + query);

            // Wait for search input to be available and clear it
            wait.until(ExpectedConditions.elementToBeClickable(searchInput));
            searchInput.clear();

            // Type the search query and submit
            searchInput.sendKeys(query);
            searchInput.sendKeys(Keys.ENTER);

            // Wait for results using multiple strategies
            try {
                // First try waiting for title to contain the query
                wait.until(ExpectedConditions.titleContains(query));
                System.out.println("Search results loaded. Page title: " + driver.getTitle());
            } catch (TimeoutException e) {
                System.out.println("Title didn't contain query, trying alternative wait strategy");

                // If title wait fails, try waiting for either Google or DuckDuckGo results
                wait.until(
                        ExpectedConditions.or(ExpectedConditions.visibilityOf(googleSearchResults),
                                ExpectedConditions.visibilityOf(duckDuckGoSearchResults)));
                System.out.println("Search results container is visible");
            }
        } catch (Exception e) {
            System.err.println("Error during search: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public String getTitle() {
        return driver.getTitle();
    }
}

