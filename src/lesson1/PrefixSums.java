package lesson1;

public class PrefixSums {
    public static void main(String[] args) {
        int[] a = {3, 1, 4, 1, 5, 9, 2, 6};
        int n = a.length;

        // pref[i] — сумма первых i элементов (pref[0] = 0)
        long[] pref = new long[n + 1];
        for (int i = 0; i < n; i++) {
            pref[i + 1] = pref[i] + a[i];
        }

        // Сумма на [l, r] включительно (0-индексация): pref[r+1] - pref[l]
        int l = 2, r = 5;
        long sum = pref[r + 1] - pref[l];
        System.out.println("Sum[2..5] = " + sum); // 4+1+5+9 = 19

        // Частный случай: количество элементов, равных 0, на отрезке
        // Строим массив 0/1 и префиксные суммы по нему
        int[] b = {0, 1, 0, 0, 1, 1, 0, 1};
        int[] cnt = new int[b.length + 1];
        for (int i = 0; i < b.length; i++) cnt[i + 1] = cnt[i] + b[i];
        System.out.println("Zeros on [1..5] = " + (
                // Предположим все элементы 0
                (5 - 1) + 1
                // вычтем количество единиц (через префиксную сумму)
                - (cnt[6] - cnt[1])));
    }
}