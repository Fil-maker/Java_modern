package lesson2;

import java.util.List;

/**
 * Принцип 3. ПОЛИМОРФИЗМ
 * - динамический (переопределение): метод выбирается по реальному типу объекта во время выполнения;
 * - статический (перегрузка): метод выбирается компилятором по типам аргументов.
 */
public class PolymorphismDemo {

    static abstract class Shape {
        abstract double area();

        String name() {
            return getClass().getSimpleName();
        }
    }

    static class Circle extends Shape {
        private final double r;

        Circle(double r) { this.r = r; }

        @Override
        double area() { return Math.PI * r * r; }
    }

    static class Rectangle extends Shape {
        private final double w, h;

        Rectangle(double w, double h) { this.w = w; this.h = h; }

        @Override
        double area() { return w * h; }
    }

    static class Square extends Rectangle {
        Square(double side) { super(side, side); }
    }

    // ---- Перегрузка (overloading): одно имя, разные параметры ----
    static class Printer {
        void print(int x)    { System.out.println("int: " + x); }
        void print(double x) { System.out.println("double: " + x); }
        void print(String s) { System.out.println("String: " + s); }
        void print(Shape s)  { System.out.println("Shape: " + s.name()); }
        void print(Circle c) { System.out.println("Circle (особый случай)"); }
    }

    public static void main(String[] args) {
        // Переменная типа Shape, объекты — разных классов
        List<Shape> shapes = List.of(new Circle(1), new Rectangle(2, 3), new Square(4));

        double total = 0;
        for (Shape s : shapes) {
            // какой area() вызовется — решается во время выполнения (позднее связывание)
            System.out.printf("%-10s площадь = %.2f%n", s.name(), s.area());
            total += s.area();
        }
        System.out.printf("Суммарная площадь: %.2f%n%n", total);

        Printer pr = new Printer();
        pr.print(42);
        pr.print(3.14);
        pr.print("hello");

        Shape sh = new Circle(5);
        pr.print(sh);              // Shape: Circle — перегрузка выбирается по СТАТИЧЕСКОМУ типу переменной!
        pr.print(new Circle(5));   // Circle (особый случай)
    }
}
