package ru.smartjava.server;

import ru.smartjava.classes.Request;
import ru.smartjava.interfaces.Handler;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server extends Thread {

    private final Integer SERVER_SOCKET_TIMEOUT = 500;
    private final ServerSocket serverSocket;
    private final int SERVER_THREAD_POOL_SIZE = 64;
    private final int SERVER_PORT = 8080;

    Map<String, Handler> errorHandlers = new HashMap<>();
    Map<String, Map<String, Handler>> handlersList = new HashMap<>();
//            Collections.emptyMap();
    private final ExecutorService requestProcessingPool = Executors.newFixedThreadPool(SERVER_THREAD_POOL_SIZE);

    public void addHandler(String method, String path, Handler handler) {
        handlersList.putIfAbsent(method, new HashMap<>());
        handlersList.get(method).put(path, handler);
//        handlersList.getOrDefault(method, new HashMap<>() {
//            {
//                put(path, handler);
//            }}).put(path, handler);
//        Objects.requireNonNull(handlersList.putIfAbsent(method, new HashMap<>() {
//            {
//                put(path, handler);
//            }
//        })).put(path, handler);
//        handlersList.get(method).put(path, handler);
//        handlersList.put(method, new HashMap<>() {
//            {
//                put(path, handler);
//            }
//        });
    }

    private void initServiceHandlers() {
        addServerHandler("BadRequest", (request, responseStream) -> {
            System.out.println("BadRequest");
            try {
                responseStream.write((
                        "HTTP/1.1 400 Bad Request\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                responseStream.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        addServerHandler("NotFound", (request, responseStream) -> {
            System.out.println("NotFound");
            try {
                responseStream.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                responseStream.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void addServerHandler(String error, Handler handler) {
        errorHandlers.put(error, handler);
    }

    public Server() {
        initServiceHandlers();
        System.out.println("Старт сервера");
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Сервер запущен");
        } catch (IOException e) {
            System.out.println("Ошибка открытия сокета: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {

        try {
            this.serverSocket.setSoTimeout(SERVER_SOCKET_TIMEOUT);
        } catch (IOException e1) {
            //
        }

        while (true) {
            try {
                final var socket = serverSocket.accept();
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                System.out.println("Подключение " + socket.getInetAddress() + " : " + socket.getPort());
                final String requestLine = in.readLine();
                Request request = new Request(requestLine);
                final BufferedOutputStream out;
                try {
                    out = new BufferedOutputStream(socket.getOutputStream());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (!request.isHeaderGood() || handlersList.isEmpty() || handlersList.get(request.getMethod()).isEmpty()) {
                    requestProcessingPool.execute(() -> errorHandlers.get("BadRequest").handle(new Request(), out));
                    continue;
                }

                if (handlersList.get(request.getMethod()).containsKey(request.getPath())) {
                    requestProcessingPool.execute(
                            () -> handlersList.get(request.getMethod()).get(request.getPath()).handle(request, out)
                    );
                } else {
                    requestProcessingPool.execute(
                            () -> errorHandlers.get("NotFound").handle(new Request(), out)
                    );
                }
            } catch (SocketTimeoutException te) {
                // Срабатывает каждые SOCKET_TIMEOUT миллисекунд
            } catch (IOException e) {
                System.out.println(e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

}

