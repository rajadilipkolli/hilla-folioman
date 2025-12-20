package com.app.folioman.shared;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuditableTest {

    private TestableAuditable auditable;
    private LocalDateTime testDate;

    @BeforeEach
    void setUp() {
        auditable = new TestableAuditable();
        testDate = LocalDateTime.of(2025, 11, 2, 0, 0, 0);
    }

    @Test
    void getCreatedByWhenNotSetShouldReturnNull() {
        assertThat(auditable.getCreatedBy()).isNull();
    }

    @Test
    void setCreatedByShouldSetValueAndReturnThis() {
        String creator = "testUser";

        TestableAuditable result = (TestableAuditable) auditable.setCreatedBy(creator);

        assertThat(auditable.getCreatedBy()).isEqualTo(creator);
        assertThat(result).isSameAs(auditable);
    }

    @Test
    void setCreatedByWithNullShouldSetNull() {
        auditable.setCreatedBy("user");

        auditable.setCreatedBy(null);

        assertThat(auditable.getCreatedBy()).isNull();
    }

    @Test
    void getCreatedDateWhenNotSetShouldReturnNull() {
        assertThat(auditable.getCreatedDate()).isNull();
    }

    @Test
    void setCreatedDateShouldSetValueAndReturnThis() {
        TestableAuditable result = (TestableAuditable) auditable.setCreatedDate(testDate);

        assertThat(auditable.getCreatedDate()).isEqualTo(testDate);
        assertThat(result).isSameAs(auditable);
    }

    @Test
    void setCreatedDateWithNullShouldSetNull() {
        auditable.setCreatedDate(testDate);

        auditable.setCreatedDate(null);

        assertThat(auditable.getCreatedDate()).isNull();
    }

    @Test
    void getLastModifiedByWhenNotSetShouldReturnNull() {
        assertThat(auditable.getLastModifiedBy()).isNull();
    }

    @Test
    void setLastModifiedByShouldSetValueAndReturnThis() {
        String modifier = "testUser";

        TestableAuditable result = (TestableAuditable) auditable.setLastModifiedBy(modifier);

        assertThat(auditable.getLastModifiedBy()).isEqualTo(modifier);
        assertThat(result).isSameAs(auditable);
    }

    @Test
    void setLastModifiedByWithNullShouldSetNull() {
        auditable.setLastModifiedBy("user");

        auditable.setLastModifiedBy(null);

        assertThat(auditable.getLastModifiedBy()).isNull();
    }

    @Test
    void getLastModifiedDateWhenNotSetShouldReturnNull() {
        assertThat(auditable.getLastModifiedDate()).isNull();
    }

    @Test
    void setLastModifiedDateShouldSetValueAndReturnThis() {
        TestableAuditable result = (TestableAuditable) auditable.setLastModifiedDate(testDate);

        assertThat(auditable.getLastModifiedDate()).isEqualTo(testDate);
        assertThat(result).isSameAs(auditable);
    }

    @Test
    void setLastModifiedDateWithNullShouldSetNull() {
        auditable.setLastModifiedDate(testDate);

        auditable.setLastModifiedDate(null);

        assertThat(auditable.getLastModifiedDate()).isNull();
    }

    @Test
    void methodChaining() {
        String user = "testUser";
        LocalDateTime created = LocalDateTime.now();
        LocalDateTime modified = LocalDateTime.now().plusMinutes(1);

        TestableAuditable result = (TestableAuditable) auditable
                .setCreatedBy(user)
                .setCreatedDate(created)
                .setLastModifiedBy(user)
                .setLastModifiedDate(modified);

        assertThat(result).isSameAs(auditable);
        assertThat(auditable.getCreatedBy()).isEqualTo(user);
        assertThat(auditable.getCreatedDate()).isEqualTo(created);
        assertThat(auditable.getLastModifiedBy()).isEqualTo(user);
        assertThat(auditable.getLastModifiedDate()).isEqualTo(modified);
    }

    private static class TestableAuditable extends Auditable<String> {}
}
