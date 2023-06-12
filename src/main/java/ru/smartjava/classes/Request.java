package ru.smartjava.classes;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class Request {

    String method;
    String requestLine;
    List<String> headers;
    String body;

    public Request() {
    }

    public Request(String requestLine) {
        this.requestLine = requestLine;
        this.method = getRequestType();
    }

    public void setHeaders(List<String> headers) {
        System.out.println("HEADERS: " + headers);
        this.headers = headers;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isHeaderGood() {
        return requestLine.split(" ").length == 3;
    }

    public String getPath() {
        try {
            URI uri = new URI(requestLine.split(" ")[1]);
            return uri.getPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<String> extractHeader(String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    public void printHeader(String header) {
        extractHeader(header).ifPresent(System.out::println);
    }
    public List<NameValuePair> getQueryParams()  {
        try {
            return URLEncodedUtils.parse(new URI(requestLine.split(" ")[1]).getQuery(), StandardCharsets.UTF_8);
        } catch (URISyntaxException ignored) {

        }
        return null;
    }
    public List<NameValuePair> getBodyQueryParams()  {
        if(extractHeader("Content-Type").isPresent()) {
            if(extractHeader("Content-Type").get().contains("text/plain") || extractHeader("Content-Type").get().contains("application/x-www-form-urlencoded")) {
                    return URLEncodedUtils.parse(body.replace("\r\n","&"), StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    public List<String> getBodyParams(String name)  {
        if(getQueryParams() != null) {
            return getBodyQueryParams().stream().filter(pair -> Objects.equals(pair.getName(), name)).map(NameValuePair::getValue).collect(Collectors.toList());
        }
        return null;
    }

    public List<String> getParams(String name)  {
        if(getQueryParams() != null) {
            return getQueryParams().stream().filter(pair -> Objects.equals(pair.getName(), name)).map(NameValuePair::getValue).collect(Collectors.toList());
        }
        return null;
    }

    public String getRequestType() {
        return requestLine.split(" ")[0];
    }

    public String getMethod() {
        return method;
    }

    public String getBody() {
        return body;
    }

    public Request(String method, String requestLine, String body) {
        this.method = method;
        this.requestLine = requestLine;
        this.body = body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return Objects.equals(method, request.method) && Objects.equals(requestLine, request.requestLine);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, requestLine);
    }
}
