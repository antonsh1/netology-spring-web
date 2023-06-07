package ru.smartjava.classes;

import java.util.Objects;

public class Request {

    String method;
    String headers;
    String body;

    public Request() {
    }

    public Request(String headers) {
        this.headers = headers;
        this.method = getRequestType();
    }

    public boolean isHeaderGood() {
        return headers.split(" ").length == 3;
    }
    public String getPath() {
        return headers.split(" ")[1];
    }

    public String getRequestType() {
        return headers.split(" ")[0];
    }

    public String getMethod() {
        return method;
    }

    public String getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public Request(String method, String headers, String body) {
        this.method = method;
        this.headers = headers;
        this.body = body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return Objects.equals(method, request.method) && Objects.equals(headers, request.headers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, headers);
    }
}
