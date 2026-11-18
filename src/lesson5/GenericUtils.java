package lesson5;

import java.util.*;
import java.util.function.*;

/*
 * Задача «Обобщённые утилиты»
 *
 * Напишите статические обобщённые методы с максимально гибкими сигнатурами
 * (правило PECS: откуда читаем — ? extends, куда пишем или что потребляет T — ? super):
 *
 *   1) Optional<Pair<T, T>> minMax(Iterable<? extends T> items, Comparator<? super T> cmp)
 *      — минимум и максимум за один проход; для пустой коллекции Optional.empty();
 *   2) Pair<List<T>, List<T>> partition(Iterable<? extends T> items, Predicate<? super T> p)
 *      — разбить на подходящие и неподходящие;
 *   3) <K, T> Map<K, List<T>> groupBy(Iterable<? extends T> items, Function<? super T, ? extends K> key)
 *      — сгруппировать с сохранением порядка ключей (без Stream API);
 *   4) List<Pair<A, B>> zip(List<? extends A> a, List<? extends B> b)
 *      — склеить попарно; если длины разные — IllegalArgumentException;
 *   5) void fill(List<? super T> dst, Supplier<? extends T> gen, int n)
 *      — добавить n значений от генератора.
 * В main покажите, что методы принимают «родственные» типы: List<Integer>
 * с Comparator<Number>, List<Object> как приёмник для Integer и т. п.
 *
 * Пример вывода:
 *   minMax(nums) = Optional[Pair[first=-4, second=15]]
 *   minMax(пусто) = Optional.empty
 *   по длине: Pair[first=ёж, second=черепаха]
 *   чётные/нечётные: Pair[first=[8, -4, 2], second=[3, 15, 7]]
 *   по первой букве: {к=[кот, кит], ё=[ёж], ч=[черепаха], с=[сом]}
 *   zip: [Pair[first=Аня, second=5], Pair[first=Борис, second=4]]
 *   zip разной длины: длины 2 и 3
 *   fill: [начало, 1, 2, 4, 8]
 */
public class GenericUtils {

    record Pair<A, B>(A first, B second) {}

    static <T> Optional<Pair<T, T>> minMax(Iterable<? extends T> items, Comparator<? super T> cmp) {
        Iterator<? extends T> it = items.iterator();
        if (!it.hasNext()) return Optional.empty();
        T min = it.next(), max = min;
        while (it.hasNext()) {
            T x = it.next();
            if (cmp.compare(x, min) < 0) min = x;
            if (cmp.compare(x, max) > 0) max = x;
        }
        return Optional.of(new Pair<>(min, max));
    }

    static <T> Pair<List<T>, List<T>> partition(Iterable<? extends T> items, Predicate<? super T> p) {
        List<T> yes = new ArrayList<>(), no = new ArrayList<>();
        for (T x : items) (p.test(x) ? yes : no).add(x);
        return new Pair<>(yes, no);
    }

    static <K, T> Map<K, List<T>> groupBy(Iterable<? extends T> items, Function<? super T, ? extends K> key) {
        Map<K, List<T>> out = new LinkedHashMap<>();
        for (T x : items) {
            out.computeIfAbsent(key.apply(x), k -> new ArrayList<>()).add(x);
        }
        return out;
    }

    static <A, B> List<Pair<A, B>> zip(List<? extends A> a, List<? extends B> b) {
        if (a.size() != b.size()) {
            throw new IllegalArgumentException("длины " + a.size() + " и " + b.size());
        }
        List<Pair<A, B>> out = new ArrayList<>(a.size());
        for (int i = 0; i < a.size(); i++) out.add(new Pair<>(a.get(i), b.get(i)));
        return out;
    }

    static <T> void fill(List<? super T> dst, Supplier<? extends T> gen, int n) {
        for (int i = 0; i < n; i++) dst.add(gen.get());
    }

    public static void main(String[] args) {
        List<Integer> nums = List.of(3, 8, -4, 15, 2, 7);
        // Comparator<Number> подходит для Integer благодаря ? super T
        Comparator<Number> byDouble = Comparator.comparingDouble(Number::doubleValue);
        System.out.println("minMax(nums) = " + minMax(nums, byDouble));
        System.out.println("minMax(пусто) = " + minMax(List.<Integer>of(), byDouble));

        List<String> animals = List.of("кот", "ёж", "черепаха", "кит", "сом");
        Comparator<CharSequence> byLen = Comparator.comparingInt(CharSequence::length);
        System.out.println("по длине: " + minMax(animals, byLen).orElseThrow());

        Predicate<Number> even = n -> n.longValue() % 2 == 0;
        System.out.println("чётные/нечётные: " + partition(nums, even));

        Function<CharSequence, Character> firstChar = s -> s.charAt(0);
        Map<Character, List<String>> byFirst = groupBy(animals, firstChar);
        System.out.println("по первой букве: " + byFirst);

        List<Pair<String, Integer>> marks = zip(List.of("Аня", "Борис"), List.of(5, 4));
        System.out.println("zip: " + marks);
        try {
            zip(List.of(1, 2), List.of("a", "b", "c"));
        } catch (IllegalArgumentException e) {
            System.out.println("zip разной длины: " + e.getMessage());
        }

        List<Object> sink = new ArrayList<>(List.of("начало"));
        int[] pow = {1};
        Supplier<Integer> powers = () -> { int r = pow[0]; pow[0] *= 2; return r; };
        fill(sink, powers, 4);    // T = Integer, приёмник — List<Object>
        System.out.println("fill: " + sink);
    }
}
