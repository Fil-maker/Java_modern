package lesson2;

import java.util.ArrayList;
import java.util.List;

/**
 * Принцип 4. АБСТРАКЦИЯ
 * Выделяем существенное поведение в интерфейс/абстрактный класс,
 * а детали реализации прячем. Клиентский код зависит от абстракции.
 */
public class AbstractionDemo {

    // Интерфейс — контракт: "что умеет", без "как"
    interface PaymentMethod {
        boolean pay(long amount);

        // default-метод (Java 8+): общая реализация прямо в интерфейсе
        default String describe() {
            return "Способ оплаты " + getClass().getSimpleName();
        }

        // static-метод интерфейса
        static PaymentMethod cash() {
            return new PaymentMethod() {            // анонимный класс: реализация "на месте"
                @Override
                public boolean pay(long amount) {
                    System.out.println("  Оплата наличными: " + amount);
                    return true;
                }

                @Override
                public String describe() { return "Наличные"; }
            };
        }
    }

    // Абстрактный класс — частичная реализация: общий алгоритм + "дырки" для наследников
    static abstract class CardPayment implements PaymentMethod {
        protected final String number;

        CardPayment(String number) { this.number = number; }

        // шаблонный метод: последовательность шагов фиксирована
        @Override
        public final boolean pay(long amount) {
            if (!authorize(amount)) {
                System.out.println("  " + bankName() + ": отказ в авторизации");
                return false;
            }
            System.out.println("  " + bankName() + ": списано " + amount + " с карты *" + number.substring(number.length() - 4));
            return true;
        }

        protected abstract boolean authorize(long amount);
        protected abstract String bankName();
    }

    static class DebitCard extends CardPayment {
        private long balance;

        DebitCard(String number, long balance) { super(number); this.balance = balance; }

        @Override
        protected boolean authorize(long amount) {
            if (amount > balance) return false;
            balance -= amount;
            return true;
        }

        @Override
        protected String bankName() { return "Дебетовая"; }
    }

    static class CreditCard extends CardPayment {
        private final long limit;
        private long debt;

        CreditCard(String number, long limit) { super(number); this.limit = limit; }

        @Override
        protected boolean authorize(long amount) {
            if (debt + amount > limit) return false;
            debt += amount;
            return true;
        }

        @Override
        protected String bankName() { return "Кредитная"; }
    }

    // Клиент ничего не знает о конкретных классах
    static class Shop {
        private final List<String> log = new ArrayList<>();

        void checkout(String item, long price, PaymentMethod method) {
            System.out.println(item + " за " + price + " — " + method.describe());
            log.add(item + (method.pay(price) ? ": OK" : ": FAIL"));
        }

        List<String> getLog() { return log; }
    }

    public static void main(String[] args) {
        Shop shop = new Shop();
        shop.checkout("Книга", 500, new DebitCard("1111222233334444", 1000));
        shop.checkout("Ноутбук", 90_000, new DebitCard("1111222233334444", 1000));
        shop.checkout("Ноутбук", 90_000, new CreditCard("5555666677778888", 100_000));
        shop.checkout("Кофе", 200, PaymentMethod.cash());
        System.out.println("Журнал: " + shop.getLog());

        // new PaymentMethod();  // ошибка: нельзя создать экземпляр интерфейса
        // new CardPayment("1"); // ошибка: CardPayment is abstract
    }
}
