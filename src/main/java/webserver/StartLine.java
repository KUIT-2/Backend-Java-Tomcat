package webserver;

public class StartLine {
    private final String method;
    private final String path;
    private final String version;

    StartLine(String method, String path, String version){
        this.method =  method;
        this.path = path;
        this.version = version;
    }

    public String getMethod(){
        return this.method;
    }

    public String getPath(){
        return this.path;
    }

    public String getVersion(){
        return this.version;
    }

}
