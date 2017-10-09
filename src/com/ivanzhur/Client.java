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

        int testCase = Integer.valueOf(args[0]);
        int position = Integer.valueOf(args[1]);

        BinaryFunction function = position == 0
                ? BinaryFunction.getBinaryFunctionA(testCase)
                : BinaryFunction.getBinaryFunctionB(testCase);

        Client client = new Client(function);
        try {
            client.execute();
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static Process startProcess(int testCase, int testFunction) throws IOException, InterruptedException {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = Client.class.getCanonicalName();

        String argTestCase = String.valueOf(testCase);
        String argPosition = String.valueOf(testFunction);

        ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", classpath, className, argTestCase, argPosition);

        return builder.start();
    }
}
