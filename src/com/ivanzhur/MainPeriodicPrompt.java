package com.ivanzhur;

import java.io.IOException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class MainPeriodicPrompt {

    private static final int PROMPT_PERIOD = 2000;

    private Process serverA;
    private Process serverB;
    private Client client;

    private Scanner scanner;
    private Timer timer;

    private class PromptTimerTask extends TimerTask {

        @Override
        public void run() {
            boolean isACompleted = client.isFunctionACalculated();
            boolean isBCompleted = client.isFunctionBCalculated();
            String statusA = isACompleted ? "Function A is calculated" : "Calculation of function A is in progress";
            String statusB = isBCompleted ? "Function B is calculated" : "Calculation of function B is in progress";
            if (isACompleted && isBCompleted) {
                showCalculationResult();
            }
            else {
                System.out.println(statusA);
                System.out.println(statusB);
                System.out.println("Options (keys):\nc - continue\nn - continue without prompt\ns - cancel calculation");
                System.out.println("Any other input considered as 'continue'");
                String command = scanner.next();
                switch (command) {
                    case "s":
                        cancelCalculation();
                        break;

                    case "n":
                        break;

                    default:
                        System.out.println("timer thread: " + Thread.currentThread().getId());
                        PromptTimerTask timerTask = new PromptTimerTask();
                        timer.schedule(timerTask, PROMPT_PERIOD);
                        break;
                }
            }
        }
    }

    private class ShowResultTask extends TimerTask {

        @Override
        public void run() {
            showCalculationResult();
        }
    }

    public static void main(String[] args) {
        MainPeriodicPrompt program = new MainPeriodicPrompt();
        try {
            program.execute();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MainPeriodicPrompt() {
        scanner = new Scanner(System.in);
        timer = new Timer();
    }

    private void execute() throws IOException, InterruptedException {
        int testCase = 0;
        BinaryFunction functionA = BinaryFunction.getBinaryFunctionA(testCase);
        BinaryFunction functionB = BinaryFunction.getBinaryFunctionB(testCase);

        serverA = Server.start(1000, functionA);
        serverB = Server.start(1001, functionB);
        client = Client.getInstance();

        new Thread(() -> {
            try {
                client.execute();
                System.out.println(Thread.currentThread().getId());
                ShowResultTask timerTask = new ShowResultTask();
                timer.schedule(timerTask, 0);
            }
            catch (Exception e) {}
        }).start();


        TimerTask task = new PromptTimerTask();
        timer.schedule(task, PROMPT_PERIOD);
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
        timer.cancel();
    }
}
