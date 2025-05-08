from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service
import os

def test_google_homepage():
    chrome_options = Options()
    chrome_options.add_argument("--headless")
    chrome_options.add_argument("--no-sandbox")
    chrome_options.add_argument("--disable-dev-shm-usage")

    # Explicitly set path to chromedriver
    chrome_driver_path = os.environ.get("CHROMEDRIVER_BIN", "/usr/bin/chromedriver")
    service = Service(executable_path=chrome_driver_path)

    driver = webdriver.Chrome(service=service, options=chrome_options)

    try:
        driver.get("https://www.google.com")
        print("Page Title:", driver.title)
        assert "Google" in driver.title
    finally:
        driver.quit()

if __name__ == "__main__":
    test_google_homepage()
