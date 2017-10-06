package com.ivanzhur;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Client {

    private AsynchronousSocketChannel client;
    private Future<Void> future;

    private BinaryFunction function;

    public Client(BinaryFunction function) {
        this.function = function;
        try {
            client = AsynchronousSocketChannel.open();
            InetSocketAddress hostAddressA = new InetSocketAddress("localhost", 2000);
            future = client.connect(hostAddressA);
            start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void start() {
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void readFunction() {
        try {
            if (client != null && client.isOpen()) {
                ByteBuffer buffer = ByteBuffer.allocate(1);
                Future<Integer> readResult = client.read(buffer);
                readResult.get();
                boolean functionResult = buffer.array()[0] == 1;
                buffer = ByteBuffer.allocate(8);
                long functionExecutionTime = buffer.getLong();
                function = new BinaryFunction(functionResult, functionExecutionTime);
                System.out.println("Function: " + functionResult + " " + functionExecutionTime);

                execute();
            }
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void execute() throws InterruptedException, ExecutionException {
        try {
            if (client != null && client.isOpen()) {
                boolean value = function.execute();
                System.out.println("F: " + value);
                byte b = value ? (byte)1 : (byte)0;
                byte[] bytes = new byte[]{b};
                ByteBuffer buffer = ByteBuffer.wrap(bytes);

                client.write(buffer);

                /*Future<Integer> writeResult = client.write(buffer);
                writeResult.get();
                buffer.clear();

                client.close();*/
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("Start client");

        boolean functionResult = Boolean.valueOf(args[0]);
        long functionExecutionTime = Long.valueOf(args[1]);

        Client client = new Client(new BinaryFunction(functionResult, functionExecutionTime));
        //client.readFunction();
        try {
            client.execute();
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static Process startProcess(boolean result, long time) throws IOException, InterruptedException {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = Client.class.getCanonicalName();

        String argResult = String.valueOf(result);
        String atrTime = String.valueOf(time);

        ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", classpath, className, argResult, atrTime);

        return builder.start();
    }
}
