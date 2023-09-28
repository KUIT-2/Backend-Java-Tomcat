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
    private String WEBAPP_PATH = "./webapp";
    private String Index_PATH = "/index.html";
    private String Form_PATH = "/user/form.html";

    //private String Signup_PATH = "/user/signup";

    public RequestHandler(Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : " + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()){
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            DataOutputStream dos = new DataOutputStream(out);
            String startline = br.readLine();
            String[] lines = startline.split(" ");
            String method = lines[0];
            String url = lines[1];
            System.out.println("url : " + url);
            while(true){
                String line = br.readLine();
                if(line.equals(""))
                {
                    System.out.println("---------------------------");
                    break;
                }
                System.out.println(line);
            }

            if(url.equals("/")){
                url = WEBAPP_PATH + Index_PATH;
            }
            if(url.equals("/user/form.html")){
                url = WEBAPP_PATH + Form_PATH;
            }





            byte[] body = Files.readAllBytes(Paths.get(url));
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