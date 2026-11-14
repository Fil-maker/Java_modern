package lesson4;

import java.util.*;
import java.util.function.*;

/*
 * Лямбды, функциональные интерфейсы, ссылки на методы и Comparator.
 */
public class LambdaDemo {
    // Функциональный интерфейс — интерфейс ровно с одним абстрактным методом
    @FunctionalInterface
    interface Operation {
        int apply(int a, int b);
    }

    record Student(String name, String group, double avg) {}

    public static void main(String[] args) {
        // ===== От анонимного класса к лямбде
        Comparator<String> byLen1 = new Comparator<>() {
            @Override
            public int compare(String a, String b) {
                return Integer.compare(a.length(), b.length());
            }
        };
        Comparator<String> byLen2 = (a, b) -> Integer.compare(a.length(), b.length());
        Comparator<String> byLen3 = Comparator.comparingInt(String::length);

        List<String> words = new ArrayList<>(List.of("стрим", "лямбда", "мап", "коллекция"));
        words.sort(byLen1);
        System.out.println(words);
        words.sort(byLen2.reversed());
        System.out.println(words);
        words.sort(byLen3.thenComparing(Comparator.naturalOrder()));
        System.out.println(words);

        // ===== Свой функциональный интерфейс
        Operation plus = (a, b) -> a + b;
        Operation pow = (a, b) -> {          // тело из нескольких операторов — в фигурных скобках
            int r = 1;
            for (int i = 0; i < b; i++) r *= a;
            return r;
        };
        System.out.println("plus=" + plus.apply(2, 10) + " pow=" + pow.apply(2, 10));

        // ===== Стандартные интерфейсы из java.util.function
        Function<String, Integer> len = String::length;          // T → R
        Predicate<String> isLong = s -> s.length() > 5;          // T → boolean
        Consumer<String> printer = System.out::println;          // T → void
        Supplier<List<String>> newList = ArrayList::new;         // () → T
        UnaryOperator<String> upper = String::toUpperCase;       // T → T
        BinaryOperator<Integer> max = Math::max;                 // (T, T) → T
        BiFunction<String, Integer, String> rep = String::repeat; // (T, U) → R

        System.out.println(len.apply("лямбда") + " " + isLong.test("лямбда") + " "
                + upper.apply("java") + " " + max.apply(3, 7) + " " + rep.apply("ха", 3));
        printer.accept("Consumer печатает строку");
        List<String> fresh = newList.get();
        fresh.add("новый список от Supplier");
        System.out.println(fresh);

        // Композиция функций
        Function<String, String> trimUpper = ((Function<String, String>) String::trim).andThen(upper);
        Predicate<String> shortWord = isLong.negate();
        Predicate<String> startsK = s -> s.startsWith("к");
        System.out.println("[" + trimUpper.apply("   java  ") + "] "
                + shortWord.test("мап") + " " + shortWord.and(startsK).test("кот"));

        // ===== Четыре вида ссылок на методы
        Function<String, Integer> parse = Integer::parseInt;     // статический метод
        String prefix = "Java ";
        Function<String, Boolean> hasPrefix = prefix::equals;    // метод конкретного объекта
        Function<String, String> lower = String::toLowerCase;    // метод произвольного объекта типа
        Supplier<StringBuilder> sb = StringBuilder::new;         // конструктор
        System.out.println(parse.apply("42") + 1 + " " + hasPrefix.apply("Java ")
                + " " + lower.apply("ПРИВЕТ") + " " + sb.get().append("ok"));

        // ===== Захват переменных: только effectively final
        int bonus = 10;
        // bonus++;   // раскомментируйте — лямбда ниже перестанет компилироваться
        Function<Integer, Integer> addBonus = x -> x + bonus;
        System.out.println("addBonus(5) = " + addBonus.apply(5));

        // ===== Comparator для объектов
        List<Student> students = new ArrayList<>(List.of(
                new Student("Иванов", "ИВТ-1", 4.5),
                new Student("Петрова", "ИВТ-2", 4.9),
                new Student("Сидоров", "ИВТ-1", 4.5),
                new Student("Алексеева", "ИВТ-2", 3.8)));

        // по группе, внутри группы — по убыванию среднего, при равенстве — по имени
        students.sort(Comparator.comparing(Student::group)
                .thenComparing(Student::avg, Comparator.reverseOrder())
                .thenComparing(Student::name));
        students.forEach(System.out::println);

        // ===== Методы коллекций, принимающие лямбды
        List<Integer> nums = new ArrayList<>(List.of(5, 12, 7, 20, 3));
        nums.removeIf(x -> x < 6);
        nums.replaceAll(x -> x * 10);
        System.out.println("nums = " + nums);

        Map<String, Integer> ages = new TreeMap<>(Map.of("Аня", 19, "Борис", 20));
        ages.forEach((name, age) -> System.out.println(name + " — " + age));
        ages.replaceAll((name, age) -> age + 1);
        System.out.println("через год: " + ages);
    }
}
