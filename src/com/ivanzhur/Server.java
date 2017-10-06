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
        BinaryFunction functionA = BinaryFunction.getBinaryFunctionA(testCase);
        BinaryFunction functionB = BinaryFunction.getBinaryFunctionB(testCase);

        try {
            serverChannel = AsynchronousServerSocketChannel.open();
            InetSocketAddress hostAddress = new InetSocketAddress("localhost", 2000);
            serverChannel.bind(hostAddress);

            try {
                Client.startProcess(functionA.getResult(), functionA.getExecutionTime());
                Future<AsynchronousSocketChannel> acceptResultA = serverChannel.accept();
                clientChannelA = acceptResultA.get();
                Client.startProcess(functionB.getResult(), functionB.getExecutionTime());
                Future<AsynchronousSocketChannel> acceptResultB = serverChannel.accept();
                clientChannelB = acceptResultB.get();
                //writeFunctionsToClients();
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeFunctionsToClients() {
        try {
            boolean resultA = true;
            long waitTimeA = 1000;
            boolean resultB = false;
            long waitTimeB = 2000;
            byte b = resultA ? (byte) 1 : (byte) 0;
            byte[] bytes = new byte[]{b};
            ByteBuffer buffer = ByteBuffer.allocate(9);//.wrap(bytes);
            buffer.put(b);
            buffer.putLong(waitTimeA);
            Future<Integer> writeResult = clientChannelA.write(buffer);
            writeResult.get();
            buffer.clear();

            buffer.put((byte) 0);
            buffer.putLong(waitTimeB);
            writeResult = clientChannelB.write(buffer);
            writeResult.get();
            buffer.clear();
        }
        catch (InterruptedException | ExecutionException e) {
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
