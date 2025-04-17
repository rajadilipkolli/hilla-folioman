package com.app.folioman.mfschemes.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.app.folioman.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

class ApplicationPropertiesIT extends AbstractIntegrationTest {

    @Test
    void whenValidPropertiesProvided_thenBindingSucceeds() {
        assertThat(applicationProperties.getAmfi()).isNotNull();
        assertThat(applicationProperties.getAmfi().getScheme()).isNotNull();
        assertThat(applicationProperties.getAmfi().getScheme().getDataUrl())
                .isNotNull()
                .isEqualTo("https://portal.amfiindia.com/DownloadSchemeData_Po.aspx?mf=0");
        assertThat(applicationProperties.getBseStar()).isNotNull();
        assertThat(applicationProperties.getBseStar().getScheme()).isNotNull();
        assertThat(applicationProperties.getBseStar().getScheme().getDataUrl())
                .isNotNull()
                .isEqualTo("https://bsestarmf.in/RptSchemeMaster.aspx");
        assertThat(applicationProperties.getNav()).isNotNull();
        assertThat(applicationProperties.getNav().getAmfi()).isNotNull();
        assertThat(applicationProperties.getNav().getAmfi().getDataUrl())
                .isNotNull()
                .isEqualTo("https://www.amfiindia.com/spages/NAVAll.txt");
        assertThat(applicationProperties.getNav().getMfApi()).isNotNull();
        assertThat(applicationProperties.getNav().getMfApi().getDataUrl())
                .isNotNull()
                .isEqualTo("https://api.mfapi.in/mf/{schemeCode}");
    }
}
