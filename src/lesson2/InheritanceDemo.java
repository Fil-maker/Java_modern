package lesson2;

/**
 * Принцип 2. НАСЛЕДОВАНИЕ
 * Подкласс получает поля и методы суперкласса (extends),
 * может их расширять и переопределять (@Override), вызывать super.
 */
public class InheritanceDemo {

    static class Animal {
        protected final String name;   // protected — виден наследникам

        Animal(String name) {
            this.name = name;
            System.out.println("  конструктор Animal(" + name + ")");
        }

        void eat() {
            System.out.println(name + " ест");
        }

        String sound() {
            return "...";
        }

        void speak() {
            System.out.println(name + " говорит: " + sound());
        }
    }

    static class Dog extends Animal {
        Dog(String name) {
            super(name);               // вызов конструктора родителя — всегда первым
            System.out.println("  конструктор Dog(" + name + ")");
        }

        @Override
        String sound() {
            return "Гав!";
        }

        // новый метод, которого нет у Animal
        void fetch() {
            System.out.println(name + " приносит палку");
        }
    }

    static class Puppy extends Dog {
        Puppy(String name) {
            super(name);
        }

        @Override
        String sound() {
            return super.sound().toLowerCase() + " (тоненько)";  // используем реализацию родителя
        }
    }

    // final-класс нельзя унаследовать: class Kitten extends Cat — ошибка
    static final class Cat extends Animal {
        Cat(String name) {
            super(name);
        }

        @Override
        String sound() {
            return "Мяу";
        }
    }

    public static void main(String[] args) {
        System.out.println("Создаём Puppy:");
        Puppy p = new Puppy("Шарик");   // цепочка конструкторов: Animal -> Dog -> Puppy
        p.eat();                        // унаследовано от Animal
        p.fetch();                      // унаследовано от Dog
        p.speak();                      // speak() из Animal вызывает переопределённый sound()

        Cat c = new Cat("Мурка");
        c.speak();

        // Все классы неявно наследуют java.lang.Object
        System.out.println(p instanceof Animal);   // true
        System.out.println(p instanceof Object);   // true
        System.out.println(p.getClass().getSuperclass().getSimpleName()); // Dog
    }
}
