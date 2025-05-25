package com.app.folioman.portfolio.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FileTypeEnumTest {

    @Test
    void shouldHaveCorrectValues() {
        // Check that enum has the expected values
        assertThat(FileTypeEnum.values()).hasSize(3);
        assertThat(FileTypeEnum.valueOf("CAMS")).isEqualTo(FileTypeEnum.CAMS);
        assertThat(FileTypeEnum.valueOf("KARVY")).isEqualTo(FileTypeEnum.KARVY);
        assertThat(FileTypeEnum.valueOf("UNKNOWN")).isEqualTo(FileTypeEnum.UNKNOWN);
    }
}
