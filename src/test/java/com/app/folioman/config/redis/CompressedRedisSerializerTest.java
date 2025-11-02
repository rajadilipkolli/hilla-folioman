package com.app.folioman.config.redis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

@ExtendWith(MockitoExtension.class)
class CompressedRedisSerializerTest {

    @Mock
    private RedisSerializer<String> delegate;

    private CompressedRedisSerializer<String> compressedSerializer;

    @BeforeEach
    void setUp() {
        compressedSerializer = new CompressedRedisSerializer<>(delegate);
    }

    @Test
    void constructor_ShouldSetDelegate() {
        CompressedRedisSerializer<String> serializer = new CompressedRedisSerializer<>(delegate);
        assertNotNull(serializer);
    }

    @Test
    void serialize_WithNullValue_ShouldReturnNull() {
        byte[] result = compressedSerializer.serialize(null);
        assertNull(result);
        verifyNoInteractions(delegate);
    }

    @Test
    void serialize_WithSmallValue_ShouldNotCompress() {
        String smallValue = "test";
        byte[] smallSerialized = "test".getBytes();
        when(delegate.serialize(smallValue)).thenReturn(smallSerialized);

        byte[] result = compressedSerializer.serialize(smallValue);

        assertNotNull(result);
        assertEquals(0, result[0]); // Not compressed marker
        assertEquals(smallSerialized.length + 1, result.length);

        byte[] actualData = new byte[result.length - 1];
        System.arraycopy(result, 1, actualData, 0, actualData.length);
        assertArrayEquals(smallSerialized, actualData);
    }

    @Test
    void serialize_WithLargeValue_ShouldCompress() {
        String largeValue = "x".repeat(2000);
        byte[] largeSerialized = largeValue.getBytes();
        when(delegate.serialize(largeValue)).thenReturn(largeSerialized);

        byte[] result = compressedSerializer.serialize(largeValue);

        assertNotNull(result);
        assertEquals(1, result[0]); // Compressed marker
        assertTrue(result.length < largeSerialized.length + 1); // Should be smaller due to compression
    }

    @Test
    void serialize_WithNullSerializedValue_ShouldHandleGracefully() {
        String value = "test";
        when(delegate.serialize(value)).thenReturn(null);

        byte[] result = compressedSerializer.serialize(value);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(0, result[0]); // Not compressed marker
    }

    @Test
    void serialize_WithIOException_ShouldThrowSerializationException() {
        String value = "test";
        RedisSerializer<String> faultyDelegate = mock(RedisSerializer.class);
        when(faultyDelegate.serialize(value)).thenThrow(new RuntimeException("IO Error"));

        CompressedRedisSerializer<String> serializer = new CompressedRedisSerializer<>(faultyDelegate);

        assertThrows(RuntimeException.class, () -> serializer.serialize(value));
    }

    @Test
    void deserialize_WithNullBytes_ShouldReturnNull() {
        String result = compressedSerializer.deserialize(null);
        assertNull(result);
        verifyNoInteractions(delegate);
    }

    @Test
    void deserialize_WithEmptyBytes_ShouldReturnNull() {
        String result = compressedSerializer.deserialize(new byte[0]);
        assertNull(result);
        verifyNoInteractions(delegate);
    }

    @Test
    void deserialize_WithUncompressedData_ShouldDeserialize() {
        String expectedValue = "test";
        byte[] originalData = "test".getBytes();
        byte[] bytesWithMarker = new byte[originalData.length + 1];
        bytesWithMarker[0] = 0; // Not compressed marker
        System.arraycopy(originalData, 0, bytesWithMarker, 1, originalData.length);

        when(delegate.deserialize(originalData)).thenReturn(expectedValue);

        String result = compressedSerializer.deserialize(bytesWithMarker);

        assertEquals(expectedValue, result);
        verify(delegate).deserialize(originalData);
    }

    @Test
    void deserialize_WithCompressedData_ShouldDecompressAndDeserialize() throws Exception {
        String originalValue = "x".repeat(2000);
        byte[] originalSerialized = originalValue.getBytes();
        when(delegate.serialize(originalValue)).thenReturn(originalSerialized);
        when(delegate.deserialize(originalSerialized)).thenReturn(originalValue);

        // First serialize to get compressed data
        byte[] compressed = compressedSerializer.serialize(originalValue);

        // Then deserialize
        String result = compressedSerializer.deserialize(compressed);

        assertEquals(originalValue, result);
    }

    @Test
    void roundTrip_WithSmallValue_ShouldPreserveData() {
        String originalValue = "small test value";
        byte[] serialized = originalValue.getBytes();
        when(delegate.serialize(originalValue)).thenReturn(serialized);
        when(delegate.deserialize(serialized)).thenReturn(originalValue);

        byte[] compressed = compressedSerializer.serialize(originalValue);
        String result = compressedSerializer.deserialize(compressed);

        assertEquals(originalValue, result);
    }

    @Test
    void roundTrip_WithLargeValue_ShouldPreserveData() {
        String originalValue = "x".repeat(2000);
        byte[] serialized = originalValue.getBytes();
        when(delegate.serialize(originalValue)).thenReturn(serialized);
        when(delegate.deserialize(serialized)).thenReturn(originalValue);

        byte[] compressed = compressedSerializer.serialize(originalValue);
        String result = compressedSerializer.deserialize(compressed);

        assertEquals(originalValue, result);
    }

    @Test
    void deserialize_WithCorruptedCompressedData_ShouldThrowSerializationException() {
        byte[] corruptedData = new byte[10];
        corruptedData[0] = 1; // Compressed marker
        // Rest is random data that cannot be decompressed

        assertThrows(SerializationException.class, () -> compressedSerializer.deserialize(corruptedData));
    }

    @Test
    void serialize_WithExactThresholdSize_ShouldNotCompress() {
        String value = "x".repeat(1024);
        byte[] serialized = value.getBytes();
        when(delegate.serialize(value)).thenReturn(serialized);

        byte[] result = compressedSerializer.serialize(value);

        assertNotNull(result);
        assertEquals(0, result[0]); // Not compressed marker
    }

    @Test
    void serialize_WithJustAboveThreshold_ShouldCompress() {
        String value = "x".repeat(1025);
        byte[] serialized = value.getBytes();
        when(delegate.serialize(value)).thenReturn(serialized);

        byte[] result = compressedSerializer.serialize(value);

        assertNotNull(result);
        assertEquals(1, result[0]); // Compressed marker
    }
}
