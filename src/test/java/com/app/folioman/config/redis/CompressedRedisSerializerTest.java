package com.app.folioman.config.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
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
        assertThat(serializer).isNotNull();
    }

    @Test
    void serialize_WithNullValue_ShouldReturnNull() {
        byte[] result = compressedSerializer.serialize(null);
        assertThat(result).isNull();
        verifyNoInteractions(delegate);
    }

    @Test
    void serialize_WithSmallValue_ShouldNotCompress() {
        String smallValue = "test";
        byte[] smallSerialized = "test".getBytes();
        when(delegate.serialize(smallValue)).thenReturn(smallSerialized);

        byte[] result = compressedSerializer.serialize(smallValue);

        assertThat(result).isNotNull();
        assertThat(result[0]).isEqualTo(0); // Not compressed marker
        assertThat(result.length).isEqualTo(smallSerialized.length + 1);

        byte[] actualData = new byte[result.length - 1];
        System.arraycopy(result, 1, actualData, 0, actualData.length);
        assertThat(actualData).containsExactly(smallSerialized);
    }

    @Test
    void serialize_WithLargeValue_ShouldCompress() {
        String largeValue = "x".repeat(2000);
        byte[] largeSerialized = largeValue.getBytes();
        when(delegate.serialize(largeValue)).thenReturn(largeSerialized);

        byte[] result = compressedSerializer.serialize(largeValue);

        assertThat(result).isNotNull();
        assertThat(result[0]).isEqualTo(1); // Compressed marker
        assertThat(result.length).isLessThan(largeSerialized.length + 1); // Should be smaller due to compression
    }

    @Test
    void serialize_WithNullSerializedValue_ShouldHandleGracefully() {
        String value = "test";
        when(delegate.serialize(value)).thenReturn(null);

        byte[] result = compressedSerializer.serialize(value);

        assertThat(result).isNotNull();
        assertThat(result.length).isOne();
        assertThat(result[0]).isEqualTo(0); // Not compressed marker
    }

    @Test
    void serialize_WithIOException_ShouldThrowSerializationException() {
        String value = "test";
        RedisSerializer<String> faultyDelegate = mock(RedisSerializer.class);
        when(faultyDelegate.serialize(value)).thenThrow(new RuntimeException("IO Error"));

        CompressedRedisSerializer<String> serializer = new CompressedRedisSerializer<>(faultyDelegate);

        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> serializer.serialize(value));
    }

    @Test
    void deserialize_WithNullBytes_ShouldReturnNull() {
        String result = compressedSerializer.deserialize(null);
        assertThat(result).isNull();
        verifyNoInteractions(delegate);
    }

    @Test
    void deserialize_WithEmptyBytes_ShouldReturnNull() {
        String result = compressedSerializer.deserialize(new byte[0]);
        assertThat(result).isNull();
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

        assertThat(result).isEqualTo(expectedValue);
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

        assertThat(result).isEqualTo(originalValue);
    }

    @Test
    void roundTrip_WithSmallValue_ShouldPreserveData() {
        String originalValue = "small test value";
        byte[] serialized = originalValue.getBytes();
        when(delegate.serialize(originalValue)).thenReturn(serialized);
        when(delegate.deserialize(serialized)).thenReturn(originalValue);

        byte[] compressed = compressedSerializer.serialize(originalValue);
        String result = compressedSerializer.deserialize(compressed);

        assertThat(result).isEqualTo(originalValue);
    }

    @Test
    void roundTrip_WithLargeValue_ShouldPreserveData() {
        String originalValue = "x".repeat(2000);
        byte[] serialized = originalValue.getBytes();
        when(delegate.serialize(originalValue)).thenReturn(serialized);
        when(delegate.deserialize(serialized)).thenReturn(originalValue);

        byte[] compressed = compressedSerializer.serialize(originalValue);
        String result = compressedSerializer.deserialize(compressed);

        assertThat(result).isEqualTo(originalValue);
    }

    @Test
    void deserialize_WithCorruptedCompressedData_ShouldThrowSerializationException() {
        byte[] corruptedData = new byte[10];
        corruptedData[0] = 1; // Compressed marker
        // Rest is random data that cannot be decompressed

        assertThatExceptionOfType(SerializationException.class)
                .isThrownBy(() -> compressedSerializer.deserialize(corruptedData));
    }

    @Test
    void serialize_WithExactThresholdSize_ShouldNotCompress() {
        String value = "x".repeat(1024);
        byte[] serialized = value.getBytes();
        when(delegate.serialize(value)).thenReturn(serialized);

        byte[] result = compressedSerializer.serialize(value);

        assertThat(result).isNotNull();
        assertThat(result[0]).isEqualTo(0); // Not compressed marker
    }

    @Test
    void serialize_WithJustAboveThreshold_ShouldCompress() {
        String value = "x".repeat(1025);
        byte[] serialized = value.getBytes();
        when(delegate.serialize(value)).thenReturn(serialized);

        byte[] result = compressedSerializer.serialize(value);

        assertThat(result).isNotNull();
        assertThat(result[0]).isEqualTo(1); // Compressed marker
    }
}
