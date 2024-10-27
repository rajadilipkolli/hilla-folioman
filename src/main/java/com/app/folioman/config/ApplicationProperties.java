package com.app.folioman.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {

    private Amfi amfi;
    private BseStar bseStar;
    private Nav nav;

    public Amfi getAmfi() {
        return amfi;
    }

    public void setAmfi(Amfi amfi) {
        this.amfi = amfi;
    }

    public BseStar getBseStar() {
        return bseStar;
    }

    public void setBseStar(BseStar bseStar) {
        this.bseStar = bseStar;
    }

    public Nav getNav() {
        return nav;
    }

    public void setNav(Nav nav) {
        this.nav = nav;
    }

    public static class BseStar {

        private Scheme scheme;

        public Scheme getScheme() {
            return scheme;
        }

        public void setScheme(Scheme scheme) {
            this.scheme = scheme;
        }
    }

    public static class Nav {

        private Amfi amfi;

        private MfApi mfApi;

        public Amfi getAmfi() {
            return amfi;
        }

        public void setAmfi(Amfi amfi) {
            this.amfi = amfi;
        }

        public MfApi getMfApi() {
            return mfApi;
        }

        public void setMfApi(MfApi mfApi) {
            this.mfApi = mfApi;
        }
    }

    public static class MfApi {
        private String dataUrl;

        public String getDataUrl() {
            return dataUrl;
        }

        public void setDataUrl(String dataUrl) {
            this.dataUrl = dataUrl;
        }
    }
}
