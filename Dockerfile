FROM python:3.11-slim

# Install system dependencies
RUN apt-get update && apt-get install -y \
    chromium chromium-driver \
    fonts-liberation \
    libasound2 libatk-bridge2.0-0 libnspr4 libnss3 libx11-xcb1 libxcomposite1 libxdamage1 libxrandr2 xdg-utils \
    wget unzip curl gnupg ca-certificates \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Set environment variables
ENV CHROME_BIN=/usr/bin/chromium
ENV CHROMEDRIVER_BIN=/usr/bin/chromedriver

# Install Python packages
RUN pip install selenium

# Copy test code
COPY test_selenium_chromium.py .

# Run the test
CMD ["python", "test_selenium_chromium.py"]
