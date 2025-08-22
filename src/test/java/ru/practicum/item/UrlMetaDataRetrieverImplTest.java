package ru.practicum.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.common.ItemRetrieverException;
import ru.practicum.item.UrlMetaDataRetriever.UrlMetadata;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlMetaDataRetrieverImplTest {

    @Mock
    private HttpClient mockHttpClient;

    private UrlMetaDataRetrieverImpl urlMetaDataRetriever;

    @BeforeEach
    void setUp() throws Exception {
        urlMetaDataRetriever = new UrlMetaDataRetrieverImpl(120);
        replaceHttpClientWithMock();
    }

    private void replaceHttpClientWithMock() throws Exception {
        Field clientField = UrlMetaDataRetrieverImpl.class.getDeclaredField("client");
        clientField.setAccessible(true);
        clientField.set(urlMetaDataRetriever, mockHttpClient);
    }

    // Вспомогательный класс для создания HttpResponse
    private static class MockHttpResponse<T> implements HttpResponse<T> {
        private final int statusCode;
        private final T body;
        private final HttpHeaders headers;
        private final URI uri;

        public MockHttpResponse(int statusCode, T body, HttpHeaders headers, URI uri) {
            this.statusCode = statusCode;
            this.body = body;
            this.headers = headers;
            this.uri = uri;
        }

        @Override
        public int statusCode() {
            return statusCode;
        }

        @Override
        public T body() {
            return body;
        }

        @Override
        public HttpHeaders headers() {
            return headers;
        }

        @Override
        public URI uri() {
            return uri;
        }

        @Override
        public HttpRequest request() {
            return null;
        }

        @Override
        public Optional<HttpResponse<T>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }

        @Override
        public Optional<SSLSession> sslSession() {
            return Optional.empty();
        }
    }

    // Создаем HttpHeaders с content-type
    private HttpHeaders createHeaders(String contentType) {
        return HttpHeaders.of(
                Map.of("Content-Type", List.of(contentType)),
                (s1, s2) -> true
        );
    }

    // Пустые заголовки
    private HttpHeaders emptyHeaders() {
        return HttpHeaders.of(Map.of(), (s1, s2) -> true);
    }

    @Test
    void retrieve_WithMalformedUrl_ShouldThrowException() {
        assertThrows(ItemRetrieverException.class,
                () -> urlMetaDataRetriever.retrieve("invalid url"));
    }

    @Test
    void retrieve_WithTextContentType_ShouldReturnTextMetadata() throws Exception {
        // Arrange
        String url = "https://example.com";
        URI uri = new URI(url);

        HttpResponse<Void> headResponse = new MockHttpResponse<>(
                200, null, createHeaders("text/html"), uri
        );

        HttpResponse<String> getResponse = new MockHttpResponse<>(
                200, "<html><head><title>Test Title</title></head><body></body></html>",
                createHeaders("text/html"), uri
        );

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(headResponse)
                .thenReturn(getResponse);

        // Act
        UrlMetadata result = urlMetaDataRetriever.retrieve(url);

        // Assert
        assertNotNull(result);
        assertEquals("Test Title", result.getTitle());
        assertEquals("text", result.getMimeType());
        assertEquals(url, result.getNormalUrl());
        assertFalse(result.isHasImage());
        assertFalse(result.isHasVideo());
    }

    @Test
    void retrieve_WithUnsupportedContentType_ShouldThrowException() throws Exception {
        // Arrange
        String url = "https://example.com/file.zip";
        URI uri = new URI(url);

        HttpResponse<Void> headResponse = new MockHttpResponse<>(
                200, null, createHeaders("application/zip"), uri
        );

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(headResponse);

        // Act & Assert
        assertThrows(ItemRetrieverException.class,
                () -> urlMetaDataRetriever.retrieve(url));
    }

    @Test
    void retrieve_WithHttpError_ShouldThrowException() throws Exception {
        // Arrange
        String url = "https://example.com";
        URI uri = new URI(url);

        HttpResponse<Void> headResponse = new MockHttpResponse<>(
                404, null, emptyHeaders(), uri
        );

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(headResponse);

        // Act & Assert
        assertThrows(ItemRetrieverException.class,
                () -> urlMetaDataRetriever.retrieve(url));
    }

    @Test
    void retrieve_WithUnauthorizedStatus_ShouldThrowException() throws Exception {
        // Arrange
        String url = "https://example.com";
        URI uri = new URI(url);

        HttpResponse<Void> headResponse = new MockHttpResponse<>(
                401, null, emptyHeaders(), uri
        );

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(headResponse);

        // Act & Assert
        assertThrows(ItemRetrieverException.class,
                () -> urlMetaDataRetriever.retrieve(url));
    }

    @Test
    void retrieve_WithIoException_ShouldThrowException() throws Exception {
        // Arrange
        String url = "https://example.com";

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("Connection failed"));

        // Act & Assert
        assertThrows(ItemRetrieverException.class,
                () -> urlMetaDataRetriever.retrieve(url));
    }

    @Test
    void retrieve_WithInterruptedException_ShouldThrowRuntimeException() throws Exception {
        // Arrange
        String url = "https://example.com";

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new InterruptedException("Interrupted"));

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> urlMetaDataRetriever.retrieve(url));

        assertTrue(Thread.interrupted());
    }

    @Test
    void retrieve_WithHtmlContainingImages_ShouldDetectImages() throws Exception {
        // Arrange
        String url = "https://example.com";
        URI uri = new URI(url);

        HttpResponse<Void> headResponse = new MockHttpResponse<>(
                200, null, createHeaders("text/html"), uri
        );

        HttpResponse<String> getResponse = new MockHttpResponse<>(
                200,
                "<html><head><title>Test</title></head><body>" +
                "<img src='image1.jpg'><img src='image2.png'>" +
                "</body></html>",
                createHeaders("text/html"), uri
        );

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(headResponse)
                .thenReturn(getResponse);

        // Act
        UrlMetadata result = urlMetaDataRetriever.retrieve(url);

        // Assert
        assertNotNull(result);
        assertTrue(result.isHasImage());
        assertFalse(result.isHasVideo());
    }

    @Test
    void retrieve_WithHtmlContainingVideos_ShouldDetectVideos() throws Exception {
        // Arrange
        String url = "https://example.com";
        URI uri = new URI(url);

        HttpResponse<Void> headResponse = new MockHttpResponse<>(
                200, null, createHeaders("text/html"), uri
        );

        HttpResponse<String> getResponse = new MockHttpResponse<>(
                200,
                "<html><head><title>Test</title></head><body>" +
                "<video src='video.mp4'></video>" +
                "</body></html>",
                createHeaders("text/html"), uri
        );

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(headResponse)
                .thenReturn(getResponse);

        // Act
        UrlMetadata result = urlMetaDataRetriever.retrieve(url);

        // Assert
        assertNotNull(result);
        assertFalse(result.isHasImage());
        assertTrue(result.isHasVideo());
    }

    @Test
    void retrieve_WithUnknownStatusCode_ShouldThrowException() throws Exception {
        // Arrange
        String url = "https://example.com";
        URI uri = new URI(url);

        HttpResponse<Void> headResponse = new MockHttpResponse<>(
                999, null, emptyHeaders(), uri
        );

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(headResponse);

        // Act & Assert
        assertThrows(ItemRetrieverException.class,
                () -> urlMetaDataRetriever.retrieve(url));
    }

    @Test
    void retrieve_WithRedirect_ShouldUseResolvedUrl() throws Exception {
        // Arrange
        String originalUrl = "https://example.com";
        URI resolvedUri = new URI("https://example.com/resolved");

        HttpResponse<Void> headResponse = new MockHttpResponse<>(
                200, null, createHeaders("text/html"), resolvedUri
        );

        HttpResponse<String> getResponse = new MockHttpResponse<>(
                200, "<html><head><title>Test</title></head></html>",
                createHeaders("text/html"), resolvedUri
        );

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(headResponse)
                .thenReturn(getResponse);

        // Act
        UrlMetadata result = urlMetaDataRetriever.retrieve(originalUrl);

        // Assert
        assertEquals(originalUrl, result.getNormalUrl());
        assertEquals(resolvedUri.toString(), result.getResolvedUrl());
    }

    @Test
    void retrieve_WithEmptyTitle_ShouldHandleGracefully() throws Exception {
        // Arrange
        String url = "https://example.com";
        URI uri = new URI(url);

        HttpResponse<Void> headResponse = new MockHttpResponse<>(
                200, null, createHeaders("text/html"), uri
        );

        HttpResponse<String> getResponse = new MockHttpResponse<>(
                200, "<html><head></head><body></body></html>",
                createHeaders("text/html"), uri
        );

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(headResponse)
                .thenReturn(getResponse);

        // Act
        UrlMetadata result = urlMetaDataRetriever.retrieve(url);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getTitle());
    }

    @Test
    void retrieve_WithTextPlainContentType_ShouldWork() throws Exception {
        // Arrange
        String url = "https://example.com/text.txt";
        URI uri = new URI(url);

        HttpResponse<Void> headResponse = new MockHttpResponse<>(
                200, null, createHeaders("text/plain"), uri
        );

        HttpResponse<String> getResponse = new MockHttpResponse<>(
                200, "Plain text content",
                createHeaders("text/plain"), uri
        );

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(headResponse)
                .thenReturn(getResponse);

        // Act
        UrlMetadata result = urlMetaDataRetriever.retrieve(url);

        // Assert
        assertNotNull(result);
        assertEquals("text", result.getMimeType());
    }

    // Тестируем методы handleImage и handleVideo отдельно через рефлексию
    @Test
    void handleImage_WithFileUri_ShouldReturnMetadata() throws Exception {
        // Arrange
        URI uri = new URI("file:///path/to/image.jpg");

        // Act через рефлексию
        Method handleImageMethod = UrlMetaDataRetrieverImpl.class.getDeclaredMethod("handleImage", URI.class);
        handleImageMethod.setAccessible(true);
        Object result = handleImageMethod.invoke(urlMetaDataRetriever, uri);

        // Assert
        assertNotNull(result);
        // Проверяем, что это UrlMetadataImpl с правильными полями
        Class<?> resultClass = result.getClass();
        Field titleField = resultClass.getDeclaredField("title");
        titleField.setAccessible(true);
        Field isHasImageField = resultClass.getDeclaredField("hasImage");
        isHasImageField.setAccessible(true);

        assertEquals("image.jpg", titleField.get(result));
        assertTrue((Boolean) isHasImageField.get(result));
    }

    @Test
    void handleVideo_WithFileUri_ShouldReturnMetadata() throws Exception {
        // Arrange
        URI uri = new URI("file:///path/to/video.mp4");

        // Act через рефлексию
        Method handleVideoMethod = UrlMetaDataRetrieverImpl.class.getDeclaredMethod("handleVideo", URI.class);
        handleVideoMethod.setAccessible(true);
        Object result = handleVideoMethod.invoke(urlMetaDataRetriever, uri);

        // Assert
        assertNotNull(result);
        // Проверяем, что это UrlMetadataImpl с правильными полями
        Class<?> resultClass = result.getClass();
        Field titleField = resultClass.getDeclaredField("title");
        titleField.setAccessible(true);
        Field isHasVideoField = resultClass.getDeclaredField("hasVideo");
        isHasVideoField.setAccessible(true);

        assertEquals("video.mp4", titleField.get(result));
        assertTrue((Boolean) isHasVideoField.get(result));
    }

    // Тесты для метода connect
    @Test
    void connect_WithSuccessfulResponse_ShouldReturnResponse() throws Exception {
        // Arrange
        URI uri = new URI("https://example.com");
        HttpResponse<Void> expectedResponse = new MockHttpResponse<>(200, null, emptyHeaders(), uri);

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(expectedResponse);

        // Act через рефлексию
        Method connectMethod = UrlMetaDataRetrieverImpl.class.getDeclaredMethod("connect",
                URI.class, String.class, HttpResponse.BodyHandler.class);
        connectMethod.setAccessible(true);

        HttpResponse<Void> result = (HttpResponse<Void>) connectMethod.invoke(
                urlMetaDataRetriever, uri, "HEAD", HttpResponse.BodyHandlers.discarding());

        // Assert
        assertNotNull(result);
        assertEquals(200, result.statusCode());
    }
}