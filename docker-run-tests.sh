#!/bin/bash

# Script to build and run Selenium tests in Docker

# Default values
DOCKERFILE="firefox-dockerfile"
IMAGE_NAME="selenium-firefox-tests"
RUN_MODE="xvfb-run"
MAVEN_ARGS=""

# Display usage information
function show_usage {
    echo "Usage: $0 [options]"
    echo "Options:"
    echo "  -b, --build           Build the Docker image before running tests"
    echo "  -d, --dockerfile FILE Use specified Dockerfile (default: firefox-dockerfile)"
    echo "  -i, --image NAME      Use specified Docker image name (default: selenium-firefox-tests)"
    echo "  -m, --mode MODE       Run mode: script, xvfb-run, or direct (default: xvfb-run)"
    echo "  -a, --args \"ARGS\"     Additional Maven arguments (e.g., \"-Dtest=GoogleTest\")"
    echo "  -h, --help            Show this help message"
    exit 1
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    key="$1"
    case $key in
    -b | --build)
        BUILD_IMAGE=true
        shift
        ;;
    -d | --dockerfile)
        DOCKERFILE="$2"
        shift 2
        ;;
    -i | --image)
        IMAGE_NAME="$2"
        shift 2
        ;;
    -m | --mode)
        RUN_MODE="$2"
        shift 2
        ;;
    -a | --args)
        MAVEN_ARGS="$2"
        shift 2
        ;;
    -h | --help)
        show_usage
        ;;
    *)
        echo "Unknown option: $1"
        show_usage
        ;;
    esac
done

# Build the Docker image if requested
if [ "$BUILD_IMAGE" = true ]; then
    echo "Building Docker image: $IMAGE_NAME from $DOCKERFILE"
    docker build -t "$IMAGE_NAME" -f "$DOCKERFILE" .
    if [ $? -ne 0 ]; then
        echo "Error: Docker build failed"
        exit 1
    fi
fi

# Run the tests based on the selected mode
echo "Running tests in $RUN_MODE mode"

case $RUN_MODE in
script)
    # Use the run-tests.sh script
    docker run --rm "$IMAGE_NAME" /usr/local/bin/run-tests.sh $MAVEN_ARGS
    ;;
xvfb-run)
    # Use xvfb-run-safe wrapper
    docker run --rm "$IMAGE_NAME" xvfb-run-safe mvn test $MAVEN_ARGS
    ;;
direct)
    # Use direct xvfb-run command
    docker run --rm "$IMAGE_NAME" xvfb-run -a --server-args="-screen 0 1920x1080x24" mvn test $MAVEN_ARGS
    ;;
*)
    echo "Error: Unknown run mode: $RUN_MODE"
    show_usage
    ;;
esac

# Get the exit code from the Docker run command
EXIT_CODE=$?

# Display the result
if [ $EXIT_CODE -eq 0 ]; then
    echo "Tests completed successfully"
else
    echo "Tests failed with exit code: $EXIT_CODE"
fi

exit $EXIT_CODE
