package lesson4;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/*
 * Stream API: конвейер операций, Collectors и Optional.
 */
public class StreamDemo {
    record Student(String name, String group, int score) {}

    static final List<Student> STUDENTS = List.of(
            new Student("Иванов", "ИВТ-1", 78),
            new Student("Петрова", "ИВТ-2", 95),
            new Student("Сидоров", "ИВТ-1", 64),
            new Student("Алексеева", "ИВТ-2", 88),
            new Student("Кузнецов", "ПМИ-1", 91),
            new Student("Смирнова", "ПМИ-1", 47),
            new Student("Волков", "ИВТ-1", 85));

    public static void main(String[] args) {
        basics();
        terminalOps();
        collectors();
        optionalDemo();
        moreStreams();
        pitfalls();
    }

    // ===== Конвейер: источник → промежуточные операции → терминальная операция
    static void basics() {
        System.out.println("=== Конвейер ===");
        // Цикл
        List<String> res1 = new ArrayList<>();
        for (Student s : STUDENTS) {
            if (s.score() >= 80) res1.add(s.name().toUpperCase());
        }
        Collections.sort(res1);
        System.out.println("цикл:  " + res1);

        // То же стримом
        List<String> res2 = STUDENTS.stream()          // источник
                .filter(s -> s.score() >= 80)            // оставить подходящие
                .map(s -> s.name().toUpperCase())        // Student → String
                .sorted()                                // отсортировать
                .toList();                               // собрать (Java 16+)
        System.out.println("стрим: " + res2);

        // Топ-3 по баллам
        STUDENTS.stream()
                .sorted(Comparator.comparingInt(Student::score).reversed())
                .limit(3)
                .forEach(s -> System.out.println("  " + s.name() + " " + s.score()));

        // Ленивость: без терминальной операции ничего не выполняется
        Stream<Student> lazy = STUDENTS.stream().peek(s -> System.out.println("смотрим " + s.name()));
        System.out.println("стрим создан, но ещё ничего не напечатал");
        lazy.limit(2).forEach(s -> {});          // только сейчас peek сработает — и всего 2 раза
        System.out.println();
    }

    // ===== Терминальные операции
    static void terminalOps() {
        System.out.println("=== Терминальные операции ===");
        long passed = STUDENTS.stream().filter(s -> s.score() >= 50).count();
        boolean anyGenius = STUDENTS.stream().anyMatch(s -> s.score() > 90);
        boolean allPassed = STUDENTS.stream().allMatch(s -> s.score() >= 50);
        System.out.println("сдали: " + passed + ", есть >90: " + anyGenius + ", сдали все: " + allPassed);

        // Примитивные стримы: sum, average, max без упаковки в Integer
        IntSummaryStatistics st = STUDENTS.stream().mapToInt(Student::score).summaryStatistics();
        System.out.println("min=" + st.getMin() + " max=" + st.getMax()
                + " avg=" + String.format("%.1f", st.getAverage()) + " sum=" + st.getSum());

        // reduce — свёртка: произведение чисел 1..10
        long factorial = LongStream.rangeClosed(1, 10).reduce(1, (a, b) -> a * b);
        System.out.println("10! = " + factorial);

        int sumSquares = IntStream.rangeClosed(1, 100).map(x -> x * x).sum();
        System.out.println("сумма квадратов 1..100 = " + sumSquares);
        System.out.println();
    }

