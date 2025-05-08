Feature: Google Search Functionality

  Scenario: Search for ChatGPT on Google
    Given I am on the Google page
    When I search for "ChatGPT"
    Then the page title should contain "ChatGPT"
