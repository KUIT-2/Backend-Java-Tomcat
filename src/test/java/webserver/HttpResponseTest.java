package webserver;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

class HttpResponseTest {

    @Test
    void 응답테스트() throws IOException {
        HttpResponse httpResponse = new HttpResponse(outputStreamToFile("src/test/resources/response.txt"));

        httpResponse.forward("/index.html");

    }
    private OutputStream outputStreamToFile(String path) throws IOException {
        return Files.newOutputStream(Paths.get(path));
    }


}