package webserver;

import db.MemoryUserRepository;
import model.User;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable{
    Socket connection;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());

    private static final String WEBAPP_PATH = "/Users/jaeyeon/KUIT_Missions/Backend-Java-Tomcat/webapp/";


    public RequestHandler(Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : " + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()){
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            DataOutputStream dos = new DataOutputStream(out);

            String line =  br.readLine();
            if (line != null && (line.startsWith("GET / ") || (line.startsWith("GET /index.html")))) {
                byte[] body = loadFile("index.html");
                if (body != null) {
                    response200Header(dos, body.length);
                    responseBody(dos, body);
                } else {
                    byte[] notFoundBody = "404 Not Found".getBytes();
                    response404Header(dos, notFoundBody.length);
                    responseBody(dos, notFoundBody);
                }
            }

            if (line != null && (line.startsWith("GET /user/form.html"))) {
                byte[] body = loadFile("user/form.html");
                if (body != null) {
                    response200Header(dos, body.length);
                    responseBody(dos, body);
                } else {
                    byte[] notFoundBody = "404 Not Found".getBytes();
                    response404Header(dos, notFoundBody.length);
                    responseBody(dos, notFoundBody);
                }
            }

            if(line != null && line.startsWith("GET /user/signup")) {
                String queryString = line.split("\\?")[1].split(" ")[0];
                Map<String, String> queryParams = parseQueryString(queryString);

                String userId = queryParams.get("userId");
                String password = queryParams.get("password");
                String name = queryParams.get("name");
                String email = queryParams.get("email");

                User newUser = new User(userId, password, name, email);
                System.out.println(newUser + "\n\n");
                MemoryUserRepository.getInstance().addUser(newUser);

                redirectResponse(dos, "/");
            }


        } catch (IOException e) {
            log.log(Level.SEVERE,e.getMessage());
        }
    }

    private Map<String, String> parseQueryString(String queryString) {
        Map<String, String> params = new HashMap<>();
        for (String param : queryString.split("&")) {
            String[] keyValue = param.split("=");
            if (keyValue.length > 1) {
                params.put(keyValue[0], keyValue[1]);
            }
        }

        return params;
    }


    private byte[] loadFile(String filename) throws IOException {
        File file = new File(WEBAPP_PATH + filename);
        if (file.exists() && file.isFile()) {
            try (FileInputStream fis = new FileInputStream(file);
                 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = fis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }

                return bos.toByteArray();
            }
        }
        return null;
    }

    private void redirectResponse(DataOutputStream dos, String locaion) {
        try{
            dos.writeBytes("HTTP/1.1 302Found \r\n");
            dos.writeBytes("Location: " + locaion + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
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

    private void response404Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 404 Not Found \r\n");
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