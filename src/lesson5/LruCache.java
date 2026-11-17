package lesson5;

import java.util.*;
import java.util.function.*;

/*
 * Задача «LRU-кеш»
 *
 * Реализуйте обобщённый кеш LruCache<K, V> ограниченной ёмкости.
 * Когда места нет, вытесняется элемент, к которому дольше всего не обращались
 * (Least Recently Used). Методы:
 *   Optional<V> get(K key)            — обращение делает элемент «свежим»;
 *   void put(K key, V value);
 *   V getOrCompute(K key, Function<? super K, ? extends V> loader)
 *                                     — если ключа нет, вычислить, положить и вернуть;
 *   int hits(), int misses()          — статистика попаданий для getOrCompute.
 * Ёмкость <= 0 — IllegalArgumentException в конструкторе.
 * Если loader бросил исключение, кеш не меняется, исключение летит дальше.
 * Подсказка: LinkedHashMap(capacity, 0.75f, true) хранит порядок обращений,
 * а метод removeEldestEntry позволяет удалять старейший элемент.
 *
 * Пример вывода:
 *   {a=1, b=2, c=3}
 *   get(a) = Optional[1]
 *   после put(d): {c=3, a=1, d=4}
 *   get(b) = Optional.empty
 *   медленное вычисление 40... 2 раза
 *   fib(40) = 102334155, fib(40) = 102334155, fib(35) = 9227465
 *   hits = 1, misses = 2
 *   loader упал: NumberFormatException; ключ x в кеше? false
 *   ёмкость 0: IllegalArgumentException: ёмкость должна быть > 0, а не 0
 */
public class LruCache<K, V> {
    private final LinkedHashMap<K, V> map;
    private int hits, misses;

    public LruCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("ёмкость должна быть > 0, а не " + capacity);
        }
        // accessOrder = true: get перемещает элемент в конец
        this.map = new LinkedHashMap<>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > capacity;
            }
        };
    }

    public Optional<V> get(K key) {
        return Optional.ofNullable(map.get(key));
    }

    public void put(K key, V value) {
        map.put(Objects.requireNonNull(key), Objects.requireNonNull(value));
    }

    public V getOrCompute(K key, Function<? super K, ? extends V> loader) {
        V v = map.get(key);
        if (v != null) {
            hits++;
            return v;
        }
        misses++;
        V computed = loader.apply(key);   // если бросит — до put не дойдём
        put(key, computed);
        return computed;
    }

    public int hits() { return hits; }
    public int misses() { return misses; }

    @Override
    public String toString() { return map.toString(); }

    static long slowFib(int n) {
        return n < 2 ? n : slowFib(n - 1) + slowFib(n - 2);
    }

    public static void main(String[] args) {
        LruCache<String, Integer> c = new LruCache<>(3);
        c.put("a", 1);
        c.put("b", 2);
        c.put("c", 3);
        System.out.println(c);
        System.out.println("get(a) = " + c.get("a"));   // a стал самым свежим
        c.put("d", 4);                                   // вытеснен b
        System.out.println("после put(d): " + c);
        System.out.println("get(b) = " + c.get("b"));

        // Function<Object, Long> подходит как Function<? super Integer, ? extends Number>
        LruCache<Integer, Number> fib = new LruCache<>(10);
        int[] slowCalls = {0};
        Function<Object, Long> loader = n -> {
            slowCalls[0]++;
            return slowFib((Integer) n);
        };
        System.out.print("медленное вычисление 40... ");
        Number f1 = fib.getOrCompute(40, loader);
        Number f2 = fib.getOrCompute(40, loader);
        Number f3 = fib.getOrCompute(35, loader);
        System.out.println(slowCalls[0] + " раза");
        System.out.println("fib(40) = " + f1 + ", fib(40) = " + f2 + ", fib(35) = " + f3);
        System.out.println("hits = " + fib.hits() + ", misses = " + fib.misses());

        LruCache<String, Integer> parsed = new LruCache<>(5);
        try {
            parsed.getOrCompute("x", Integer::parseInt);
        } catch (NumberFormatException e) {
            System.out.println("loader упал: " + e.getClass().getSimpleName()
                    + "; ключ x в кеше? " + parsed.get("x").isPresent());
        }

        try {
            new LruCache<String, String>(0);
        } catch (IllegalArgumentException e) {
            System.out.println("ёмкость 0: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
