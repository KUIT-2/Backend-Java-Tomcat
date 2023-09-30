package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable{
    Socket connection;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());
    private static final String ROOT_URL = "./webapp";

    public RequestHandler(Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : " + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()){
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            DataOutputStream dos = new DataOutputStream(out);

            byte[] body = "Hello World".getBytes();

            String startLine[] = br.readLine().split(" ");
            String httpMethod = startLine[0]; // GET, POST, PUT, DELETE 등
            String requestTarget = startLine[1]; //HTTP Request가 전송되는 목표 주소

            int requestContentLength = 0;

            //header
            while (true){
                final String line = br.readLine();
                if(line.equals("")){ //blank line 이후부터는 body값이므로
                    break;
                }
                if (line.startsWith("Content-Length")) {
                    requestContentLength = Integer.parseInt(line.split(": ")[1]);
                    //parseInt : 문자열을 정수로 변환
                }
            }

            log.info(requestTarget.toString());
            //요구사항 1번
            if (httpMethod.equals("GET")&& (requestTarget.endsWith(".html") ||requestTarget.endsWith("/"))){
                body = Files.readAllBytes(Paths.get(ROOT_URL + "/index.html"));
            }

            response200Header(dos, body.length);
            responseBody(dos, body);

        } catch (IOException e) {
            log.log(Level.SEVERE,e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

}