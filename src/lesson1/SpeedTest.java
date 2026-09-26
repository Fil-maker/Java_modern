package lesson1;

import java.io.*;
import java.util.*;

public class SpeedTest {

    static final int N = 1_000_000;   // количество чисел
    static final String FILE = "input.txt";

    public static void main(String[] args) throws IOException {
        generateFile();

        // Прогрев JIT, чтобы первый способ не выглядел "медленным" из-за компиляции
        readWithScanner();
        readWithBufferedReader();
        readWithFastScanner();

        long sum1 = timed("Scanner",        SpeedTest::readWithScanner);
        long sum2 = timed("BufferedReader", SpeedTest::readWithBufferedReader);
        long sum3 = timed("FastScanner",    SpeedTest::readWithFastScanner);

        // Проверяем, что все способы дали одинаковую сумму
        if (sum1 != sum2 || sum2 != sum3) {
            System.out.println("!!! Суммы не совпали: " + sum1 + " / " + sum2 + " / " + sum3);
        } else {
            System.out.println("Все суммы совпали: " + sum1);
        }
    }

    // Универсальный замер времени
    interface Reader { long run() throws IOException; }

    static long timed(String label, Reader r) throws IOException {
        long start = System.nanoTime();
        long result = r.run();
        long elapsed = System.nanoTime() - start;
        System.out.printf("%-16s %10.2f мс%n", label + ":", elapsed / 1_000_000.0);
        return result;
    }

    // ---------- Генерация файла ----------
    static void generateFile() throws IOException {
        Random rnd = new Random(42);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE), 1 << 16);
             PrintWriter pw = new PrintWriter(bw)) {
            for (int i = 0; i < N; i++) {
                pw.print(rnd.nextInt(1000));
                // раз в 20 чисел — перевод строки, чтобы были разные строки
                pw.print(i % 20 == 19 ? '\n' : ' ');
            }
        }
        System.out.println("Файл сгенерирован: " + N + " чисел");
    }

    // ---------- 1. Scanner ----------
    static long readWithScanner() throws IOException {
        Scanner sc = new Scanner(new File(FILE));
        long sum = 0;
        while (sc.hasNextInt()) {
            sum += sc.nextInt();
        }
        sc.close();
        return sum;
    }

    // ---------- 2. BufferedReader + StringTokenizer ----------
    static long readWithBufferedReader() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(FILE), 1 << 16);
        long sum = 0;
        String line;
        while ((line = br.readLine()) != null) {
            StringTokenizer st = new StringTokenizer(line);
            while (st.hasMoreTokens()) {
                sum += Integer.parseInt(st.nextToken());
            }
        }
        br.close();
        return sum;
    }

    // ---------- 3. FastScanner ----------
    static long readWithFastScanner() throws IOException {
        FastScanner fs = new FastScanner(new FileInputStream(FILE));
        long sum = 0;
        int x;
        while ((x = fs.nextIntOrMinus()) != Integer.MIN_VALUE) {
            sum += x;
        }
        return sum;
    }

    // Быстрый сканер: читает байты, сам собирает числа
    static class FastScanner {
        private final InputStream in;
        private final byte[] buf = new byte[1 << 16];
        private int ptr = 0, len = 0;

        FastScanner(InputStream in) { this.in = in; }

        private int read() throws IOException {
            if (ptr >= len) {
                len = in.read(buf);
                ptr = 0;
                if (len <= 0) return -1;
            }
            return buf[ptr++];
        }

        // Возвращает Integer.MIN_VALUE, если данных больше нет
        int nextIntOrMinus() throws IOException {
            int c;
            // пропускаем пробелы
            do {
                c = read();
                if (c == -1) return Integer.MIN_VALUE;
            } while (c <= ' ');

            int sign = 1;
            if (c == '-') {
                sign = -1;
                c = read();
            }

            int num = 0;
            while (c > ' ') {
                num = num * 10 + (c - '0');
                c = read();
            }
            return num * sign;
        }
    }
}