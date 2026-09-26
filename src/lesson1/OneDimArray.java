package lesson1;

import java.util.*;

public class OneDimArray {
    static void printArr(int[] a, int n){
        for (int i=0; i < n; i++){
            System.out.print(a[i] + " ");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        Scanner sc;
        sc = new Scanner(System.in);

        // Создание массива размера n
        int n = sc.nextInt();
        int[] a = new int[n];

        // Ввод элементов
        for (int i = 0; i < n; i++) {
            a[i] = sc.nextInt();
        }

        printArr(a, n);

        // Сумма элементов — O(n)
        long sum = 0;
        for (int x : a) sum += x;

        // Поиск максимума/минимума — O(n)
        int max = a[0], min = a[0];
        for (int i = 1; i < n; i++) {
            if (a[i] > max) max = a[i];
            if (a[i] < min) min = a[i];
        }

        // Реверс массива — O(n)
        for (int i = 0, j = n - 1; i < j; i++, j--) {
            int tmp = a[i];
            a[i] = a[j];
            a[j] = tmp;
        }
        printArr(a, n);

        System.out.println("sum=" + sum + " max=" + max + " min=" + min);
    }
}