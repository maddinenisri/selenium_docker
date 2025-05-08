package com.mdstech.runners;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

@Suite
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME,
        value = "com.mdstech.runners.steps, com.mdstech.runners.hooks")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME,
        value = "pretty,html:target/cucumber-reports/cucumber.html")
public class TestRunner {
}
