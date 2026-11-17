package lesson5.contest.task3_minmax_stack;

import java.io.*;
import java.util.*;
import java.util.function.Function;

public class MinMaxStackTask {

    static class MinMaxStack<T extends Comparable<? super T>> {
        // элемент и минимум/максимум стека на момент его добавления
        private static final class Entry<T> {
            final T value, min, max;

            Entry(T value, T min, T max) {
                this.value = value;
                this.min = min;
                this.max = max;
            }
        }

        private final ArrayList<Entry<T>> data = new ArrayList<>();

        void push(T x) {
            if (data.isEmpty()) {
                data.add(new Entry<>(x, x, x));
            } else {
                Entry<T> top = data.get(data.size() - 1);
                T min = x.compareTo(top.min) < 0 ? x : top.min;
                T max = x.compareTo(top.max) > 0 ? x : top.max;
                data.add(new Entry<>(x, min, max));
            }
        }

        T pop() {
            T v = top().value;
            data.remove(data.size() - 1);
            return v;
        }

        T peek() { return top().value; }
        T min() { return top().min; }
        T max() { return top().max; }
        int size() { return data.size(); }

        private Entry<T> top() {
            if (data.isEmpty()) throw new NoSuchElementException("стек пуст");
            return data.get(data.size() - 1);
        }
    }

    // один и тот же код для любого типа T — тип задаёт только функция разбора
    static <T extends Comparable<? super T>> String run(BufferedReader in, int q, Function<String, T> parse)
            throws IOException {
        MinMaxStack<T> st = new MinMaxStack<>();
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < q; i++) {
            String line = in.readLine();
            if (line.startsWith("PUSH ")) {
                st.push(parse.apply(line.substring(5)));
                continue;
            }
            try {
                Object res;
                switch (line) {
                    case "POP": res = st.pop(); break;
                    case "TOP": res = st.peek(); break;
                    case "MIN": res = st.min(); break;
                    case "MAX": res = st.max(); break;
                    default: res = st.size();
                }
                out.append(res);
            } catch (NoSuchElementException e) {
                out.append("EMPTY");
            }
            out.append('\n');
        }
        return out.toString();
    }

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer head = new StringTokenizer(in.readLine());
        String type = head.nextToken();
        int q = Integer.parseInt(head.nextToken());
        String out = type.equals("INT")
                ? run(in, q, Long::parseLong)
                : run(in, q, s -> s);
        System.out.print(out);
    }
}
