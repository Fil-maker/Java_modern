package lesson5;

import java.util.*;
import java.util.function.*;

/*
 * Wildcards: ? extends, ? super и правило PECS.
 */
public class WildcardsDemo {

    static abstract class Animal {
        final String name;
        Animal(String name) { this.name = name; }
        abstract String voice();
        @Override public String toString() { return getClass().getSimpleName() + "(" + name + ")"; }
    }

    static class Cat extends Animal {
        Cat(String name) { super(name); }
        String voice() { return "мяу"; }
    }

    static class Dog extends Animal {
        Dog(String name) { super(name); }
        String voice() { return "гав"; }
    }

    // ===== Инвариантность: List<Cat> НЕ является List<Animal>
    static void chorusStrict(List<Animal> animals) {
        for (Animal a : animals) System.out.print(a.voice() + " ");
        System.out.println();
    }

    // ===== ? extends — «производитель»: из списка только ЧИТАЕМ как Animal
    static void chorus(List<? extends Animal> animals) {
        for (Animal a : animals) System.out.print(a.voice() + " ");
        System.out.println();
        // animals.add(new Cat("Мурка"));  // ошибка: вдруг это List<Dog>?
    }

    static double total(Collection<? extends Number> nums) {
        double s = 0;
        for (Number n : nums) s += n.doubleValue();
        return s;
    }

    // ===== ? super — «потребитель»: в список только ПИШЕМ Cat
    static void addKittens(List<? super Cat> shelter, int n) {
        for (int i = 1; i <= n; i++) shelter.add(new Cat("котёнок-" + i));
        Object any = shelter.get(0);   // читать можно только как Object
        System.out.println("  первый: " + any);
    }

    // ===== PECS: Producer Extends, Consumer Super
    static <T> void copy(List<? super T> dst, List<? extends T> src) {
        for (T x : src) dst.add(x);
    }

    // так устроен Collections.max: T сравнивается сам с собой или с предком
    static <T> Optional<T> maxBy(Iterable<? extends T> items, Comparator<? super T> cmp) {
        T best = null;
        for (T x : items) {
            if (best == null || cmp.compare(x, best) > 0) best = x;
        }
        return Optional.ofNullable(best);
    }

    static <T> List<T> filter(Collection<? extends T> src, Predicate<? super T> keep) {
        List<T> out = new ArrayList<>();
        for (T x : src) if (keep.test(x)) out.add(x);
        return out;
    }

    // ===== ? без ограничений: тип не важен, работаем как с Object
    static int countNulls(Collection<?> c) {
        int k = 0;
        for (Object o : c) if (o == null) k++;
        return k;
    }

    public static void main(String[] args) {
        List<Cat> cats = List.of(new Cat("Барсик"), new Cat("Мурка"));
        List<Dog> dogs = List.of(new Dog("Шарик"));
        List<Animal> zoo = new ArrayList<>(List.of(new Dog("Рекс")));

        // ===== 1. Инвариантность
        System.out.println("1. Инвариантность:");
        System.out.print("  zoo: ");
        chorusStrict(zoo);
        // chorusStrict(cats);   // ошибка компиляции: List<Cat> не List<Animal>
        // Почему так: иначе внутри можно было бы сделать animals.add(new Dog(...))
        // и в списке кошек оказалась бы собака.

        // А массивы ковариантны — и ошибка всплывает только при запуске
        Object[] objs = new String[2];
        try {
            objs[0] = 42;
        } catch (ArrayStoreException e) {
            System.out.println("  массивы: ArrayStoreException " + e.getMessage());
        }

        // ===== 2. ? extends
        System.out.println("2. ? extends — читаем:");
        System.out.print("  cats: ");
        chorus(cats);
        System.out.print("  dogs: ");
        chorus(dogs);
        System.out.println("  total = " + total(List.of(1, 2, 3)) + ", " + total(Set.of(0.5, 1.5)));

        // ===== 3. ? super
        System.out.println("3. ? super — пишем:");
        List<Animal> shelter = new ArrayList<>();
        List<Object> box = new ArrayList<>();
        addKittens(shelter, 2);
        addKittens(box, 1);
        // addKittens(dogs, 1);   // ошибка: Dog не предок Cat

        // ===== 4. PECS вместе
        System.out.println("4. PECS:");
        copy(zoo, cats);            // T = Cat: dst — List<Animal>, src — List<Cat>
        copy(zoo, dogs);
        System.out.println("  zoo = " + zoo);

        Comparator<Animal> byName = Comparator.comparing(a -> a.name);
        // компаратор для Animal подходит и для Cat — благодаря ? super T
        System.out.println("  max cat = " + maxBy(cats, byName).orElseThrow());
        System.out.println("  max of empty = " + maxBy(List.<Cat>of(), byName));

        Predicate<Object> notNull = Objects::nonNull;
        Predicate<Animal> loud = a -> a.voice().equals("гав");
        System.out.println("  " + filter(zoo, loud) + " " + filter(cats, notNull));
        System.out.println("  Collections.max: " + Collections.max(cats, byName));

        // ===== 5. Неограниченный ?
        System.out.println("5. <?>:");
        System.out.println("  nulls = " + countNulls(Arrays.asList(1, null, 3, null))
                + ", " + countNulls(List.of("a")));
    }
}
