package com.ivanzhur;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Server {

    private AsynchronousServerSocketChannel serverChannel;
    private Future<AsynchronousSocketChannel> acceptResult;
    private AsynchronousSocketChannel clientChannel;

    private BinaryFunction function;

    public Server(int port) {
        try {
            serverChannel = AsynchronousServerSocketChannel.open();
            InetSocketAddress hostAddress = new InetSocketAddress("localhost", port);
            serverChannel.bind(hostAddress);
            acceptResult = serverChannel.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setFunction(BinaryFunction function) {
        this.function = function;
    }

    private void runServer() {
        try {
            clientChannel = acceptResult.get();
            if (clientChannel != null && clientChannel.isOpen()) {
                boolean value = function.execute();
                byte b = value ? (byte)1 : (byte)0;
                byte[] bytes = new byte[]{b};
                ByteBuffer buffer = ByteBuffer.wrap(bytes);

                Future<Integer> writeResult = clientChannel.write(buffer);
                writeResult.get();
                buffer.clear();

                clientChannel.close();
                serverChannel.close();
            }
        } catch (InterruptedException | ExecutionException | IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        int port;
        boolean functionResult;
        long executionTime;
        try {
            port = Integer.valueOf(args[0]);
            functionResult = Boolean.valueOf(args[1]);
            executionTime = Long.valueOf(args[2]);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            port = 1000;
            functionResult = true;
            executionTime = 2000;
        }

        Server server = new Server(port);
        BinaryFunction function = new BinaryFunction(functionResult, executionTime);
        server.setFunction(function);
        server.runServer();
    }

    public static Process start(int port, BinaryFunction function) throws IOException, InterruptedException {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = Server.class.getCanonicalName();

        String portArg = String.valueOf(port);
        String argResult = String.valueOf(function.getResult());
        String argTime = String.valueOf(function.getExecutionTime());

        ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", classpath, className, portArg, argResult, argTime);

        return builder.start();
    }
}
