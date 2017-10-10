package com.ivanzhur;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class MainInputListener {

    private Server server;

    public static void main(String[] args) {
        MainInputListener program = new MainInputListener();
        program.execute();
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
            }

            @Override
            public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {

            }
        });
    }

    private void execute() {
        try {
            server = new Server();
            server.runServer();
            showCalculationResult();
        }
        catch (Exception e) {
        }
    }

    private void showCalculationResult() {
        shutDownCalculation();
        System.out.println("Calculation completed");
        System.out.println("Result: " + server.getResult());
        System.out.println("Time: " + server.getExecutionTime());
    }

    private void cancelCalculation() {
        shutDownCalculation();

        if (server.isResultCalculated()) {
            System.out.println("Result found");
            System.out.println("Result: " + server.getResult());
            System.out.println("Time: " + server.getExecutionTime());
        }
        else if (server.isFunctionACalculated()) {
            System.out.println("Calculation cancelled\nResult is not found because function B is not calculated");
        }
        else if (server.isFunctionBCalculated()) {
            System.out.println("Calculation cancelled\nResult is not found because function A is not calculated");
        }
        else {
            System.out.println("Calculation cancelled\nResult is not found because both functions are not calculated");
        }
    }

    private void shutDownCalculation() {
        server.stop();
        try {
            GlobalScreen.unregisterNativeHook();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
