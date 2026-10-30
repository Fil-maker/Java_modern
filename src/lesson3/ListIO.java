package lesson3;

import java.io.*;
import java.util.*;
import java.util.stream.*;

public class ListIO {
    // Вывод списка через пробел вручную
    static void printList(List<Integer> list) {
        for (int x : list) {
            System.out.print(x + " ");
        }
        System.out.println();
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        // Формат ввода:
        // n
        // a1 a2 ... an          (могут быть разбиты на несколько строк)

        int n = Integer.parseInt(br.readLine().trim());

        // Ввод: известное количество элементов
        // Токены берём из следующих строк, пока не наберём n чисел
        List<Integer> list = new ArrayList<>(n);
        StringTokenizer st = new StringTokenizer("");
        while (list.size() < n) {
            while (!st.hasMoreTokens()) {
                st = new StringTokenizer(br.readLine());
            }
            list.add(Integer.parseInt(st.nextToken()));
        }

        // Вывод 1: простой цикл
        printList(list);

        // Вывод 2: toString() у списка — формат [1, 2, 3]
        System.out.println(list);

        // Вывод 3: StringBuilder — быстро для больших списков
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(' ');
            sb.append(list.get(i));
        }
        System.out.println(sb);

        // Вывод 4: Stream API + Collectors.joining
        System.out.println(list.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(" ")));

        // Вывод 5: каждый элемент с новой строки
        StringBuilder lines = new StringBuilder();
        for (int x : list) lines.append(x).append('\n');
        System.out.print(lines);

        // Ввод: неизвестное количество элементов — читаем до конца файла
        List<Integer> rest = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            st = new StringTokenizer(line);
            while (st.hasMoreTokens()) {
                rest.add(Integer.parseInt(st.nextToken()));
            }
        }
        System.out.println("read until EOF: " + rest.size() + " numbers");
        System.out.println(rest);
    }
}
