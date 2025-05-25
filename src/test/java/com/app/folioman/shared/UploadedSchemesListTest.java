package com.app.folioman.shared;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class UploadedSchemesListTest {

    @Test
    void testCreationWithMultipleSchemes() {
        // Given a list of scheme IDs
        List<Long> schemeIds = Arrays.asList(1L, 2L, 3L);

        // When creating an UploadedSchemesList with the list
        UploadedSchemesList uploadedSchemesList = new UploadedSchemesList(schemeIds);

        // Then the list should be accessible via the accessor method
        assertThat(uploadedSchemesList.schemesList()).isNotNull().hasSize(3).containsExactly(1L, 2L, 3L);
    }

    @Test
    void testCreationWithEmptyList() {
        // Given an empty list
        List<Long> emptyList = Collections.emptyList();

        // When creating an UploadedSchemesList with the empty list
        UploadedSchemesList uploadedSchemesList = new UploadedSchemesList(emptyList);

        // Then the list should be accessible and empty
        assertThat(uploadedSchemesList.schemesList()).isNotNull().isEmpty();
    }

    @Test
    void testEquality() {
        // Given two UploadedSchemesList instances with the same content
        UploadedSchemesList list1 = new UploadedSchemesList(Arrays.asList(1L, 2L, 3L));
        UploadedSchemesList list2 = new UploadedSchemesList(Arrays.asList(1L, 2L, 3L));

        // Then they should be equal and have the same hash code
        assertThat(list1).isEqualTo(list2).hasSameHashCodeAs(list2);
    }

    @Test
    void testToString() {
        // Given an UploadedSchemesList
        UploadedSchemesList list = new UploadedSchemesList(Arrays.asList(1L, 2L, 3L));

        // When getting its string representation
        String stringRepresentation = list.toString();

        // Then it should contain the list values
        assertThat(stringRepresentation).contains("schemesList").contains("[1, 2, 3]");
    }
}
