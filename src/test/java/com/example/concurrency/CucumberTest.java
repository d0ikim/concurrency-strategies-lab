// 1. ./gradlew test 실행 시 가장 처음 읽는 시작점
// = "Cucumber로 실행하고, features 파일은 저기, steps 파일은 여기" 라는 지도역할
package com.example.concurrency;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")     // <- "features 폴더 찾아봐"
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.example.concurrency")    // <- "steps 파일은 이 패키지에 있어"
public class CucumberTest {
}