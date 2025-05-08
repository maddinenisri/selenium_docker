package com.mdstech.runners.steps;


import io.cucumber.java.en.*;
import static com.mdstech.runners.hooks.Hooks.driver;
import org.junit.jupiter.api.Assertions;
import com.mdstech.pages.GoogleSearchPage;

public class GoogleSearchSteps {
    GoogleSearchPage googlePage;

    @Given("I am on the Google page")
    public void i_am_on_the_google_page() {
        googlePage = new GoogleSearchPage(driver);
        googlePage.openGoogle();
    }

    @When("I search for {string}")
    public void i_search_for(String searchTerm) {
        googlePage.searchFor(searchTerm);
    }

    @Then("the page title should contain {string}")
    public void the_page_title_should_contain(String expectedTitlePart) {
        Assertions.assertTrue(driver.getTitle().contains(expectedTitlePart));
    }
}

