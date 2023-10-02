package webserver;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class WebServer {
    private static final int DEFAULT_PORT = 80;
    private static final int DEFAULT_THREAD_NUM = 50;
    private static final Logger log = Logger.getLogger(WebServer.class.getName());

    public static void main(String[] args) throws IOException {
        int port = DEFAULT_PORT;
        //스레드 풀 생성
        ExecutorService service = Executors.newFixedThreadPool(DEFAULT_THREAD_NUM);

        if (args.length != 0) {
            port = Integer.parseInt(args[0]);
        }

        /* TCP 환영 소켓
        * 클라이언트의 연결이 수락되면 WebServer의 main메서드에서 ServerSocket을 생성하고 연결 기다림
        * 연결이 수락되면 RequestHandler를 생성하고 클라이언트 소켓을 넘겨줍니다.*/
        try (ServerSocket welcomeSocket = new ServerSocket(port)){

            // 연결 소켓
            Socket connection;
            while ((connection = welcomeSocket.accept()) != null) {
                // 스레드에 작업 전달
                service.submit(new RequestHandler(connection));
            }
        }

    }
}
