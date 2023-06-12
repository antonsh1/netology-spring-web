package ru.smartjava.server;

import ru.smartjava.classes.Request;
import ru.smartjava.init.Methods;
import ru.smartjava.interfaces.Handler;

import java.io.*;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server extends Thread {

    private final Integer SERVER_SOCKET_TIMEOUT = 500;
    private final ServerSocket serverSocket;
    private final int SERVER_THREAD_POOL_SIZE = 64;
    private final int SERVER_PORT = 8080;

    Map<String, Handler> errorHandlers = new HashMap<>();
    Map<String, Map<String, Handler>> handlersList = new HashMap<>();
    private final ExecutorService requestProcessingPool = Executors.newFixedThreadPool(SERVER_THREAD_POOL_SIZE);
    public void addHandler(String method, String path, Handler handler) {
        handlersList.putIfAbsent(method, new HashMap<>());
        handlersList.get(method).put(path, handler);
    }

    private void initServiceHandlers()  {
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

        addServerHandler("GoodOk", (request, responseStream) -> {
            System.out.println("GoodOk");
            try {
                responseStream.write((
                        "HTTP/1.1 200 OK\r\n" +
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
                final var in = new BufferedInputStream(socket.getInputStream());
//                System.out.println("Подключение " + socket.getInetAddress() + " : " + socket.getPort());
                final var limit = 4096;

                in.mark(limit);
                final var buffer = new byte[limit];
                final var read = in.read(buffer);

                final var requestLineDelimiter = new byte[]{'\r', '\n'};
                final int requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);

                final BufferedOutputStream out;
                try {
                    out = new BufferedOutputStream(socket.getOutputStream());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                // ищем заголовки
                final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
                final var headersStart = requestLineEnd + requestLineDelimiter.length;
                final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
                //Сохраняем requestLine
                Request request = new Request(new String(Arrays.copyOf(buffer, requestLineEnd)));
                System.out.println(request.getPath());
                System.out.println(request.getMethod());
                if (requestLineEnd == -1 || headersEnd == -1 || !request.isHeaderGood() || !Methods.allowed.contains(request.getMethod()) || !request.getPath().startsWith("/") || handlersList.isEmpty() || !handlersList.containsKey(request.getMethod())) {
                    requestProcessingPool.execute(() -> errorHandlers.get("BadRequest").handle(new Request(), out));
                    continue;
                }
                // отматываем на начало буфера
                in.reset();
                // пропускаем requestLine
                in.skip(headersStart);
                final var headersBytes = in.readNBytes(headersEnd - headersStart);
                request.setHeaders(Arrays.asList(new String(headersBytes).split("\r\n")));
                request.printHeader("Content-Length");
                request.printHeader("Content-Type");
                //Сохраняем тело запроса
                if (request.extractHeader("Content-Length").isPresent()) {
                    final int length = Integer.parseInt(request.extractHeader("Content-Length").get());
                    in.skip(headersDelimiter.length);
                    request.setBody(new String(in.readNBytes(length)));
                }

                //Выбор обработчика
//                Optional<String> mainPath = handlersList.get(request.getMethod()).keySet().stream().filter(s -> request.getPath().startsWith(s)).findFirst();
                Optional<Map.Entry<String,Handler>> handlerItem = handlersList.get(request.getMethod()).entrySet().stream().filter(it -> request.getPath().startsWith(it.getKey())).findFirst();
                if(handlerItem.isPresent()) {
                    System.out.println("Handler Found: " + handlerItem.get().getKey());
                    requestProcessingPool.execute(
                            () -> handlerItem.get().getValue().handle(request, out)
                    );
                }else {
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
    // from google guava with modifications
    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}

