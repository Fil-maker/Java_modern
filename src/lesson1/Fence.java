package lesson1;

import java.util.*;
import java.io.*;

public class Fence {
    public static void main(String[] args) throws IOException {
        SpeedTest.FastScanner fs = new SpeedTest.FastScanner(System.in);
        int n = fs.nextIntOrMinus();        // досок
        int m = fs.nextIntOrMinus();        // маляров

        // diff[i] = a[i] - a[i-1], размер n+1 чтобы безопасно делать r+1
        long[] diff = new long[n + 1];

        for (int i = 0; i < m; i++) {
            int l = fs.nextIntOrMinus();
            int r = fs.nextIntOrMinus();
            diff[l]     += 1;   // начали красить
            diff[r + 1] -= 1;   // перестали
        }

        // Восстанавливаем покрытие каждой доски
        long[] cover = new long[n];
        long cur = 0;
        long maxCover = 0;
        int maxPos = 0;
        for (int i = 0; i < n; i++) {
            cur += diff[i];
            cover[i] = cur;
            if (cur > maxCover) { maxCover = cur; maxPos = i; }
        }

        // (A) Покрытие каждой доски
        System.out.println("Cover: " + Arrays.toString(cover));

        // (B) Сколько досок покрыто ровно k раз
        int k = fs.nextIntOrMinus();
        int cntK = 0;
        for (long c : cover) if (c == k) cntK++;
        System.out.println("Boards painted exactly " + k + " times: " + cntK);

        // (C) Максимум
        System.out.println("Max = " + maxCover + " at pos " + maxPos);
    }
}