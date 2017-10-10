package com.ivanzhur;

import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class MainPeriodicPrompt {

    private static final int PROMPT_PERIOD = 3000;

    private Server server;
    private Scanner scanner;
    private Timer timer;
    private boolean isWaitingUserInput = false;
    private boolean isExecutingPrompt = false;

    private class PromptTimerTask extends TimerTask {

        @Override
        public void run() {
            isExecutingPrompt = true;

            boolean isACompleted = server.isFunctionACalculated();
            boolean isBCompleted = server.isFunctionBCalculated();
            if (isACompleted && isBCompleted) {
                showCalculationResult();
            }
            else {
                isWaitingUserInput = true;
                System.out.println("Options (keys):\nc - continue\nn - continue without prompt\ns - cancel calculation");
                System.out.println("Any other input considered as 'continue'");
                String command = scanner.next();
                switch (command) {
                    case "s":
                        cancelCalculation();
                        break;

                    case "n":
                        if (server.isResultCalculated()) showCalculationResult();
                        break;

                    default:
                        if (server.isResultCalculated()) {
                            showCalculationResult();
                        }
                        else {
                            PromptTimerTask timerTask = new PromptTimerTask();
                            timer.schedule(timerTask, PROMPT_PERIOD);
                        }
                        break;
                }

                isWaitingUserInput = false;
            }

            isExecutingPrompt = false;
        }
    }

    public static void main(String[] args) {
        MainPeriodicPrompt program = new MainPeriodicPrompt();
        program.execute();
    }

    private MainPeriodicPrompt() {
        scanner = new Scanner(System.in);
        timer = new Timer();
    }

    private void execute() {
        TimerTask task = new PromptTimerTask();
        timer.schedule(task, PROMPT_PERIOD);

        try {
            server = new Server();
            server.runServer();
            if (!isWaitingUserInput && !isExecutingPrompt) {
                timer.cancel();
                showCalculationResult();
            }
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
        timer.cancel();
    }
}
