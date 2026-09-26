package lesson1;

import java.util.Arrays;

public class DiffArray {
    public static void main(String[] args) {
        int n = 10;
        long[] diff = new long[n + 2]; // +2 для безопасного r+1

        // Операция: прибавить val к отрезку [l, r] включительно (0-индексы)
        // Пример: 3 запроса
        add(diff, 1, 5, 7);   // +7 на [1..5]
        add(diff, 3, 8, -2);  // -2 на [3..8]
        add(diff, 0, 9, 1);   // +1 на всё

        // Восстановление массива a
        long[] a = new long[n];
        long cur = 0;
        for (int i = 0; i < n; i++) {
            cur += diff[i];
            a[i] = cur;
        }
        System.out.println(Arrays.toString(a));
        // При подготовке использовался deepSeek.
        // Проверяйте, что вам там пишут в интернетах
        // [1, 8, 8, 13, 13, 13, 6, 6, 6, 6]
    }

    static void add(long[] diff, int l, int r, long val) {
        diff[l] += val;
        diff[r + 1] -= val;
    }
}