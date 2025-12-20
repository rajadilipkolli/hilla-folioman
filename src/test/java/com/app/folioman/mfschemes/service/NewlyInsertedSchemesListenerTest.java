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
    void constructor_WithValidMFNavService_ShouldInitializeSuccessfully() {
        NewlyInsertedSchemesListener newListener = new NewlyInsertedSchemesListener(mfNavService);

        // Verify object is created successfully (implicit assertion)
    }

    @Test
    void onOrderResponseEvent_WithValidSchemesListContainingNonNullElements_ShouldCallProcessNavsAsync() {
        List<Long> schemesList = Arrays.asList(10001L, 12235L, 14456L);
        when(uploadedSchemesList.schemesList()).thenReturn(schemesList);

        listener.onOrderResponseEvent(uploadedSchemesList);

        verify(mfNavService).processNavsAsync(schemesList);
    }

    @Test
    void onOrderResponseEvent_WithSchemesListContainingNullElements_ShouldFilterNullsAndCallProcessNavsAsync() {
        List<Long> schemesListWithNulls = Arrays.asList(10001L, null, 12235L, null, 14456L);
        List<Long> expectedFilteredList = Arrays.asList(10001L, 12235L, 14456L);
        when(uploadedSchemesList.schemesList()).thenReturn(schemesListWithNulls);

        listener.onOrderResponseEvent(uploadedSchemesList);

        verify(mfNavService).processNavsAsync(expectedFilteredList);
    }

    @Test
    void onOrderResponseEvent_WithEmptySchemesList_ShouldCallProcessNavsAsyncWithEmptyList() {
        List<Long> emptyList = Collections.emptyList();
        when(uploadedSchemesList.schemesList()).thenReturn(emptyList);

        listener.onOrderResponseEvent(uploadedSchemesList);

        verify(mfNavService).processNavsAsync(emptyList);
    }

    @Test
    void onOrderResponseEvent_WithSchemesListContainingOnlyNulls_ShouldCallProcessNavsAsyncWithEmptyList() {
        List<Long> nullOnlyList = Arrays.asList(null, null, null);
        when(uploadedSchemesList.schemesList()).thenReturn(nullOnlyList);

        listener.onOrderResponseEvent(uploadedSchemesList);

        verify(mfNavService).processNavsAsync(Collections.emptyList());
    }
}
