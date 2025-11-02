package com.app.folioman.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuditableTest {

    private TestableAuditable auditable;
    private LocalDateTime testDate;

    @BeforeEach
    void setUp() {
        auditable = new TestableAuditable();
        testDate = LocalDateTime.now();
    }

    @Test
    void testGetCreatedBy_WhenNotSet_ShouldReturnNull() {
        assertNull(auditable.getCreatedBy());
    }

    @Test
    void testSetCreatedBy_ShouldSetValueAndReturnThis() {
        String creator = "testUser";

        TestableAuditable result = (TestableAuditable) auditable.setCreatedBy(creator);

        assertEquals(creator, auditable.getCreatedBy());
        assertSame(auditable, result);
    }

    @Test
    void testSetCreatedBy_WithNull_ShouldSetNull() {
        auditable.setCreatedBy("user");

        auditable.setCreatedBy(null);

        assertNull(auditable.getCreatedBy());
    }

    @Test
    void testGetCreatedDate_WhenNotSet_ShouldReturnNull() {
        assertNull(auditable.getCreatedDate());
    }

    @Test
    void testSetCreatedDate_ShouldSetValueAndReturnThis() {
        TestableAuditable result = (TestableAuditable) auditable.setCreatedDate(testDate);

        assertEquals(testDate, auditable.getCreatedDate());
        assertSame(auditable, result);
    }

    @Test
    void testSetCreatedDate_WithNull_ShouldSetNull() {
        auditable.setCreatedDate(testDate);

        auditable.setCreatedDate(null);

        assertNull(auditable.getCreatedDate());
    }

    @Test
    void testGetLastModifiedBy_WhenNotSet_ShouldReturnNull() {
        assertNull(auditable.getLastModifiedBy());
    }

    @Test
    void testSetLastModifiedBy_ShouldSetValueAndReturnThis() {
        String modifier = "testUser";

        TestableAuditable result = (TestableAuditable) auditable.setLastModifiedBy(modifier);

        assertEquals(modifier, auditable.getLastModifiedBy());
        assertSame(auditable, result);
    }

    @Test
    void testSetLastModifiedBy_WithNull_ShouldSetNull() {
        auditable.setLastModifiedBy("user");

        auditable.setLastModifiedBy(null);

        assertNull(auditable.getLastModifiedBy());
    }

    @Test
    void testGetLastModifiedDate_WhenNotSet_ShouldReturnNull() {
        assertNull(auditable.getLastModifiedDate());
    }

    @Test
    void testSetLastModifiedDate_ShouldSetValueAndReturnThis() {
        TestableAuditable result = (TestableAuditable) auditable.setLastModifiedDate(testDate);

        assertEquals(testDate, auditable.getLastModifiedDate());
        assertSame(auditable, result);
    }

    @Test
    void testSetLastModifiedDate_WithNull_ShouldSetNull() {
        auditable.setLastModifiedDate(testDate);

        auditable.setLastModifiedDate(null);

        assertNull(auditable.getLastModifiedDate());
    }

    @Test
    void testMethodChaining() {
        String user = "testUser";
        LocalDateTime created = LocalDateTime.now();
        LocalDateTime modified = LocalDateTime.now().plusMinutes(1);

        TestableAuditable result = (TestableAuditable) auditable
                .setCreatedBy(user)
                .setCreatedDate(created)
                .setLastModifiedBy(user)
                .setLastModifiedDate(modified);

        assertSame(auditable, result);
        assertEquals(user, auditable.getCreatedBy());
        assertEquals(created, auditable.getCreatedDate());
        assertEquals(user, auditable.getLastModifiedBy());
        assertEquals(modified, auditable.getLastModifiedDate());
    }

    private static class TestableAuditable extends Auditable<String> {}
}
