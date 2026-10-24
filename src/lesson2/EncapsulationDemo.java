package lesson2;

/**
 * Принцип 1. ИНКАПСУЛЯЦИЯ
 * Состояние объекта скрыто (private), изменять его можно только
 * через методы, которые проверяют инварианты.
 */
public class EncapsulationDemo {

    static class BankAccount {
        private final String owner;   // не меняется после создания
        private long balance;         // в копейках, снаружи напрямую недоступен

        BankAccount(String owner, long initialBalance) {
            if (initialBalance < 0) {
                throw new IllegalArgumentException("Начальный баланс не может быть отрицательным");
            }
            this.owner = owner;
            this.balance = initialBalance;
        }

        // геттер: только чтение
        public long getBalance() {
            return balance;
        }

        public String getOwner() {
            return owner;
        }

        // вместо setBalance — осмысленные операции с проверками
        public void deposit(long amount) {
            if (amount <= 0) throw new IllegalArgumentException("Сумма должна быть положительной");
            balance += amount;
        }

        public void withdraw(long amount) {
            if (amount <= 0) throw new IllegalArgumentException("Сумма должна быть положительной");
            if (amount > balance) throw new IllegalStateException("Недостаточно средств");
            balance -= amount;
        }

        @Override
        public String toString() {
            return owner + ": " + balance / 100 + "." + String.format("%02d", balance % 100) + " руб.";
        }
    }

    public static void main(String[] args) {
        BankAccount acc = new BankAccount("Иван", 100_00);
        acc.deposit(50_00);
        acc.withdraw(30_00);
        System.out.println(acc);

        // acc.balance = -1_000_000;   // ошибка компиляции: balance has private access

        try {
            acc.withdraw(1_000_00);
        } catch (IllegalStateException e) {
            System.out.println("Отказ: " + e.getMessage());
        }
        System.out.println("Баланс не испорчен: " + acc);
    }
}
