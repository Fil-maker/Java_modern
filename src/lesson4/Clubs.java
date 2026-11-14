package lesson4;

import java.io.*;
import java.util.*;
import java.util.stream.*;

/*
 * Задача «Кружки»
 *
 * Даны три строки, в каждой — фамилии через запятую:
 *   1) все ученики класса;
 *   2) кто ходит в шахматный кружок;
 *   3) кто ходит в кружок программирования.
 * Лишние пробелы вокруг фамилий игнорируются, повторы — тоже.
 * Выведите (фамилии по алфавиту через запятую, пустой список — «—»):
 *   - кто ходит в оба кружка;
 *   - кто ходит хотя бы в один;
 *   - кто ходит только в шахматы;
 *   - кто ходит только в программирование;
 *   - кто не ходит никуда;
 *   - кто записан в кружок, но не числится в классе (опечатки в списках).
 *
 * Ввод:
 * Иванов, Петров, Сидоров, Козлов, Орлова, Белова
 * Петров, Орлова, Иванов
 * Орлова, Козлов ,Петров, Петров, Смирнов
 *
 * Вывод:
 * В оба кружка: Орлова, Петров
 * Хотя бы в один: Иванов, Козлов, Орлова, Петров, Смирнов
 * Только шахматы: Иванов
 * Только программирование: Козлов, Смирнов
 * Никуда: Белова, Сидоров
 * Не из класса: Смирнов
 */
public class Clubs {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        Set<String> all = readSet(br.readLine());
        Set<String> chess = readSet(br.readLine());
        Set<String> code = readSet(br.readLine());

        // retainAll, addAll и removeAll МЕНЯЮТ множество — поэтому работаем с копиями
        Set<String> both = new TreeSet<>(chess);
        both.retainAll(code);                    // пересечение

        Set<String> any = new TreeSet<>(chess);
        any.addAll(code);                        // объединение

        Set<String> onlyChess = new TreeSet<>(chess);
        onlyChess.removeAll(code);               // разность

        Set<String> onlyCode = new TreeSet<>(code);
        onlyCode.removeAll(chess);

        Set<String> nowhere = new TreeSet<>(all);
        nowhere.removeAll(any);

        Set<String> strangers = new TreeSet<>(any);
        strangers.removeAll(all);

        print("В оба кружка", both);
        print("Хотя бы в один", any);
        print("Только шахматы", onlyChess);
        print("Только программирование", onlyCode);
        print("Никуда", nowhere);
        print("Не из класса", strangers);
    }

    // TreeSet — уникальные и сразу по алфавиту
    static Set<String> readSet(String line) {
        return Arrays.stream(line.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(TreeSet::new));
    }

    static void print(String title, Set<String> set) {
        System.out.println(title + ": " + (set.isEmpty() ? "—" : String.join(", ", set)));
    }
}
