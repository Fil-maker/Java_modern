package lesson5.contest.task1_safe_calc;

import java.io.*;

// Неверное решение для проверки тестов: не замечает переполнение long.
public class SafeCalcNoOverflow {

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int q = Integer.parseInt(in.readLine().trim());
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < q; i++) {
            String[] p = in.readLine().split(" ", -1);
            String res;
            if (p.length != 3) {
                res = "ERROR FORMAT";
            } else {
                try {
                    long a = Long.parseLong(p[0]), b = Long.parseLong(p[2]);
                    switch (p[1]) {
                        case "+": res = String.valueOf(a + b); break;
                        case "-": res = String.valueOf(a - b); break;
                        case "*": res = String.valueOf(a * b); break;
                        case "/": res = b == 0 ? "ERROR DIVISION" : String.valueOf(a / b); break;
                        case "%": res = b == 0 ? "ERROR DIVISION" : String.valueOf(a % b); break;
                        default: res = "ERROR OPERATION";
                    }
                } catch (NumberFormatException e) {
                    res = "ERROR NUMBER";
                }
            }
            out.append(res).append('\n');
        }
        System.out.print(out);
    }
}
