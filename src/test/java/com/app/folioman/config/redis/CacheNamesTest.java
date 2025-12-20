package com.app.folioman.config.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.Test;

class CacheNamesTest {

    @Test
    void shouldHaveCorrectCacheNames() {
        assertThat(CacheNames.SCHEME_SEARCH_CACHE).isEqualTo("schemeSearchCache");
        assertThat(CacheNames.TRANSACTION_CACHE).isEqualTo("transactionCache");
    }

    @Test
    void shouldThrowAssertionErrorWhenInstantiated() throws Exception {
        Constructor<CacheNames> constructor = CacheNames.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException exception = assertThatExceptionOfType(InvocationTargetException.class)
                .isThrownBy(constructor::newInstance)
                .actual();

        assertThat(exception.getCause()).isInstanceOf(AssertionError.class);
        assertThat(exception.getCause().getMessage()).isEqualTo("Utility class, do not instantiate");
    }

    @Test
    void shouldBeUtilityClass() {
        assertThat(CacheNames.class.getDeclaredConstructors().length).isOne();
        assertThat(CacheNames.class.getDeclaredConstructors()[0].isAccessible()).isFalse();
    }
}
