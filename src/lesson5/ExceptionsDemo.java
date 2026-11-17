package lesson5;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/*
 * Исключения: иерархия, try/catch/finally, multi-catch,
 * try-with-resources, свои исключения, цепочки причин.
 */
public class ExceptionsDemo {

    // ===== Своё проверяемое (checked) исключение: наследуем Exception
    static class ConfigException extends Exception {
        private final int line;

        ConfigException(String message, int line) {
            super(message + " (строка " + line + ")");
            this.line = line;
        }

        ConfigException(String message, int line, Throwable cause) {
            super(message + " (строка " + line + ")", cause);   // cause — исходная причина
            this.line = line;
        }

        int line() { return line; }
    }

    // ===== Ресурс, который нужно закрыть: реализуем AutoCloseable
    static class Connection implements AutoCloseable {
        private final String name;

        Connection(String name) {
            this.name = name;
            System.out.println("  открыли " + name);
        }

        void send(String msg) {
            if (msg.isEmpty()) throw new IllegalArgumentException("пустое сообщение");
            System.out.println("  " + name + " ← " + msg);
        }

        @Override
        public void close() {
            System.out.println("  закрыли " + name);
        }
    }

    // throws в сигнатуре обязателен: ConfigException — checked
    static Map<String, Integer> parseConfig(List<String> lines) throws ConfigException {
        Map<String, Integer> cfg = new LinkedHashMap<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).strip();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] kv = line.split("=", 2);
            if (kv.length != 2) {
                throw new ConfigException("нет знака '='", i + 1);
            }
            try {
                cfg.put(kv[0].strip(), Integer.parseInt(kv[1].strip()));
            } catch (NumberFormatException e) {
                // заворачиваем низкоуровневое исключение в своё, сохраняя причину
                throw new ConfigException("не число: " + kv[1].strip(), i + 1, e);
            }
        }
        return cfg;
    }

    static int depth(int n) {
        return depth(n + 1);   // бесконечная рекурсия → StackOverflowError
    }

    @SuppressWarnings("finally")
    static int finallyWins() {
        try {
            return 1;
        } finally {
            System.out.println("  finally выполняется даже после return");
            // return 2;   // так делать нельзя: «съест» и результат, и исключение
        }
    }

    public static void main(String[] args) {
        // ===== 1. Непроверяемые (unchecked) исключения — ошибки программиста
        System.out.println("1. Типичные RuntimeException:");
        List<Runnable> bugs = List.of(
                () -> System.out.println(10 / 0),
                () -> { int[] a = new int[3]; a[5] = 1; },
                () -> System.out.println(((String) null).length()),
                () -> System.out.println(Integer.parseInt("12a")),
                () -> System.out.println(List.of(1, 2).get(5)),
                () -> List.of(1, 2).add(3),
                () -> { Object o = "строка"; Integer x = (Integer) o; }
        );
        for (Runnable bug : bugs) {
            try {
                bug.run();
            } catch (RuntimeException e) {
                System.out.println("  " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }

        // ===== 2. Порядок catch: от частного к общему
        System.out.println("2. Несколько catch и multi-catch:");
        for (String s : new String[]{"5", "0", "abc", null}) {
            try {
                int x = Integer.parseInt(s.strip());
                System.out.println("  100 / " + x + " = " + 100 / x);
            } catch (NumberFormatException | NullPointerException e) {   // multi-catch
                System.out.println("  плохой ввод: " + e.getClass().getSimpleName());
            } catch (ArithmeticException e) {
                System.out.println("  деление на ноль");
            } finally {
                System.out.println("  — finally для \"" + s + "\"");
            }
        }

        // ===== 3. finally и return
        System.out.println("3. finally и return:");
        System.out.println("  результат = " + finallyWins());

        // ===== 4. try-with-resources: close() вызывается автоматически,
        //          ресурсы закрываются в обратном порядке
        System.out.println("4. try-with-resources:");
        try (Connection a = new Connection("db"); Connection b = new Connection("log")) {
            a.send("SELECT 1");
            b.send("");                       // исключение — но оба ресурса закроются
        } catch (IllegalArgumentException e) {
            System.out.println("  поймали: " + e.getMessage());
        }

        // Файлы — тоже ресурсы. IOException — checked, его нельзя проигнорировать
        try (BufferedReader in = Files.newBufferedReader(Path.of("нет_такого_файла.txt"))) {
            System.out.println(in.readLine());
        } catch (NoSuchFileException e) {
            System.out.println("  файл не найден: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("  другая ошибка ввода-вывода: " + e);
        }

        // ===== 5. Своё checked-исключение и цепочка причин
        System.out.println("5. Своё исключение:");
        List<List<String>> configs = List.of(
                List.of("# настройки", "width = 80", "height=24"),
                List.of("width = 80", "height 24"),
                List.of("width = 80", "height = много"));
        for (List<String> cfg : configs) {
            try {
                System.out.println("  OK: " + parseConfig(cfg));
            } catch (ConfigException e) {
                System.out.println("  Ошибка: " + e.getMessage() + ", line()=" + e.line());
                if (e.getCause() != null) {
                    System.out.println("    причина: " + e.getCause());
                }
            }
        }

        // ===== 6. Error — не для перехвата в обычном коде, но посмотреть можно
        System.out.println("6. Error:");
        try {
            depth(0);
        } catch (StackOverflowError e) {
            System.out.println("  StackOverflowError — стек вызовов переполнен");
        }

        // ===== 7. Проверка аргументов: бросаем сами, как можно раньше
        System.out.println("7. Бросаем сами:");
        try {
            System.out.println("  " + Objects.requireNonNull(null, "имя не задано"));
        } catch (NullPointerException e) {
            System.out.println("  NPE: " + e.getMessage());
        }
        try {
            setAge(-3);
        } catch (IllegalArgumentException e) {
            System.out.println("  IAE: " + e.getMessage());
        }

        // ===== 8. Стек вызовов: откуда прилетело исключение
        System.out.println("8. Стек вызовов:");
        try {
            parseConfig(List.of("x = 1", "y = два"));
        } catch (ConfigException e) {
            for (StackTraceElement el : e.getStackTrace()) {
                System.out.println("  at " + el.getMethodName() + ":" + el.getLineNumber());
            }
        }
    }

    static void setAge(int age) {
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("возраст вне диапазона: " + age);
        }
    }
}
