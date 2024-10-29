package com.app.folioman.mfschemes.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.folioman.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class ApplicationPropertiesIT extends AbstractIntegrationTest {

    @Test
    void whenValidPropertiesProvided_thenBindingSucceeds() {
        assertThat(properties.getAmfi()).isNotNull();
        assertThat(properties.getAmfi().getScheme()).isNotNull();
        assertThat(properties.getAmfi().getScheme().getDataUrl())
                .isNotNull()
                .isEqualTo("https://portal.amfiindia.com/DownloadSchemeData_Po.aspx?mf=0");
        assertThat(properties.getBseStar()).isNotNull();
        assertThat(properties.getBseStar().getScheme()).isNotNull();
        assertThat(properties.getBseStar().getScheme().getDataUrl())
                .isNotNull()
                .isEqualTo("https://bsestarmf.in/RptSchemeMaster.aspx");
        assertThat(properties.getNav()).isNotNull();
        assertThat(properties.getNav().getAmfi()).isNotNull();
        assertThat(properties.getNav().getAmfi().getDataUrl())
                .isNotNull()
                .isEqualTo("https://www.amfiindia.com/spages/NAVAll.txt");
        assertThat(properties.getNav().getMfApi()).isNotNull();
        assertThat(properties.getNav().getMfApi().getDataUrl())
                .isNotNull()
                .isEqualTo("https://api.mfapi.in/mf/{schemeCode}");
    }
}
