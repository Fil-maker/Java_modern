package lesson3;

import java.io.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

/*
 * Задача «День рождения»
 *
 * Даны дата рождения и сегодняшняя дата в формате dd.MM.yyyy.
 * Выведите:
 *   - полный возраст в годах;
 *   - сколько дней осталось до ближайшего дня рождения
 *     (если он сегодня — «Сегодня день рождения!»);
 *   - день недели, в который человек родился.
 * Родившиеся 29 февраля в невисокосный год празднуют 28 февраля.
 *
 * Ввод:            Вывод:
 * 14.05.2007       Возраст: 19
 * 30.10.2026       До дня рождения: 196 дн.
 *                  День недели рождения: понедельник
 */
public class Birthday {
    static final DateTimeFormatter F = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        LocalDate birth = LocalDate.parse(br.readLine().trim(), F);
        LocalDate today = LocalDate.parse(br.readLine().trim(), F);

        // Period раскладывает интервал на годы, месяцы и дни
        int age = Period.between(birth, today).getYears();
        System.out.println("Возраст: " + age);

        // withYear для 29 февраля в невисокосный год сам даёт 28 февраля
        LocalDate next = birth.withYear(today.getYear());
        if (next.isBefore(today)) next = birth.withYear(today.getYear() + 1);

        // общее число дней — ChronoUnit, а не Period.getDays()
        long days = ChronoUnit.DAYS.between(today, next);
        if (days == 0) System.out.println("Сегодня день рождения!");
        else System.out.println("До дня рождения: " + days + " дн.");

        String dow = birth.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.of("ru"));
        System.out.println("День недели рождения: " + dow);
    }
}
