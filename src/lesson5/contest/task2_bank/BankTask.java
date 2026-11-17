package lesson5.contest.task2_bank;

import java.io.*;
import java.util.*;

public class BankTask {

    // ожидаемая ситуация — проверяемое исключение с подробностями
    static class InsufficientFundsException extends Exception {
        final long shortage;

        InsufficientFundsException(long shortage) {
            super("ERROR FUNDS " + shortage);
            this.shortage = shortage;
        }
    }

    // ошибки вызывающего — непроверяемое исключение с кодом
    static class BankException extends RuntimeException {
        BankException(String code) {
            super("ERROR " + code);
        }
    }

    static class Bank {
        private final Map<String, Long> balances = new HashMap<>();

        void open(String id, long x) {
            if (x < 0) throw new BankException("AMOUNT");
            if (balances.containsKey(id)) throw new BankException("EXISTS");
            balances.put(id, x);
        }

        void deposit(String id, long x) {
            positive(x);
            balances.put(id, balance(id) + x);
        }

        void withdraw(String id, long x) throws InsufficientFundsException {
            positive(x);
            long b = balance(id);
            if (b < x) throw new InsufficientFundsException(x - b);
            balances.put(id, b - x);
        }

        void transfer(String from, String to, long x) throws InsufficientFundsException {
            positive(x);
            balance(from);
            balance(to);          // всё проверили — теперь меняем
            withdraw(from, x);
            deposit(to, x);
        }

        long balance(String id) {
            Long b = balances.get(id);
            if (b == null) throw new BankException("NO_ACCOUNT");
            return b;
        }

        private static void positive(long x) {
            if (x <= 0) throw new BankException("AMOUNT");
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int q = Integer.parseInt(in.readLine().trim());
        Bank bank = new Bank();
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < q; i++) {
            String[] p = in.readLine().split(" ");
            String res = "OK";
            try {
                switch (p[0]) {
                    case "OPEN": bank.open(p[1], Long.parseLong(p[2])); break;
                    case "DEPOSIT": bank.deposit(p[1], Long.parseLong(p[2])); break;
                    case "WITHDRAW": bank.withdraw(p[1], Long.parseLong(p[2])); break;
                    case "TRANSFER": bank.transfer(p[1], p[2], Long.parseLong(p[3])); break;
                    default: res = String.valueOf(bank.balance(p[1]));
                }
            } catch (InsufficientFundsException | BankException e) {
                res = e.getMessage();
            }
            out.append(res).append('\n');
        }
        System.out.print(out);
    }
}
