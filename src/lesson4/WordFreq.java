package lesson4;

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/*
 * Задача «Частотный словарь»
 *
 * В первой строке дано число k, далее до конца ввода — произвольный текст.
 * Словом считается непрерывная последовательность букв, регистр не важен.
 * Выведите общее число слов, число различных слов и k самых частых слов
 * в формате «слово — количество». При равной частоте слова идут по алфавиту.
 *
 * Ввод:                                  Вывод:
 * 3                                      Всего слов: 13
 * Мама мыла раму. Рама была мыта,        Различных: 9
 * мама рада! Мама, мама...               мама — 4
 * А рама — нет.                          рама — 2
 *                                        а — 1
 */
public class WordFreq {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int k = Integer.parseInt(br.readLine().trim());

        // lines() у BufferedReader — поток строк до конца ввода
        // разделитель — всё, что не буква (\p{L} — любая буква Unicode)
        List<String> words = br.lines()
                .flatMap(line -> Arrays.stream(line.toLowerCase().split("[^\\p{L}]+")))
                .filter(w -> !w.isEmpty())
                .toList();

        Map<String, Long> freq = words.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        System.out.println("Всего слов: " + words.size());
        System.out.println("Различных: " + freq.size());

        // сортируем пары (слово, частота): по убыванию частоты, затем по алфавиту
        Comparator<Map.Entry<String, Long>> order =
                Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey());

        freq.entrySet().stream()
                .sorted(order)
                .limit(k)
                .forEach(e -> System.out.println(e.getKey() + " — " + e.getValue()));
    }
}
