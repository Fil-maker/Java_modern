package lesson5;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/*
 * Задача «Безопасный калькулятор»
 *
 * Файл (аргумент командной строки, по умолчанию src/lesson5/calc.txt)
 * содержит выражения вида  ЧИСЛО ОПЕРАЦИЯ ЧИСЛО  через пробелы.
 * Операции: + - * / ^ (степень). Числа могут быть дробными и отрицательными.
 * Пустые строки пропускаются.
 *
 * Для каждой строки выведите результат или понятное сообщение об ошибке
 * с номером строки. Программа не должна падать ни на одной строке.
 * Различайте ошибки:
 *   - неверный формат (не три части),
 *   - не число,
 *   - неизвестная операция,
 *   - деление на ноль.
 * Для этого заведите своё проверяемое исключение CalcException.
 * В конце выведите, сколько строк посчитано успешно и сколько с ошибками.
 * Если файла нет — одно сообщение и выход с кодом 1.
 *
 * Пример вывода для приложенного calc.txt:
 *   1: 12 + 30 = 42
 *   2: ошибка: деление на ноль
 *   3: 2 ^ 10 = 1024
 *   5: ошибка: не число «abc»
 *   6: 100 - 58 = 42
 *   7: ошибка: ожидалось «число операция число», получено 2 части
 *   8: 9 / 4 = 2.25
 *   9: ошибка: ожидалось «число операция число», получено 5 частей
 *   10: -8 * 0.5 = -4
 *   11: ошибка: неизвестная операция «&»
 *   Успешно: 5, с ошибками: 5
 */
public class SafeCalc {

    static class CalcException extends Exception {
        CalcException(String message) { super(message); }
        CalcException(String message, Throwable cause) { super(message, cause); }
    }

    static double parseNumber(String s) throws CalcException {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            throw new CalcException("не число «" + s + "»", e);
        }
    }

    static double eval(String expr) throws CalcException {
        String[] p = expr.trim().split("\\s+");
        if (p.length != 3) {
            throw new CalcException("ожидалось «число операция число», получено " + p.length + " " + parts(p.length));
        }
        double a = parseNumber(p[0]);
        double b = parseNumber(p[2]);
        return switch (p[1]) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> {
                // для double деление на 0 не бросает исключение, а даёт Infinity — проверяем сами
                if (b == 0) throw new CalcException("деление на ноль");
                yield a / b;
            }
            case "^" -> Math.pow(a, b);
            default -> throw new CalcException("неизвестная операция «" + p[1] + "»");
        };
    }

    static String parts(int n) {
        if (n % 10 == 1 && n % 100 != 11) return "часть";
        if (n % 10 >= 2 && n % 10 <= 4 && (n % 100 < 10 || n % 100 >= 20)) return "части";
        return "частей";
    }

    // 42.0 → "42", 2.25 → "2.25"
    static String format(double x) {
        return x == Math.rint(x) && !Double.isInfinite(x)
                ? String.valueOf((long) x)
                : String.valueOf(x);
    }

    public static void main(String[] args) {
        Path path = Path.of(args.length > 0 ? args[0] : "src/lesson5/calc.txt");

        List<String> lines;
        try {
            lines = Files.readAllLines(path);
        } catch (IOException e) {
            System.out.println("Не удалось прочитать файл " + path + ": " + e);
            System.exit(1);
            return;   // компилятор не знает, что exit не возвращается
        }

        int ok = 0, bad = 0;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) continue;
            try {
                double r = eval(line);
                System.out.println((i + 1) + ": " + line.trim() + " = " + format(r));
                ok++;
            } catch (CalcException e) {
                System.out.println((i + 1) + ": ошибка: " + e.getMessage());
                bad++;
            }
        }
        System.out.println("Успешно: " + ok + ", с ошибками: " + bad);
    }
}
