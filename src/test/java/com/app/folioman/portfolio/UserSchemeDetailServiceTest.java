package com.app.folioman.portfolio;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserSchemeDetailServiceTest {

    @Mock
    private UserSchemeDetailService userSchemeDetailService;

    @Test
    void setUserSchemeAMFIIfNull() {
        doNothing().when(userSchemeDetailService).setUserSchemeAMFIIfNull();

        userSchemeDetailService.setUserSchemeAMFIIfNull();

        verify(userSchemeDetailService).setUserSchemeAMFIIfNull();
    }
}
