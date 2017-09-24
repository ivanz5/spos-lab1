package com.ivanzhur;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Client {

    private AsynchronousSocketChannel clientA;
    private AsynchronousSocketChannel clientB;
    private Future<Void> futureA;
    private Future<Void> futureB;
    private static Client instance;

    private boolean result;
    private boolean isFunctionACalculated  = false;
    private boolean isFunctionBCalculated  = false;
    private long startTime;
    private long executionTime;

    private Client() {
        try {
            clientA = AsynchronousSocketChannel.open();
            clientB = AsynchronousSocketChannel.open();
            InetSocketAddress hostAddressA = new InetSocketAddress("localhost", 1000);
            InetSocketAddress hostAddressB = new InetSocketAddress("localhost", 1001);
            futureA = clientA.connect(hostAddressA);
            futureB = clientB.connect(hostAddressB);
            start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Client getInstance() {
        if (instance == null)
            instance = new Client();
        return instance;
    }

    private void start() {
        try {
            startTime = System.currentTimeMillis();
            futureA.get();
            futureB.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void execute() throws InterruptedException, ExecutionException {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        Future<Integer> readResultA = clientA.read(buffer);

        ByteBuffer buffer2 = ByteBuffer.allocate(1);
        Future<Integer> readResultB = clientB.read(buffer2);

        while (!readResultA.isDone() || !readResultB.isDone()) {
            if (!isFunctionACalculated && readResultA.isDone()) {
                try {
                    readResultA.get();
                    boolean result = buffer.array()[0] == 1;
                    if (!result) {
                        executionTime = System.currentTimeMillis() - startTime;
                        return;
                    }
                    isFunctionACalculated = true;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (!isFunctionBCalculated && readResultB.isDone()) {
                try {
                    readResultB.get();
                    boolean result = buffer2.array()[0] == 1;
                    if (!result) {
                        executionTime = System.currentTimeMillis() - startTime;
                        return;
                    }
                    isFunctionBCalculated = true;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        readResultA.get();
        readResultB.get();
        boolean resultA = buffer.array()[0] == 1;
        boolean resultB = buffer2.array()[0] == 1;

        isFunctionACalculated = true;
        isFunctionBCalculated = true;
        result = resultA && resultB;
        executionTime = System.currentTimeMillis() - startTime;
    }

    public void stop() {
        try {
            clientA.close();
            clientB.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean getResult() {
        return result;
    }

    public boolean isFunctionACalculated() {
        return isFunctionACalculated;
    }

    public boolean isFunctionBCalculated() {
        return isFunctionBCalculated;
    }

    public boolean isResultCalculated() {
        return isFunctionACalculated && isFunctionBCalculated;
    }

    public long getExecutionTime() {
        return executionTime;
    }
}
