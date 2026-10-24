package lesson2;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Композиция вместо наследования + контракт equals/hashCode.
 * "has-a" (имеет) часто гибче, чем "is-a" (является).
 */
public class CompositionDemo {

    // ---- Плохо: наследование ради повторного использования кода ----
    // Наследник зависит от деталей реализации HashSet: addAll внутри вызывает add,
    // и счётчик считает элементы дважды.
    static class CountingSetBad<E> extends HashSet<E> {
        int addCount = 0;

        @Override
        public boolean add(E e) { addCount++; return super.add(e); }

        @Override
        public boolean addAll(java.util.Collection<? extends E> c) {
            addCount += c.size();
            return super.addAll(c);
        }
    }

    // ---- Хорошо: композиция — храним Set внутри и делегируем ему ----
    static class CountingSet<E> {
        private final Set<E> inner;   // "имеет" множество
        private int addCount = 0;

        CountingSet(Set<E> inner) { this.inner = inner; }

        boolean add(E e) { addCount++; return inner.add(e); }

        void addAll(java.util.Collection<? extends E> c) { for (E e : c) add(e); }

        int size() { return inner.size(); }

        int getAddCount() { return addCount; }
    }

    // ---- equals / hashCode ----
    static class Point {
        private final int x, y;

        Point(int x, int y) { this.x = x; this.y = y; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Point p)) return false;   // pattern matching для instanceof (Java 16+)
            return x == p.x && y == p.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);   // равные объекты обязаны иметь равный hashCode
        }

        @Override
        public String toString() { return "(" + x + ", " + y + ")"; }
    }

    public static void main(String[] args) {
        CountingSetBad<String> bad = new CountingSetBad<>();
        bad.addAll(Set.of("a", "b", "c"));
        System.out.println("Наследование: addCount = " + bad.addCount + " (ожидали 3)");

        CountingSet<String> good = new CountingSet<>(new HashSet<>());
        good.addAll(Set.of("a", "b", "c"));
        System.out.println("Композиция:   addCount = " + good.getAddCount());

        Point a = new Point(1, 2), b = new Point(1, 2);
        System.out.println();
        System.out.println("a == b      : " + (a == b));        // false — разные объекты
        System.out.println("a.equals(b) : " + a.equals(b));     // true  — равны по значению
        Set<Point> points = new HashSet<>();
        points.add(a);
        points.add(b);
        System.out.println("HashSet размер: " + points.size()); // 1 — благодаря hashCode
    }
}
