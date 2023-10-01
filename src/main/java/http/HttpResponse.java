package http;

import Enum.*;
import java.io.DataOutputStream;

public class HttpResponse {
    private HttpVersion httpVersion;
    private HttpStatus statusCode;
    private Url redirectUrl;
    private byte[] body;

    public HttpResponse(DataOutputStream dos) {
        writeResponse(dos);
    }

    private void writeResponse(DataOutputStream dos) {

    }
}
