# Selenium Firefox Docker Tests

This project demonstrates how to run Selenium tests with Firefox in a Docker container, specifically addressing the challenges of running Firefox in headless mode in containerized environments.

## Key Features

- Robust Firefox configuration for Docker environments
- Multiple approaches to handle Firefox headless mode
- Xvfb integration for virtual display support
- Detailed logging and error reporting

## Prerequisites

- Docker
- Maven (for local development)

## Building the Docker Image

Build the Docker image using the following command:

```bash
docker build -t selenium-firefox-tests -f firefox-dockerfile .
```

## Running Tests in Docker

There are several ways to run the tests in the Docker container:

### Option 1: Using the Default CMD

The Dockerfile includes a default CMD that runs the tests with Xvfb:

```bash
docker run --rm selenium-firefox-tests
```

### Option 2: Using the run-tests.sh script

This script provides detailed logging and error reporting:

```bash
docker run --rm selenium-firefox-tests /usr/local/bin/run-tests.sh
```

### Option 3: Using the with-xvfb wrapper

This uses a custom wrapper to execute Maven tests with Xvfb:

```bash
docker run --rm selenium-firefox-tests with-xvfb mvn test
```

### Option 4: Using xvfb-run-safe

This uses the xvfb-run wrapper to execute Maven tests:

```bash
docker run --rm selenium-firefox-tests xvfb-run-safe mvn test
```

### Option 5: Direct xvfb-run command

```bash
docker run --rm selenium-firefox-tests xvfb-run -a --server-args="-screen 0 1920x1080x24" mvn test
```

## Technical Details

### Firefox Configuration

The solution uses several approaches to ensure Firefox runs properly in headless mode:

1. **Firefox Preferences**: Using preferences instead of command-line arguments for better stability
   - `browser.headless`: true
   - `browser.tabs.remote.autostart`: false (disables multi-process mode)
   - `dom.ipc.processCount`: 1 (forces single-process mode)
   - `security.sandbox.content.level`: 2 (reduces sandbox restrictions)

2. **Environment Variables**: Setting Firefox-specific environment variables
   - `MOZ_HEADLESS=1`: Enables headless mode
   - `MOZ_FORCE_DISABLE_E10S=1`: Disables multi-process mode
   - `MOZ_DISABLE_CONTENT_SANDBOX=1`: Disables content process sandboxing
   - `MOZ_DISABLE_GMP_SANDBOX=1`: Disables media plugin sandboxing
   - And other sandbox disabling variables

3. **Xvfb Integration**: Using Xvfb to provide a virtual display
   - Screen size: 1920x1080
   - Color depth: 24-bit
   - Display number: :99

### Error Handling

The solution includes multiple fallback mechanisms:

1. Multiple approaches to create the Firefox driver
2. Detailed logging of environment information
3. Display of test reports and logs on failure
4. Process cleanup to ensure no orphaned processes

## Troubleshooting

If you encounter issues with Firefox in headless mode, try the following:

1. Check the geckodriver.log file for detailed error messages
2. Verify that all necessary Firefox dependencies are installed in the Docker image
3. Try running with different Firefox preferences or environment variables
4. Ensure proper permissions for the Firefox binary and profile directories

## Environment Variables

The following environment variables are set in the Docker container:

- `FIREFOX_BIN`: Path to the Firefox binary
- `GECKODRIVER_BIN`: Path to the Geckodriver binary
- `MOZ_HEADLESS`: Set to 1 to enable Firefox headless mode
- `DISPLAY`: Set to :99 for Xvfb
- Various `MOZ_DISABLE_*` variables to disable Firefox sandboxing features
