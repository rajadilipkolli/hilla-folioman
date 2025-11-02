package com.app.folioman.config.redis;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * A Redis serializer that compresses values using GZIP to save memory.
 * Only values larger than the threshold will be compressed.
 * This wrapper delegates the actual serialization to another serializer.
 *
 * @param <T> the type to serialize
 */
public class CompressedRedisSerializer<T> implements RedisSerializer<T> {

    private static final Logger log = LoggerFactory.getLogger(CompressedRedisSerializer.class);
    private static final int COMPRESSION_THRESHOLD_BYTES = 1024; // Only compress values > 1KB

    private final RedisSerializer<T> delegate;

    public CompressedRedisSerializer(RedisSerializer<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public byte[] serialize(T value) throws SerializationException {
        if (value == null) {
            return null;
        }

        try {
            // First serialize the value using the delegate
            byte[] serialized = delegate.serialize(value);

            // If delegate returned null, return a single-byte 'not compressed' marker
            if (serialized == null) {
                return new byte[] {0};
            }

            // Only compress if serialized value is above threshold
            if (serialized.length > COMPRESSION_THRESHOLD_BYTES) {
                byte[] compressed = compress(serialized);

                // Log compression ratio for monitoring
                if (log.isDebugEnabled()) {
                    double ratio = (double) compressed.length / serialized.length;
                    log.debug(
                            "Compressed value from {} bytes to {} bytes (ratio: {:.2f})",
                            serialized.length,
                            compressed.length,
                            ratio);
                }

                // Add a marker byte at the beginning to indicate compression
                byte[] result = new byte[compressed.length + 1];
                result[0] = 1; // Marker byte: 1 means compressed
                System.arraycopy(compressed, 0, result, 1, compressed.length);
                return result;
            } else {
                // Small values: add marker byte indicating no compression
                byte[] result = new byte[serialized.length + 1];
                result[0] = 0; // Marker byte: 0 means not compressed
                System.arraycopy(serialized, 0, result, 1, serialized.length);
                return result;
            }
        } catch (IOException e) {
            log.error("Error compressing Redis value", e);
            throw new SerializationException("Error compressing Redis value", e);
        }
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try {
            // Check the marker byte
            boolean isCompressed = bytes[0] == 1;

            // Extract the actual data without the marker byte
            byte[] data = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, data, 0, data.length);

            // Decompress if needed
            byte[] deserialized = isCompressed ? decompress(data) : data;

            // Delegate the deserialization
            return delegate.deserialize(deserialized);
        } catch (IOException e) {
            log.error("Error decompressing Redis value", e);
            throw new SerializationException("Error decompressing Redis value", e);
        }
    }

    private byte[] compress(byte[] input) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(input.length);
        try (GZIPOutputStream gzipStream = new GZIPOutputStream(byteStream)) {
            gzipStream.write(input);
        }
        return byteStream.toByteArray();
    }

    private byte[] decompress(byte[] compressed) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(compressed.length * 2);
        try (GZIPInputStream gzipStream = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = gzipStream.read(buffer)) != -1) {
                byteStream.write(buffer, 0, bytesRead);
            }
        }
        return byteStream.toByteArray();
    }
}
