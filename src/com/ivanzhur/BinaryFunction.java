package com.ivanzhur;

public class BinaryFunction {

    public static long EXECUTION_TIME_INFINITY = -1;

    private boolean result;
    private long executionTime;

    public BinaryFunction(boolean result, long executionTime) {
        this.result = result;
        this.executionTime = executionTime;
    }

    public boolean getResult() {
        return result;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public boolean execute() {
        try {
            if (executionTime == EXECUTION_TIME_INFINITY) {
                while (true) {
                    Thread.sleep(1000);
                }
            }
            else {
                Thread.sleep(executionTime);
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static BinaryFunction getBinaryFunctionA(int index) {
        switch (index) {
            case 0: return new BinaryFunction(true, 5000);
            case 1: return new BinaryFunction(true, 10000);
            case 2: return new BinaryFunction(false, 6000);
            case 3: return new BinaryFunction(true, EXECUTION_TIME_INFINITY);
            case 4: return new BinaryFunction(true, 4000);
            case 5: return new BinaryFunction(true, EXECUTION_TIME_INFINITY);
            default: return new BinaryFunction(false, 1000);
        }
    }

    public static BinaryFunction getBinaryFunctionB(int index) {
        switch (index) {
            case 0: return new BinaryFunction(true, 10000);
            case 1: return new BinaryFunction(true, 5000);
            case 2: return new BinaryFunction(true, EXECUTION_TIME_INFINITY);
            case 3: return new BinaryFunction(false, 6000);
            case 4: return new BinaryFunction(true, EXECUTION_TIME_INFINITY);
            case 5: return new BinaryFunction(true, 4000);
            default: return new BinaryFunction(false, 2000);
        }
    }
}
