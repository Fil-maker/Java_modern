package lesson1;

import java.util.Scanner;

public class TwoDimArray {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt(); // строки
        int m = sc.nextInt(); // столбцы
        int[][] a = new int[n][m];

        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++)
                a[i][j] = sc.nextInt();

        // Сумма строк
        for (int i = 0; i < n; i++) {
            int rowSum = 0;
            for (int j = 0; j < m; j++) rowSum += a[i][j];
            System.out.println("Row " + i + ": " + rowSum);
        }

        // Сумма столбцов
        for (int j = 0; j < m; j++) {
            int colSum = 0;
            for (int i = 0; i < n; i++) colSum += a[i][j];
            System.out.println("Col " + j + ": " + colSum);
        }

        // Обход по диагонали (главной)
        for (int i = 0; i < Math.min(n, m); i++) {
            System.out.print(a[i][i] + " ");
        }
        System.out.println();

        // Обход «змейкой» (популярно в задачах)
        for (int i = 0; i < n; i++) {
            if (i % 2 == 0) {
                for (int j = 0; j < m; j++) System.out.print(a[i][j] + " ");
            } else {
                for (int j = m - 1; j >= 0; j--) System.out.print(a[i][j] + " ");
            }
        }
    }
}