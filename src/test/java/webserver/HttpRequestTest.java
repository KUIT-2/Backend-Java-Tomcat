package webserver;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpRequestTest {
    @Test
    void 요청테스드() throws IOException {
        HttpRequest httpRequest = HttpRequest.from(bufferedReaderFromFile("src/test/resources/http.txt"));
        assertEquals("/user/create", httpRequest.getUrl());
    }

    private BufferedReader bufferedReaderFromFile(String path) throws IOException {
        return new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(path))));
    }

}