package com.app.folioman.config.redis;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.Test;

class CacheNamesTest {

    @Test
    void shouldHaveCorrectCacheNames() {
        assertEquals("schemeSearchCache", CacheNames.SCHEME_SEARCH_CACHE);
        assertEquals("transactionCache", CacheNames.TRANSACTION_CACHE);
    }

    @Test
    void shouldThrowAssertionErrorWhenInstantiated() throws Exception {
        Constructor<CacheNames> constructor = CacheNames.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);

        assertInstanceOf(AssertionError.class, exception.getCause());
        assertEquals("Utility class, do not instantiate", exception.getCause().getMessage());
    }

    @Test
    void shouldBeUtilityClass() {
        assertTrue(CacheNames.class.getDeclaredConstructors().length == 1);
        assertFalse(CacheNames.class.getDeclaredConstructors()[0].isAccessible());
    }
}
