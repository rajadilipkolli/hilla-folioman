@org.jspecify.annotations.NullMarked
## Add jSpecify support in Maven projects
If you are using Maven, then add the jspecify dependency in `pom.xml`.
In `pom.xml`, update or add the `maven-compiler-plugin`, to include the following configuration.

```xml
<dependencies>
    <dependency>
        <groupId>org.jspecify</groupId>
        <artifactId>jspecify</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.14.1</version>
            <configuration>
                <release>25</release>
                <encoding>UTF-8</encoding>
                <fork>true</fork>
                <compilerArgs>
                    <arg>-XDcompilePolicy=simple</arg>
                    <arg>--should-stop=ifError=FLOW</arg>
                    <arg>-Xplugin:ErrorProne -XepDisableAllChecks -Xep:NullAway:ERROR -XepOpt:NullAway:OnlyNullMarked -XepOpt:NullAway:JSpecifyMode=true</arg>
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED</arg>
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED</arg>
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED</arg>
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED</arg>
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED</arg>
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED</arg>
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED</arg>
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED</arg>
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED</arg>
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED</arg>
                </compilerArgs>
                <annotationProcessorPaths>\n                    <path>\n                        <groupId>com.google.errorprone</groupId>\n                        <artifactId>error_prone_core</artifactId>\n                        <version>2.42.0</version>\n                    </path>\n                    <path>\n                        <groupId>com.uber.nullaway</groupId>\n                        <artifactId>nullaway</artifactId>\n                        <version>0.12.12</version>\n                    </path>\n                </annotationProcessorPaths>\n            </configuration>\n        </plugin>\n    </plugins>\n</build>\n```\n\n## Add jSpecify support in Gradle projects\nIf you are using Gradle, then add the jspecify dependency.\nIn `build.gradle` or `build.gradle.kts`, update or add the following jspecify configuration.\n\n```groovy\nplugins {\n    id(\"net.ltgt.errorprone\") version \"4.3.0\"\n}\n\ntasks.withType(JavaCompile).configureEach {\n    options.errorprone {\n        disableAllChecks = true // Other error prone checks are disabled\n        option(\"NullAway:OnlyNullMarked\", \"true\") // Enable nullness checks only in null-marked code\n        error(\"NullAway\") // bump checks from warnings (default) to errors\n        option(\"NullAway:JSpecifyMode\", \"true\") // https://github.com/uber/NullAway/wiki/JSpecify-Support\n    }\n    // Keep a JDK 25 baseline\n    options.release = 25\n}\n\ndependencies {\n    implementation(\"org.jspecify:jspecify:1.0.0\")\n    errorprone(\"com.google.errorprone:error_prone_core:2.42.0\")\n    errorprone(\"com.uber.nullaway:nullaway:0.12.12\")\n}\n```\n\n## Add @NullMarked to package-info.java files\nIn every java package under the application main source code (`src/main/java`), \ncreate `package-info.java` if not exists already, and add the `@NullMarked` annotation as follows:\n\n```java\n@org.jspecify.annotations.NullMarked\npackage com.mycompnay.myproject;\n```\n\nIf `package-info.java` file already exists, update the file to add `@org.jspecify.annotations.NullMarked` annotation.\nDO NOT REMOVE ANY OTHER EXISTING CODE IN `package-info.java` FILE.\n\n## Verify jSpecify support\nIf python is installed, after adding the jSpecify support, run `scripts/verify_nullmarked.py` \nto check if all non-empty packages has `package-info.java` file or not.
