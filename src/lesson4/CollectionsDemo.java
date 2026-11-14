package lesson4;

import java.util.*;

/*
 * Демонстрация основных коллекций: List, Set, Map и полезные методы.
 */
public class CollectionsDemo {
    public static void main(String[] args) {
        listDemo();
        setDemo();
        mapDemo();
        navigableDemo();
        pitfallsDemo();
    }

    // ===== List: порядок вставки, доступ по индексу, дубликаты разрешены
    static void listDemo() {
        System.out.println("=== List ===");
        List<String> pets = new ArrayList<>(List.of("кот", "пёс", "кот", "ёж"));
        pets.add("сова");
        pets.add(1, "хомяк");                  // вставка по индексу — сдвигает хвост
        pets.remove("кот");                    // удаляет ПЕРВОЕ вхождение
        System.out.println(pets + " size=" + pets.size() + " get(0)=" + pets.get(0));
        System.out.println("indexOf(\"кот\") = " + pets.indexOf("кот"));
        System.out.println("contains(\"ёж\") = " + pets.contains("ёж"));   // O(n) — перебор

        // Ловушка: remove(int) и remove(Object) у List<Integer>
        List<Integer> nums = new ArrayList<>(List.of(10, 20, 30));
        nums.remove(1);                        // по ИНДЕКСУ → [10, 30]
        nums.remove(Integer.valueOf(30));      // по ЗНАЧЕНИЮ → [10]
        System.out.println("nums = " + nums);

        // List.of — неизменяемый список
        List<Integer> fixed = List.of(3, 1, 2);
        try {
            fixed.add(4);
        } catch (UnsupportedOperationException e) {
            System.out.println("List.of(...) менять нельзя: " + e.getClass().getSimpleName());
        }

        // Сортировка: копия + sort
        List<Integer> sorted = new ArrayList<>(fixed);
        Collections.sort(sorted);              // или sorted.sort(null)
        System.out.println("sorted = " + sorted + ", max = " + Collections.max(fixed));
        System.out.println();
    }

    // ===== Set: только уникальные элементы
    static void setDemo() {
        System.out.println("=== Set ===");
        List<String> fruits = List.of("груша", "яблоко", "банан", "яблоко", "киви", "груша");

        Set<String> hash = new HashSet<>(fruits);         // порядок не гарантирован
        Set<String> linked = new LinkedHashSet<>(fruits); // порядок первого добавления
        Set<String> tree = new TreeSet<>(fruits);         // отсортированы

        System.out.println("HashSet       " + hash);
        System.out.println("LinkedHashSet " + linked);
        System.out.println("TreeSet       " + tree);

        // add возвращает false, если элемент уже был — удобно искать повторы
        Set<String> seen = new HashSet<>();
        for (String f : fruits) {
            if (!seen.add(f)) System.out.println("повтор: " + f);
        }

        // Операции над множествами (изменяют левое множество — поэтому копии)
        Set<Integer> a = new TreeSet<>(List.of(1, 2, 3, 4));
        Set<Integer> b = new TreeSet<>(List.of(3, 4, 5));
        Set<Integer> union = new TreeSet<>(a);
        union.addAll(b);
        Set<Integer> inter = new TreeSet<>(a);
        inter.retainAll(b);
        Set<Integer> diff = new TreeSet<>(a);
        diff.removeAll(b);
        System.out.println("a∪b=" + union + "  a∩b=" + inter + "  a\\b=" + diff);
        System.out.println();
    }

    // ===== Map: ключ → значение
    static void mapDemo() {
        System.out.println("=== Map ===");
        Map<String, Integer> stock = new HashMap<>();
        stock.put("яблоко", 5);
        stock.put("груша", 3);
        stock.put("яблоко", 7);                // put по существующему ключу ЗАМЕНЯЕТ значение

        System.out.println("get(\"яблоко\") = " + stock.get("яблоко"));
        System.out.println("get(\"киви\")   = " + stock.get("киви"));            // null!
        System.out.println("getOrDefault   = " + stock.getOrDefault("киви", 0));

        // merge: если ключа нет — положить 1, иначе сложить старое и новое
        stock.merge("киви", 1, Integer::sum);
        stock.merge("киви", 1, Integer::sum);
        System.out.println("после merge: " + stock);

        // Подсчёт частот символов — классический приём
        Map<Character, Integer> freq = new TreeMap<>();
        for (char c : "абракадабра".toCharArray()) {
            freq.merge(c, 1, Integer::sum);
        }
        System.out.println("частоты: " + freq);

        // computeIfAbsent: Map<ключ, список> — группировка
        Map<Integer, List<String>> byLength = new TreeMap<>();
        for (String w : List.of("кот", "слон", "ёж", "пёс", "жираф", "лось")) {
            byLength.computeIfAbsent(w.length(), k -> new ArrayList<>()).add(w);
        }
        System.out.println("по длине: " + byLength);

        // Обход: entrySet даёт и ключ, и значение за один проход
        for (Map.Entry<Integer, List<String>> e : byLength.entrySet()) {
            System.out.println("  " + e.getKey() + " букв → " + e.getValue().size() + " слов(а)");
        }
        System.out.println();
    }

    // ===== TreeMap / TreeSet: навигация по отсортированным ключам
    static void navigableDemo() {
        System.out.println("=== TreeMap: навигация ===");
        // Шкала оценок: от какого балла какая оценка
        TreeMap<Integer, String> scale = new TreeMap<>(Map.of(
                0, "неуд", 50, "удовл", 70, "хорошо", 85, "отлично"));
        for (int points : new int[]{42, 70, 84, 100}) {
            // floorEntry — наибольший ключ <= points
            System.out.println(points + " баллов → " + scale.floorEntry(points).getValue());
        }
        System.out.println("firstKey=" + scale.firstKey() + " lastKey=" + scale.lastKey()
                + " headMap(70)=" + scale.headMap(70));

        TreeSet<Integer> buses = new TreeSet<>(List.of(705, 730, 815, 900));
        System.out.println("ближайший рейс не раньше 720: " + buses.ceiling(720));
        System.out.println("последний рейс раньше 720:    " + buses.lower(720));
        System.out.println("после 900: " + buses.higher(900));     // null — рейсов нет
        System.out.println();
    }

    // ===== Типичные ошибки
    static void pitfallsDemo() {
        System.out.println("=== Ловушки ===");
        List<Integer> list = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        try {
            for (Integer x : list) {
                if (x % 2 == 0) list.remove(x);    // нельзя менять список во время for-each
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("ConcurrentModificationException при удалении в for-each");
        }
        list = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        list.removeIf(x -> x % 2 == 0);            // правильно
        System.out.println("removeIf: " + list);

        // Сравнение Integer через == работает только для -128..127 (кэш)
        Integer a = 1000, b = 1000;
        System.out.println("1000 == 1000 ? " + (a == b) + ", equals ? " + a.equals(b));

        // Ключом HashMap должен быть объект с корректными equals/hashCode.
        // record генерирует их автоматически.
        record Point(int x, int y) {}
        Set<Point> points = new HashSet<>();
        points.add(new Point(1, 2));
        System.out.println("contains(new Point(1, 2)) = " + points.contains(new Point(1, 2)));
    }
}
