package lesson5;

import java.util.*;
import java.util.function.*;

/*
 * Обобщения (generics): обобщённые классы и методы,
 * ограничения типов, стирание типов.
 */
public class GenericsDemo {

    // ===== Без обобщений: Object и приведение типов
    static class ObjectBox {
        private Object value;
        void set(Object value) { this.value = value; }
        Object get() { return value; }
    }

    // ===== Обобщённый класс: T — параметр типа
    static class Box<T> {
        private T value;

        Box(T value) { this.value = value; }

        T get() { return value; }
        void set(T value) { this.value = value; }

        // обобщённый метод экземпляра: получаем коробку другого типа
        <R> Box<R> map(Function<T, R> f) {
            return new Box<>(f.apply(value));
        }

        @Override
        public String toString() { return "Box[" + value + "]"; }
    }

    // ===== Несколько параметров типа; record тоже может быть обобщённым
    record Pair<A, B>(A first, B second) {
        Pair<B, A> swap() { return new Pair<>(second, first); }
    }

    // ===== Обобщённый интерфейс
    interface Repository<ID, E> {
        void save(ID id, E entity);
        Optional<E> findById(ID id);
        List<E> findAll();
    }

    static class MemoryRepository<ID, E> implements Repository<ID, E> {
        private final Map<ID, E> data = new LinkedHashMap<>();

        public void save(ID id, E entity) { data.put(id, entity); }
        public Optional<E> findById(ID id) { return Optional.ofNullable(data.get(id)); }
        public List<E> findAll() { return new ArrayList<>(data.values()); }
    }

    record Book(String title, int year) {}

    // ===== Обобщённые статические методы: <T> перед типом результата
    static <T> T firstOrDefault(List<T> list, T def) {
        return list.isEmpty() ? def : list.get(0);
    }

    static <T> void swap(T[] arr, int i, int j) {
        T tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    // ===== Ограничение сверху: T обязан уметь сравниваться
    static <T extends Comparable<T>> T max(List<T> list) {
        if (list.isEmpty()) throw new NoSuchElementException("пустой список");
        T best = list.get(0);
        for (T x : list) {
            if (x.compareTo(best) > 0) best = x;   // compareTo доступен благодаря extends
        }
        return best;
    }

    // ограничение классом: у T есть методы Number
    static <T extends Number> double sum(List<T> list) {
        double s = 0;
        for (T x : list) s += x.doubleValue();
        return s;
    }

    // несколько ограничений через &
    static <T extends CharSequence & Comparable<T>> T longestThenMax(List<T> list) {
        T best = list.get(0);
        for (T x : list) {
            if (x.length() > best.length()
                    || x.length() == best.length() && x.compareTo(best) > 0) best = x;
        }
        return best;
    }

    public static void main(String[] args) {
        // ===== 1. Зачем: ошибки типов из времени выполнения переходят во время компиляции
        System.out.println("1. Object вместо T:");
        ObjectBox ob = new ObjectBox();
        ob.set("текст");
        try {
            Integer n = (Integer) ob.get();   // компилируется, падает при запуске
            System.out.println(n);
        } catch (ClassCastException e) {
            System.out.println("  ClassCastException: " + e.getMessage());
        }
        Box<String> sb = new Box<>("текст");  // <> — «ромб», тип выводится слева
        String s = sb.get();                   // приведение не нужно
        // Integer n = sb.get();               // ошибка компиляции — раскомментируйте
        System.out.println("  " + sb + ", длина " + s.length());

        // ===== 2. Обобщённый класс и map
        System.out.println("2. Box и Pair:");
        Box<Integer> len = sb.map(String::length);
        Box<String> stars = len.map("*"::repeat);
        System.out.println("  " + len + " " + stars);

        Pair<String, Integer> p = new Pair<>("Москва", 1147);
        Pair<Integer, String> q = p.swap();
        System.out.println("  " + p + " → " + q);

        // ===== 3. Обобщённый интерфейс и реализация
        System.out.println("3. Repository:");
        Repository<String, Book> books = new MemoryRepository<>();
        books.save("isbn-1", new Book("Effective Java", 2018));
        books.save("isbn-2", new Book("Java Concurrency in Practice", 2006));
        System.out.println("  " + books.findById("isbn-1").map(Book::title).orElse("нет"));
        System.out.println("  " + books.findById("isbn-9").map(Book::title).orElse("нет"));
        System.out.println("  всего: " + books.findAll().size());

        // ===== 4. Обобщённые методы: тип выводится из аргументов
        System.out.println("4. Обобщённые методы:");
        System.out.println("  " + firstOrDefault(List.of("a", "b"), "пусто")
                + " " + firstOrDefault(List.<Integer>of(), -1));
        String[] arr = {"раз", "два", "три"};
        swap(arr, 0, 2);
        System.out.println("  " + Arrays.toString(arr));
        System.out.println("  " + GenericsDemo.<Double>firstOrDefault(List.of(), 0.5)); // явный тип

        // ===== 5. Ограниченные типы
        System.out.println("5. extends:");
        System.out.println("  max = " + max(List.of(3, 17, 5)) + ", " + max(List.of("груша", "яблоко", "банан")));
        System.out.println("  sum = " + sum(List.of(1, 2, 3)) + ", " + sum(List.of(1.5, 2.5)));
        System.out.println("  " + longestThenMax(List.of("кот", "лось", "ёж", "слон")));
        // max(List.of(new Object()));  // ошибка компиляции: Object не Comparable

        // ===== 6. Только ссылочные типы: int → Integer (автоупаковка)
        System.out.println("6. Обёртки:");
        List<Integer> nums = new ArrayList<>(List.of(1000, 1000));
        System.out.println("  == : " + (nums.get(0) == nums.get(1))
                + ", equals: " + nums.get(0).equals(nums.get(1)));   // сравнивайте через equals!
        Map<String, Integer> m = new HashMap<>();
        try {
            int missing = m.get("нет");   // распаковка null → NPE
            System.out.println(missing);
        } catch (NullPointerException e) {
            System.out.println("  распаковка null → NullPointerException");
        }

        // ===== 7. Стирание типов: во время выполнения параметров нет
        System.out.println("7. Стирание типов:");
        List<String> ls = new ArrayList<>();
        List<Integer> li = new ArrayList<>();
        System.out.println("  один класс? " + (ls.getClass() == li.getClass()));
        // нельзя: new T(), new T[n], instanceof List<String>, static T field
        Object o = ls;
        if (o instanceof List<?> list) {   // можно только с <?>
            System.out.println("  это какой-то List, size = " + list.size());
        }

        // ===== 8. Сырые типы (raw types) — наследие Java 1.4, не используйте
        System.out.println("8. Raw type:");
        @SuppressWarnings({"rawtypes", "unchecked"})
        List raw = ls;
        raw.add(42);                       // компилятор только предупреждает
        try {
            String first = ls.get(0);      // а падает здесь, далеко от причины
            System.out.println(first);
        } catch (ClassCastException e) {
            System.out.println("  ClassCastException при чтении: «куча загрязнена»");
        }
    }
}
