# Maven Configuration for Spring Boot Project

This guide provides instructions on how to configure a Maven project for a Spring Boot application.

## Key principles
Follow these principles when using the Maven build tool for a Spring Boot application:

- Configure `spotless-maven-plugin` to automatically format the code and verify whether code is formatted correctly or not.
- Configure `jacoco-maven-plugin` to ensure tests are written meeting the desired code coverage level.
- Configure `git-commit-id-maven-plugin` to be able to expose the running code git commit information via Actuator.
- Configure `spring-boot-maven-plugin` to add build info to Actuator /info endpoint and specify default image name while building a Docker image using Paketo Buildpacks.

## pom.xml configuration
```xml
<properties>
    <spotless.version>3.2.0</spotless.version>
    <palantir-java-format.version>2.85.0</palantir-java-format.version>
    <jacoco-maven-plugin.version>0.8.14</jacoco-maven-plugin.version>
    <jacoco.minimum.coverage>80%</jacoco.minimum.coverage>
    <dockerImageName>{dockerhub_username}/${project.artifactId}</dockerImageName>
</properties>

<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <image>
                    <name>${dockerImageName}</name>
                </image>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>build-info</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
        <plugin>
            <groupId>io.github.git-commit-id</groupId>
            <artifactId>git-commit-id-maven-plugin</artifactId>
            <configuration>
                <failOnNoGitDirectory>false</failOnNoGitDirectory>
                <failOnUnableToExtractRepoInfo>false</failOnUnableToExtractRepoInfo>
                <generateGitPropertiesFile>true</generateGitPropertiesFile>
                <includeOnlyProperties>\n                    <includeOnlyProperty>^git.branch$</includeOnlyProperty>\n                    <includeOnlyProperty>^git.commit.id.abbrev$</includeOnlyProperty>\n                    <includeOnlyProperty>^git.commit.user.name$</includeOnlyProperty>\n                    <includeOnlyProperty>^git.commit.message.full$</includeOnlyProperty>\n                </includeOnlyProperties>\n            </configuration>\n            <executions>\n                <execution>\n                    <goals>\n                        <goal>revision</goal>\n                    </goals>\n                </execution>\n            </executions>\n        </plugin>\n        <plugin>\n            <groupId>org.jacoco</groupId>\n            <artifactId>jacoco-maven-plugin</artifactId>\n            <version>${jacoco-maven-plugin.version}</version>\n            <executions>\n                <!-- Attach JaCoCo agent -->\n                <execution>\n                    <goals>\n                        <goal>prepare-agent</goal>\n                    </goals>\n                </execution>\n                <!-- Generate report -->\n                <execution>\n                    <id>report</id>\n                    <phase>verify</phase>\n                    <goals>\n                        <goal>report</goal>\n                    </goals>\n                </execution>\n                <!-- Enforce coverage rule -->\n                <execution>\n                    <id>check</id>\n                    <phase>verify</phase>\n                    <goals>\n                        <goal>check</goal>\n                    </goals>\n                    <configuration>\n                        <rules>\n                            <rule>\n                                <element>BUNDLE</element>\n                                <limits>\n                                    <limit>\n                                        <counter>LINE</counter>\n                                        <value>COVEREDRATIO</value>\n                                        <minimum>${jacoco.minimum.coverage}</minimum>\n                                    </limit>\n                                </limits>\n                            </rule>\n                        </rules>\n                    </configuration>\n                </execution>\n            </executions>\n        </plugin>\n        <plugin>\n            <groupId>com.diffplug.spotless</groupId>\n            <artifactId>spotless-maven-plugin</artifactId>\n            <version>${spotless.version}</version>\n            <configuration>\n                <java>\n                    <importOrder/>\n                    <removeUnusedImports/>\n                    <formatAnnotations/>\n                    <palantirJavaFormat>\n                        <version>${palantir-java-format.version}</version>\n                    </palantirJavaFormat>\n                </java>\n            </configuration>\n            <executions>\n                <execution>\n                    <goals>\n                        <goal>check</goal>\n                    </goals>\n                    <phase>compile</phase>\n                </execution>\n            </executions>\n        </plugin>\n    </plugins>\n</build>\n```