    // ===== Collectors: собрать результат в коллекцию, строку, Map
    static void collectors() {
        System.out.println("=== Collectors ===");
        String names = STUDENTS.stream()
                .map(Student::name)
                .collect(Collectors.joining(", ", "[", "]"));
        System.out.println(names);

        // groupingBy: группа → список студентов
        Map<String, List<Student>> byGroup = STUDENTS.stream()
                .collect(Collectors.groupingBy(Student::group));
        System.out.println("групп: " + byGroup.keySet());

        // groupingBy + второй коллектор: группа → количество, группа → средний балл
        Map<String, Long> count = STUDENTS.stream()
                .collect(Collectors.groupingBy(Student::group, TreeMap::new, Collectors.counting()));
        Map<String, Double> avg = STUDENTS.stream()
                .collect(Collectors.groupingBy(Student::group, TreeMap::new,
                        Collectors.averagingInt(Student::score)));
        System.out.println("количество: " + count);
        System.out.println("средний:    " + avg);

        // группа → имена
        Map<String, List<String>> namesByGroup = STUDENTS.stream()
                .collect(Collectors.groupingBy(Student::group, TreeMap::new,
                        Collectors.mapping(Student::name, Collectors.toList())));
        System.out.println("имена:      " + namesByGroup);

        // partitioningBy: ровно две группы — true и false
        Map<Boolean, List<String>> passed = STUDENTS.stream()
                .collect(Collectors.partitioningBy(s -> s.score() >= 50,
                        Collectors.mapping(Student::name, Collectors.toList())));
        System.out.println("сдали: " + passed.get(true) + ", не сдали: " + passed.get(false));

        // toMap: имя → балл (при повторе ключа без merge-функции будет исключение)
        Map<String, Integer> scoreByName = STUDENTS.stream()
                .collect(Collectors.toMap(Student::name, Student::score));
        System.out.println("балл Петровой: " + scoreByName.get("Петрова"));

        // Частоты слов одной строкой
        Map<String, Long> freq = Stream.of("a b a c b a".split(" "))
                .collect(Collectors.groupingBy(Function.identity(), TreeMap::new, Collectors.counting()));
        System.out.println("частоты: " + freq);
        System.out.println();
    }

    // ===== Optional: результат, которого может не быть
    static void optionalDemo() {
        System.out.println("=== Optional ===");
        Optional<Student> best = STUDENTS.stream().max(Comparator.comparingInt(Student::score));
        best.ifPresent(s -> System.out.println("лучший: " + s.name()));

        Optional<Student> fromMath = STUDENTS.stream()
                .filter(s -> s.group().equals("МАТ-1"))
                .findFirst();
        System.out.println("isPresent: " + fromMath.isPresent());
        String name = fromMath.map(Student::name).orElse("нет такого");
        System.out.println("имя: " + name);

        OptionalDouble avgOfEmpty = IntStream.empty().average();
        System.out.println("среднее пустого: " + avgOfEmpty.orElse(Double.NaN));

        // Не делайте так: get() без проверки бросит NoSuchElementException
        try {
            fromMath.get();
        } catch (NoSuchElementException e) {
            System.out.println("get() у пустого Optional: " + e.getMessage());
        }
        System.out.println();
    }

    // ===== Другие источники и flatMap
    static void moreStreams() {
        System.out.println("=== Источники и flatMap ===");
        int[] arr = {5, 3, 8, 1};
        System.out.println("max массива: " + Arrays.stream(arr).max().getAsInt());

        List<String> lines = List.of("мама мыла раму", "рама была мыта", "мама рада");
        // flatMap: каждая строка → поток слов, все потоки склеиваются в один
        List<String> unique = lines.stream()
                .flatMap(l -> Arrays.stream(l.split(" ")))
                .distinct()
                .sorted()
                .toList();
        System.out.println("уникальные слова: " + unique);

        // Поток символов строки
        long vowels = "программирование".chars()
                .filter(c -> "аеёиоуыэюя".indexOf(c) >= 0)
                .count();
        System.out.println("гласных: " + vowels);

        // Бесконечный поток + limit
        List<Integer> powers = Stream.iterate(1, x -> x * 2).limit(11).toList();
        System.out.println("степени двойки: " + powers);
        System.out.println();
    }

    // ===== Подводные камни
    static void pitfalls() {
        System.out.println("=== Ловушки ===");
        Stream<Student> s = STUDENTS.stream();
        s.count();
        try {
            s.count();                          // стрим одноразовый
        } catch (IllegalStateException e) {
            System.out.println("повторное использование: " + e.getMessage());
        }

        // toList() возвращает неизменяемый список
        List<Integer> list = Stream.of(3, 1, 2).toList();
        try {
            list.add(4);
        } catch (UnsupportedOperationException e) {
            System.out.println("toList() — неизменяемый; для изменяемого: collect(Collectors.toCollection(ArrayList::new))");
        }

        // Не меняйте внешние переменные изнутри стрима — используйте коллекторы
        List<String> bad = new ArrayList<>();
        STUDENTS.stream().filter(st -> st.score() > 90).forEach(st -> bad.add(st.name())); // работает, но плохой стиль
        List<String> good = STUDENTS.stream().filter(st -> st.score() > 90).map(Student::name).toList();
        System.out.println(bad + " == " + good);
    }
}
