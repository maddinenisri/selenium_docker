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

public class GoogleSearchPage {

    private WebDriver driver;
    private WebDriverWait wait;

    @FindBy(name = "q")
    private WebElement searchInput;

    @FindBy(id = "search") // Corrected ID to "search"
    // @FindBy(id = "links") // DuckDuckGo results block uses id="links"
    private WebElement searchResults;

    private By searchResultByTestId = By.cssSelector("[data-testid='result-title-a']");

    public GoogleSearchPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30)); // Explicit wait
        PageFactory.initElements(driver, this); // Initialize elements
    }

    public void openGoogle() {
        // driver.get("https://www.google.com");
        driver.get("https://duckduckgo.com");
    }

    public void searchFor(String query) {
        searchInput.sendKeys(query);
        searchInput.sendKeys(Keys.ENTER);
        // wait.until(ExpectedConditions.visibilityOf(searchResults)); // Wait for
        // results
        wait.until(ExpectedConditions.titleContains(query));
        // wait.until(ExpectedConditions.visibilityOfElementLocated(searchResultByTestId));
    }

    // public boolean areSearchResultsDisplayed() {
    // return searchResults.isDisplayed();
    // }

    public String getTitle() {
        return driver.getTitle();
    }
}

