package webserver;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HttpResponse {
    public DataOutputStream dos;

    public HttpResponse(DataOutputStream dos) {
        this.dos = dos;
    }

    public HttpResponse(OutputStream outputStream) {}




    public byte[] forward(String path) throws IOException {
        String homepath = "./webapp";
        byte[] body = Files.readAllBytes(Paths.get(homepath + path));
        return body;
        //System.out.println(new String(body));
    }

    public byte[] redirect(String path) throws IOException {
        String homepath = "./webapp/user";
        byte[] body = Files.readAllBytes(Paths.get(homepath + path));
        return body;
        //System.out.println(new String(bytes));
    }
}
