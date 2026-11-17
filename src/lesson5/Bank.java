package lesson5;

import java.util.*;

/*
 * Задача «Банк»
 *
 * Реализуйте класс Bank со счетами (номер → баланс в копейках, long) и методами
 *   open(String id, long initial)           — открыть счёт;
 *   deposit(String id, long amount)         — пополнить;
 *   withdraw(String id, long amount)        — снять;
 *   transfer(String from, String to, long amount) — перевести.
 * Правила:
 *   - сумма должна быть > 0, иначе IllegalArgumentException;
 *   - неизвестный счёт — своё непроверяемое AccountNotFoundException;
 *   - открыть уже существующий счёт — IllegalStateException;
 *   - не хватает денег — своё ПРОВЕРЯЕМОЕ InsufficientFundsException,
 *     в котором есть поле: сколько не хватает;
 *   - перевод атомарный: если он не удался, ни один баланс не меняется.
 * Почему так: нехватка денег — нормальная ситуация, которую вызывающий код
 * обязан обработать; остальное — ошибки в вызывающем коде.
 *
 * В main выполните сценарий из массива команд, печатая результат каждой
 * команды или ошибку, и в конце — все балансы.
 *
 * Пример вывода:
 *   open alice 1000.00          → ok
 *   open bob 250.00             → ok
 *   transfer alice bob 300.00   → ok
 *   withdraw bob 600.00         → не хватает 50.00
 *   transfer bob carol 10.00    → нет счёта carol
 *   deposit alice -5.00         → сумма должна быть положительной: -5.00
 *   open bob 1.00               → счёт bob уже существует
 *   transfer bob alice 550.00   → ok
 *   withdraw alice 1250.00      → ok
 *   oops alice                  → неизвестная команда
 *   deposit alice abc           → неверное число «abc»
 *   Балансы: {alice=0.00, bob=0.00}
 */
public class Bank {

    static class InsufficientFundsException extends Exception {
        final long shortage;

        InsufficientFundsException(String id, long shortage) {
            super("на счёте " + id + " не хватает " + money(shortage));
            this.shortage = shortage;
        }
    }

    static class AccountNotFoundException extends RuntimeException {
        AccountNotFoundException(String id) { super("нет счёта " + id); }
    }

    private final Map<String, Long> balances = new TreeMap<>();

    void open(String id, long initial) {
        if (initial < 0) throw new IllegalArgumentException("начальный баланс отрицательный");
        if (balances.putIfAbsent(id, initial) != null) {
            throw new IllegalStateException("счёт " + id + " уже существует");
        }
    }

    void deposit(String id, long amount) {
        checkAmount(amount);
        balances.put(id, balance(id) + amount);
    }

    void withdraw(String id, long amount) throws InsufficientFundsException {
        checkAmount(amount);
        long b = balance(id);
        if (b < amount) throw new InsufficientFundsException(id, amount - b);
        balances.put(id, b - amount);
    }

    void transfer(String from, String to, long amount) throws InsufficientFundsException {
        checkAmount(amount);
        balance(to);              // сначала проверяем ВСЁ, что может сломаться,
        withdraw(from, amount);   // и только потом меняем состояние
        deposit(to, amount);
    }

    long balance(String id) {
        Long b = balances.get(id);
        if (b == null) throw new AccountNotFoundException(id);
        return b;
    }

    private static void checkAmount(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("сумма должна быть положительной: " + money(amount));
        }
    }

    static String money(long kopecks) {
        return String.format(Locale.ROOT, "%s%d.%02d", kopecks < 0 ? "-" : "",
                Math.abs(kopecks) / 100, Math.abs(kopecks) % 100);
    }

    static long parseMoney(String s) {
        try {
            return Math.round(Double.parseDouble(s) * 100);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("неверное число «" + s + "»", e);
        }
    }

    void execute(String command) throws InsufficientFundsException {
        String[] p = command.split(" ");
        switch (p[0]) {
            case "open" -> open(p[1], parseMoney(p[2]));
            case "deposit" -> deposit(p[1], parseMoney(p[2]));
            case "withdraw" -> withdraw(p[1], parseMoney(p[2]));
            case "transfer" -> transfer(p[1], p[2], parseMoney(p[3]));
            default -> throw new UnsupportedOperationException("неизвестная команда");
        }
    }

    public static void main(String[] args) {
        String[] script = {
                "open alice 1000.00",
                "open bob 250.00",
                "transfer alice bob 300.00",
                "withdraw bob 600.00",
                "transfer bob carol 10.00",
                "deposit alice -5.00",
                "open bob 1.00",
                "transfer bob alice 550.00",
                "withdraw alice 1250.00",
                "oops alice",
                "deposit alice abc",
        };

        Bank bank = new Bank();
        for (String cmd : script) {
            String result;
            try {
                bank.execute(cmd);
                result = "ok";
            } catch (InsufficientFundsException e) {
                result = "не хватает " + money(e.shortage);
            } catch (AccountNotFoundException | IllegalArgumentException
                     | IllegalStateException | UnsupportedOperationException e) {
                result = e.getMessage();
            }
            System.out.printf("%-27s → %s%n", cmd, result);
        }

        Map<String, String> view = new TreeMap<>();
        bank.balances.forEach((id, b) -> view.put(id, money(b)));
        System.out.println("Балансы: " + view);
    }
}
