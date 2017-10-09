package com.ivanzhur;

import java.io.IOException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class MainPeriodicPrompt {

    private static final int PROMPT_PERIOD = 2000;

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
            String statusA = isACompleted ? "Function A is calculated" : "Calculation of function A is in progress";
            String statusB = isBCompleted ? "Function B is calculated" : "Calculation of function B is in progress";
            if (isACompleted && isBCompleted) {
                showCalculationResult();
            }
            else {
                isWaitingUserInput = true;
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
                        if (server.isResultCalculated()) showCalculationResult();
                        break;

                    default:
                        if (server.isResultCalculated()) {
                            showCalculationResult();
                        }
                        else {
                            System.out.println("timer thread: " + Thread.currentThread().getId());
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
        else {
            System.out.println("Calculation cancelled by user");
        }
    }

    private void shutDownCalculation() {
        server.stop();
        timer.cancel();
    }
}
