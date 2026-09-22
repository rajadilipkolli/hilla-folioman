package com.app.folioman;

import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MigrationAntiPatternTest {

    /** Verifies that Liquibase migrations avoid unsupported database type patterns. */
    @Test
    void testMigrationFilesForAntiPatterns() throws Exception {
        Path migrationDir = Paths.get("src/main/resources/db/changelog/migration/");
        List<Path> xmlFiles;
        try (Stream<Path> paths = Files.list(migrationDir)) {
            xmlFiles = paths.filter(p -> p.toString().endsWith(".xml")).toList();
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();

        for (Path file : xmlFiles) {
            Document doc = builder.parse(file.toFile());
            NodeList columns = doc.getElementsByTagName("column");

            for (int i = 0; i < columns.getLength(); i++) {
                Element column = (Element) columns.item(i);
                String type = column.getAttribute("type");
                String name = column.getAttribute("name");

                if (type != null && !type.isEmpty()) {
                    String typeUpper = type.toUpperCase();

                    // 1. Assert no timestamp without timezone for audit/expiry
                    if (typeUpper.equals("TIMESTAMP")
                            && (name.equals("created_at")
                                    || name.equals("updated_at")
                                    || name.equals("lock_expires_at")
                                    || name.equals("expires_at"))) {
                        fail("Found hardcoded TIMESTAMP in file " + file.getFileName() + " for column " + name);
                    }

                    // 2. Assert no column uses FLOAT
                    if (typeUpper.startsWith("FLOAT")) {
                        fail("Found FLOAT type in file " + file.getFileName() + " for column " + name);
                    }

                    // 3. Assert no hardcoded varchar(255)
                    if (typeUpper.replace(" ", "").equals("VARCHAR(255)")) {
                        fail("Found hardcoded VARCHAR(255) in file " + file.getFileName() + " for column " + name);
                    }
                }
            }
        }
    }
}
