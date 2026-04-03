package com.app.folioman.mfschemes.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.app.folioman.mfschemes.MFNavService;
import com.app.folioman.shared.UploadedSchemesList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NewlyInsertedSchemesListenerTest {

    @Mock
    private MFNavService mfNavService;

    @Mock
    private UploadedSchemesList uploadedSchemesList;

    private NewlyInsertedSchemesListener listener;

    @BeforeEach
    void setUp() {
        listener = new NewlyInsertedSchemesListener(mfNavService);
    }

    @Test
    void onOrderResponseEvent_WithValidSchemesListContainingNonNullElements_ShouldCallProcessNavsAsync() {
        List<Long> schemesList = Arrays.asList(10001L, 12235L, 14456L);
        when(uploadedSchemesList.schemesList()).thenReturn(schemesList);

        listener.onOrderResponseEvent(uploadedSchemesList);

        verify(mfNavService).processNavsAsync(schemesList);
    }

    @Test
    void onOrderResponseEvent_WithEmptySchemesList_ShouldCallProcessNavsAsyncWithEmptyList() {
        List<Long> emptyList = Collections.emptyList();
        when(uploadedSchemesList.schemesList()).thenReturn(emptyList);

        listener.onOrderResponseEvent(uploadedSchemesList);

        verify(mfNavService).processNavsAsync(emptyList);
    }
}
