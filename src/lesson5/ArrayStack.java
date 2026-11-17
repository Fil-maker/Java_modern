package lesson5;

import java.util.*;

/*
 * Задача «Свой стек»
 *
 * Реализуйте обобщённый стек ArrayStack<T> на массиве, без коллекций Java:
 *   push(T x), T pop(), T peek(), int size(), boolean isEmpty().
 *   - массив увеличивается вдвое, когда заполнен;
 *   - pop и peek на пустом стеке бросают NoSuchElementException;
 *   - стек реализует Iterable<T>: for-each обходит его от вершины ко дну;
 *   - toString() возвращает элементы от дна к вершине: [a, b, c].
 * Подсказка: new T[n] написать нельзя (стирание типов) —
 * создайте Object[] и приводите элемент к T при чтении.
 *
 * С помощью стека проверьте расстановку скобок ()[]{} в строках.
 * Для ошибки выведите позицию (с 1) и что ожидалось.
 *
 * Пример вывода:
 *   [1, 2, 3] сверху 3, размер 3
 *   for-each: 3 2 1
 *   pop на пустом: NoSuchElementException: стек пуст
 *   ({[]})            OK
 *   list.get(map[k]   позиция 16: не закрыта «(» из позиции 9
 *   a[i] = b(c]       позиция 11: ожидалась «)», а встретилась «]»
 *   {x}}              позиция 4: лишняя «}»
 *   if (a) { b[0]; }  OK
 */
public class ArrayStack<T> implements Iterable<T> {
    private Object[] data = new Object[2];
    private int size;

    public void push(T x) {
        if (size == data.length) {
            data = Arrays.copyOf(data, data.length * 2);
        }
        data[size++] = x;
    }

    public T pop() {
        T top = peek();
        data[--size] = null;    // убираем ссылку, чтобы объект мог собрать GC
        return top;
    }

    @SuppressWarnings("unchecked")   // безопасно: кладём в массив только T
    public T peek() {
        if (size == 0) throw new NoSuchElementException("стек пуст");
        return (T) data[size - 1];
    }

    public int size() { return size; }

    public boolean isEmpty() { return size == 0; }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private int i = size - 1;

            @Override
            public boolean hasNext() { return i >= 0; }

            @Override
            @SuppressWarnings("unchecked")
            public T next() {
                if (i < 0) throw new NoSuchElementException();
                return (T) data[i--];
            }
        };
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(", ", "[", "]");
        for (int i = 0; i < size; i++) sj.add(String.valueOf(data[i]));
        return sj.toString();
    }

    // ===== Проверка скобок
    record Open(char ch, int pos) {}

    static String checkBrackets(String s) {
        String opening = "([{", closing = ")]}";
        ArrayStack<Open> stack = new ArrayStack<>();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (opening.indexOf(c) >= 0) {
                stack.push(new Open(c, i + 1));
            } else if (closing.indexOf(c) >= 0) {
                if (stack.isEmpty()) {
                    return "позиция " + (i + 1) + ": лишняя «" + c + "»";
                }
                Open top = stack.pop();
                char expected = closing.charAt(opening.indexOf(top.ch()));
                if (c != expected) {
                    return "позиция " + (i + 1) + ": ожидалась «" + expected + "», а встретилась «" + c + "»";
                }
            }
        }
        if (!stack.isEmpty()) {
            Open top = stack.peek();
            return "позиция " + (s.length() + 1) + ": не закрыта «" + top.ch() + "» из позиции " + top.pos();
        }
        return "OK";
    }

    public static void main(String[] args) {
        ArrayStack<Integer> st = new ArrayStack<>();
        st.push(1);
        st.push(2);
        st.push(3);     // здесь массив расширится с 2 до 4
        System.out.println(st + " сверху " + st.peek() + ", размер " + st.size());
        System.out.print("for-each:");
        for (int x : st) System.out.print(" " + x);
        System.out.println();

        ArrayStack<String> empty = new ArrayStack<>();
        try {
            empty.pop();
        } catch (NoSuchElementException e) {
            System.out.println("pop на пустом: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        for (String s : List.of("({[]})", "list.get(map[k]", "a[i] = b(c]", "{x}}", "if (a) { b[0]; }")) {
            System.out.printf("%-17s %s%n", s, checkBrackets(s));
        }
    }
}
