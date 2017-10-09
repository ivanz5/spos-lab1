package com.ivanzhur;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Server {

    private AsynchronousServerSocketChannel serverChannel;
    private AsynchronousSocketChannel clientChannelA;
    private AsynchronousSocketChannel clientChannelB;

    private boolean result;
    private boolean isFunctionACalculated  = false;
    private boolean isFunctionBCalculated  = false;
    private long executionTime;

    public Server() {
        int testCase = 0;

        try {
            serverChannel = AsynchronousServerSocketChannel.open();
            InetSocketAddress hostAddress = new InetSocketAddress("localhost", 2000);
            serverChannel.bind(hostAddress);

            try {
                Client.startProcess(testCase, 0);
                Future<AsynchronousSocketChannel> acceptResultA = serverChannel.accept();
                clientChannelA = acceptResultA.get();
                Client.startProcess(testCase, 1);
                Future<AsynchronousSocketChannel> acceptResultB = serverChannel.accept();
                clientChannelB = acceptResultB.get();
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void runServer() throws InterruptedException, ExecutionException {
        long startTime = System.currentTimeMillis();

        ByteBuffer bufferA = ByteBuffer.allocate(1);
        Future<Integer> readResultA = clientChannelA.read(bufferA);
        ByteBuffer bufferB = ByteBuffer.allocate(1);
        Future<Integer> readResultB = clientChannelB.read(bufferB);

        boolean resultA = true;
        boolean resultB = true;

        while (!isFunctionACalculated || !isFunctionBCalculated) {
            if (!isFunctionACalculated && readResultA.isDone()) {
                readResultA.get();
                resultA = bufferA.array()[0] == 1;
                //System.out.println("A : " + resultA);
                isFunctionACalculated = true;
                if (!resultA) {
                    result = false;
                    executionTime = System.currentTimeMillis() - startTime;
                    break;
                }
            }

            if (!isFunctionBCalculated && readResultB.isDone()) {
                readResultB.get();
                resultB = bufferB.array()[0] == 1;
                //System.out.println("B : " + resultB);
                isFunctionBCalculated = true;
                if (!resultB) {
                    result = false;
                    executionTime = System.currentTimeMillis() - startTime;
                    break;
                }
            }
        }

        isFunctionACalculated = true;
        isFunctionBCalculated = true;
        result = resultA && resultB;
        executionTime = System.currentTimeMillis() - startTime;
    }

    public boolean isFunctionACalculated() {
        return isFunctionACalculated;
    }

    public boolean isFunctionBCalculated() {
        return isFunctionBCalculated;
    }

    public boolean getResult() {
        return result;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public boolean isResultCalculated() {
        return isFunctionACalculated && isFunctionBCalculated;
    }

    public static void main(String[] args) {
        System.out.println("Start server");
        Server server = new Server();
        try {
            server.runServer();
            System.out.println("Result: " + server.result);
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            serverChannel.close();
            clientChannelA.close();
            clientChannelB.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
