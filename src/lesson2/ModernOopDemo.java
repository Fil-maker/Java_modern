package lesson2;

import java.util.List;

/**
 * Современная Java (17–21+): record, sealed-иерархии, pattern matching в switch.
 * Алгебраические типы данных поверх классического ООП.
 */
public class ModernOopDemo {

    // sealed: список наследников закрыт — компилятор знает их все
    sealed interface Expr permits Num, Add, Mul, Neg {}

    // record — неизменяемый класс-носитель данных:
    // конструктор, геттеры x(), equals, hashCode, toString генерируются автоматически
    record Num(double value) implements Expr {}
    record Add(Expr left, Expr right) implements Expr {}
    record Mul(Expr left, Expr right) implements Expr {}
    record Neg(Expr operand) implements Expr {}

    // компактный конструктор record с валидацией
    record Range(int from, int to) {
        Range {
            if (from > to) throw new IllegalArgumentException(from + " > " + to);
        }

        int length() { return to - from; }   // в record можно добавлять методы
    }

    // switch по sealed-типу: default не нужен, компилятор проверяет полноту
    static double eval(Expr e) {
        return switch (e) {
            case Num n -> n.value();
            case Add(Expr l, Expr r) -> eval(l) + eval(r);    // record-деконструкция (Java 21)
            case Mul(Expr l, Expr r) -> eval(l) * eval(r);
            case Neg(Expr x) -> -eval(x);
        };
    }

    static String show(Expr e) {
        return switch (e) {
            case Num n when n.value() < 0 -> "(" + n.value() + ")";  // guard-условие
            case Num n -> String.valueOf(n.value());
            case Add a -> "(" + show(a.left()) + " + " + show(a.right()) + ")";
            case Mul m -> show(m.left()) + " * " + show(m.right());
            case Neg n -> "-" + show(n.operand());
        };
    }

    public static void main(String[] args) {
        // (2 + 3) * -4
        Expr e = new Mul(new Add(new Num(2), new Num(3)), new Neg(new Num(4)));
        System.out.println(show(e) + " = " + eval(e));
        System.out.println(e);   // автоматический toString у record

        System.out.println(new Num(1).equals(new Num(1)));   // true — сравнение по значению

        Range r = new Range(3, 10);
        System.out.println(r + ", length = " + r.length());
        try {
            new Range(10, 3);
        } catch (IllegalArgumentException ex) {
            System.out.println("Неверный диапазон: " + ex.getMessage());
        }

        // var + записи в коллекциях
        var exprs = List.<Expr>of(new Num(7), new Neg(new Num(1)), new Add(new Num(1), new Num(1)));
        for (var x : exprs) System.out.println(show(x) + " -> " + eval(x));
    }
}
