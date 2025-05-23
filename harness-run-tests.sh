#!/bin/bash

# Script to run Maven tests with Xvfb in Harness CI/CD environment

# Function to log messages with timestamps
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

# Function to check if a process is running
is_process_running() {
    ps -p $1 >/dev/null
    return $?
}

# Function to clean up processes on exit
cleanup() {
    log "Cleaning up processes..."

    # Kill Xvfb if it's running
    if [ ! -z "$XVFB_PID" ] && is_process_running $XVFB_PID; then
        log "Stopping Xvfb (PID: $XVFB_PID)"
        kill $XVFB_PID || kill -9 $XVFB_PID
    fi

    log "Cleanup complete"
}

# Set up trap to ensure cleanup on script exit
trap cleanup EXIT INT TERM

# Start Xvfb with specific dimensions and color depth
log "Starting Xvfb..."
Xvfb :99 -screen 0 1920x1080x24 -ac &
XVFB_PID=$!

# Wait for Xvfb to initialize
sleep 2

# Check if Xvfb started successfully
if ! is_process_running $XVFB_PID; then
    log "ERROR: Failed to start Xvfb"
    exit 1
fi

log "Xvfb started successfully with PID: $XVFB_PID"

# Export the DISPLAY variable
export DISPLAY=:99

# Export Firefox environment variables
export MOZ_HEADLESS=1
export MOZ_FORCE_DISABLE_E10S=1
export MOZ_DISABLE_CONTENT_SANDBOX=1
export MOZ_DISABLE_GMP_SANDBOX=1
export MOZ_DISABLE_RDD_SANDBOX=1
export MOZ_DISABLE_SOCKET_PROCESS_SANDBOX=1
export MOZ_DISABLE_UTILITY_SANDBOX=1

# Print environment information
log "Environment Information:"
log "DISPLAY=$DISPLAY"
log "MOZ_HEADLESS=$MOZ_HEADLESS"
log "Java version: $(java -version 2>&1 | head -n 1)"
log "Firefox version: $(firefox --version 2>&1)"
log "Geckodriver version: $(geckodriver --version 2>&1 | head -n 1)"

# Check Firefox and Geckodriver binaries
if [ -f "/usr/bin/firefox" ]; then
    log "Firefox binary exists at /usr/bin/firefox"
    ls -la /usr/bin/firefox
else
    log "WARNING: Firefox binary not found at /usr/bin/firefox"
fi

if [ -f "/usr/local/bin/geckodriver" ]; then
    log "Geckodriver exists at /usr/local/bin/geckodriver"
    ls -la /usr/local/bin/geckodriver
else
    log "WARNING: Geckodriver not found at /usr/local/bin/geckodriver"
fi

# Create a directory for Firefox cache and give it proper permissions
mkdir -p /tmp/.mozilla
chmod 777 /tmp/.mozilla
log "Created Firefox cache directory with proper permissions"

# Run the Maven command passed as arguments
log "Running Maven command: mvn $@"
mvn "$@"
TEST_EXIT_CODE=$?

# If tests failed, display the surefire reports
if [ $TEST_EXIT_CODE -ne 0 ]; then
    log "Tests failed with exit code: $TEST_EXIT_CODE"
    log "Displaying surefire reports:"
    if [ -d "target/surefire-reports" ]; then
        for file in target/surefire-reports/*.txt; do
            log "=== $file ==="
            cat "$file"
            echo ""
        done

        # Check for Geckodriver logs
        if [ -f "geckodriver.log" ]; then
            log "=== geckodriver.log (last 50 lines) ==="
            tail -n 50 geckodriver.log
        fi

        # Check for Selenium error report
        if [ -f "selenium-error-report.txt" ]; then
            log "=== selenium-error-report.txt ==="
            cat "selenium-error-report.txt"
        fi
    else
        log "No surefire reports found."
    fi
else
    log "Tests completed successfully"
fi

# Return the test exit code
exit $TEST_EXIT_CODE
