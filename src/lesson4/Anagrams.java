package lesson4;

import java.io.*;
import java.util.*;
import java.util.stream.*;

/*
 * Задача «Анаграммы»
 *
 * До конца ввода даны слова, разделённые пробелами и переводами строк.
 * Анаграммы — слова из одних и тех же букв (регистр не важен): «кот» и «ток».
 * Найдите все группы анаграмм из двух и более различных слов.
 * Внутри группы слова идут по алфавиту, через пробел.
 * Группы выводятся по убыванию размера, при равенстве — по первому слову.
 * Если групп нет — выведите «Анаграмм нет».
 *
 * Ввод:                              Вывод:
 * кот ток окт пила липа              кот окт ток
 * лес сел ель лень Пила              лес сел
 *                                    липа пила
 */
public class Anagrams {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        // Ключ группы — буквы слова в отсортированном порядке: «ток» → «кот»
        // TreeSet внутри группы убирает повторы и сортирует слова
        Map<String, TreeSet<String>> groups = br.lines()
                .flatMap(line -> Arrays.stream(line.trim().split("\\s+")))
                .filter(w -> !w.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.groupingBy(Anagrams::key,
                        Collectors.toCollection(TreeSet::new)));

        List<TreeSet<String>> result = groups.values().stream()
                .filter(g -> g.size() >= 2)
                .sorted(Comparator.<TreeSet<String>>comparingInt(Set::size).reversed()
                        .thenComparing(TreeSet::first))
                .toList();

        if (result.isEmpty()) {
            System.out.println("Анаграмм нет");
            return;
        }
        for (TreeSet<String> g : result) {
            System.out.println(String.join(" ", g));
        }
    }

    static String key(String word) {
        char[] c = word.toCharArray();
        Arrays.sort(c);
        return new String(c);
    }
}
