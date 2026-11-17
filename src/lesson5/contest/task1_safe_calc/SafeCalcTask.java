package lesson5.contest.task1_safe_calc;

import java.io.*;

public class SafeCalcTask {

    static class CalcException extends Exception {
        CalcException(String code) {
            super("ERROR " + code);
        }
    }

    static long number(String s) throws CalcException {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            throw new CalcException("NUMBER");
        }
    }

    static long eval(String line) throws CalcException {
        String[] p = line.split(" ", -1);
        if (p.length != 3) throw new CalcException("FORMAT");
        long a = number(p[0]);
        long b = number(p[2]);
        String op = p[1];
        if (!op.equals("+") && !op.equals("-") && !op.equals("*") && !op.equals("/") && !op.equals("%")) {
            throw new CalcException("OPERATION");
        }
        if ((op.equals("/") || op.equals("%")) && b == 0) throw new CalcException("DIVISION");
        try {
            switch (op) {
                case "+": return Math.addExact(a, b);
                case "-": return Math.subtractExact(a, b);
                case "*": return Math.multiplyExact(a, b);
                case "/":
                    if (a == Long.MIN_VALUE && b == -1) throw new ArithmeticException("overflow");
                    return a / b;
                default: return a % b;
            }
        } catch (ArithmeticException e) {
            throw new CalcException("OVERFLOW");
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int q = Integer.parseInt(in.readLine().trim());
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < q; i++) {
            String line = in.readLine();
            try {
                out.append(eval(line));
            } catch (CalcException e) {
                out.append(e.getMessage());
            }
            out.append('\n');
        }
        System.out.print(out);
    }
}
