package webserver;

public class RequestHeader {
    private final int content_length;
    private final boolean cookie;

    RequestHeader(int content_length, boolean cookie){
        this.content_length = content_length;
        this.cookie = cookie;
    }

    public int getContent_length(){
        return this.content_length;
    }

    public boolean getCookie(){
        return this.cookie;
    }
}
