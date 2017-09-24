package com.ivanzhur;

import javafx.scene.input.KeyCode;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class MainInputListener {

    private Process serverA;
    private Process serverB;
    private Client client;

    private Timer timer;

    private class ShowResultTask extends TimerTask {

        @Override
        public void run() {
            showCalculationResult();
        }
    }

    public static void main(String[] args) {
        MainInputListener program = new MainInputListener();
        try {
            program.execute();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MainInputListener() {
        LogManager.getLogManager().reset();
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);

        try {
            GlobalScreen.registerNativeHook();
        }
        catch (NativeHookException e) {
            e.printStackTrace();
        }

        GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
            @Override
            public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

            }

            @Override
            public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
                if (nativeKeyEvent.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
                    cancelCalculation();
                }
                System.out.println(nativeKeyEvent.getKeyCode());
            }

            @Override
            public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {

            }
        });
    }

    private void execute() throws IOException, InterruptedException {
        int testCase = 0;
        BinaryFunction functionA = BinaryFunction.getBinaryFunctionA(testCase);
        BinaryFunction functionB = BinaryFunction.getBinaryFunctionB(testCase);

        serverA = Server.start(1000, functionA);
        serverB = Server.start(1001, functionB);
        client = Client.getInstance();

        timer = new Timer();

        new Thread(() -> {
            try {
                client.execute();
                System.out.println(Thread.currentThread().getId());
                ShowResultTask timerTask = new ShowResultTask();
                timer.schedule(timerTask, 0);
            }
            catch (Exception e) {}
        }).start();
    }

    private void showCalculationResult() {
        timer.cancel();
        System.out.println("result thread:" + Thread.currentThread().getId());
        shutDownCalculation();
        System.out.println("Calculation completed");
        System.out.println("Result: " + client.getResult());
        System.out.println("Time: " + client.getExecutionTime());
    }

    private void cancelCalculation() {
        System.out.println("cancel thread:" + Thread.currentThread().getId());
        shutDownCalculation();

        if (client.isResultCalculated()) {
            System.out.println("Result found");
            System.out.println("Result: " + client.getResult());
            System.out.println("Time: " + client.getExecutionTime());
        }
        else {
            System.out.println("Calculation cancelled by user");
        }
    }

    private void shutDownCalculation() {
        serverA.destroy();
        serverB.destroy();
        client.stop();
    }
}
