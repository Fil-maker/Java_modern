package lesson5.contest.task2_bank;

import java.io.*;
import java.util.*;

// Неверное решение для проверки тестов: перевод не атомарный.
// Деньги списываются раньше, чем проверен счёт получателя, и при ошибке
// NO_ACCOUNT пропадают.
public class BankNotAtomic {

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int q = Integer.parseInt(in.readLine().trim());
        Map<String, Long> bal = new HashMap<>();
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < q; i++) {
            String[] p = in.readLine().split(" ");
            String res = "OK";
            switch (p[0]) {
                case "OPEN": {
                    long x = Long.parseLong(p[2]);
                    if (x < 0) res = "ERROR AMOUNT";
                    else if (bal.containsKey(p[1])) res = "ERROR EXISTS";
                    else bal.put(p[1], x);
                    break;
                }
                case "DEPOSIT": {
                    long x = Long.parseLong(p[2]);
                    if (x <= 0) res = "ERROR AMOUNT";
                    else if (!bal.containsKey(p[1])) res = "ERROR NO_ACCOUNT";
                    else bal.put(p[1], bal.get(p[1]) + x);
                    break;
                }
                case "WITHDRAW": {
                    long x = Long.parseLong(p[2]);
                    if (x <= 0) res = "ERROR AMOUNT";
                    else if (!bal.containsKey(p[1])) res = "ERROR NO_ACCOUNT";
                    else if (bal.get(p[1]) < x) res = "ERROR FUNDS " + (x - bal.get(p[1]));
                    else bal.put(p[1], bal.get(p[1]) - x);
                    break;
                }
                case "TRANSFER": {
                    long x = Long.parseLong(p[3]);
                    if (x <= 0) res = "ERROR AMOUNT";
                    else if (!bal.containsKey(p[1])) res = "ERROR NO_ACCOUNT";
                    else if (bal.get(p[1]) < x) res = "ERROR FUNDS " + (x - bal.get(p[1]));
                    else {
                        bal.put(p[1], bal.get(p[1]) - x);          // списали…
                        if (!bal.containsKey(p[2])) res = "ERROR NO_ACCOUNT";  // …а получателя нет
                        else bal.put(p[2], bal.get(p[2]) + x);
                    }
                    break;
                }
                default:
                    res = bal.containsKey(p[1]) ? String.valueOf(bal.get(p[1])) : "ERROR NO_ACCOUNT";
            }
            out.append(res).append('\n');
        }
        System.out.print(out);
    }
}
