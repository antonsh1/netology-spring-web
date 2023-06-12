package ru.smartjava.utils;

import ru.smartjava.classes.Request;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RequestParametersExtractor {

    private final Request request;
    public RequestParametersExtractor(Request request) {
        this.request = request;
    }

    public Path getFilePath() {
        return Path.of(".", "public", request.getPath());
    }

    public String getMimeType() {
        try {
            return  Files.probeContentType(getFilePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public long getFileSize() {
        try {
            return Files.size(getFilePath());
        } catch (IOException e) {
            return 0;
//            throw new RuntimeException(e);
        }
    }

    public String getHeader() {
        if(getFileSize() !=0 ) {
            return "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: " + getMimeType() + "\r\n" +
                    "Content-Length: " + getFileSize() + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";
        } else {
            return  "HTTP/1.1 404 Not Found\r\n" +
                    "Content-Length: 0\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";
        }

    }

    public String getEmptyHeader() {
        return "HTTP/1.1 200 OK\r\n" +
                "Connection: close\r\n" +
                "\r\n";
    }
    public String getFileContent() {
        try {
            return Files.readString(getFilePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
