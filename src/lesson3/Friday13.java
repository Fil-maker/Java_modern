package lesson3;

import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

/*
 * Задача «Пятница, 13-е»
 *
 * Даны два года a ≤ b. Найдите все пятницы, выпавшие на 13-е число,
 * в годах с a по b включительно. Выведите их количество, затем сами даты.
 *
 * Ввод:            Вывод:
 * 2026 2026        3
 *                  13.02.2026
 *                  13.03.2026
 *                  13.11.2026
 */
public class Friday13 {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        int a = Integer.parseInt(st.nextToken());
        int b = Integer.parseInt(st.nextToken());

        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        List<LocalDate> found = new ArrayList<>();
        for (int y = a; y <= b; y++) {
            for (Month m : Month.values()) {
                // YearMonth — ещё один тип java.time: месяц конкретного года
                LocalDate d = YearMonth.of(y, m).atDay(13);
                if (d.getDayOfWeek() == DayOfWeek.FRIDAY) found.add(d);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(found.size()).append('\n');
        for (LocalDate d : found) sb.append(d.format(f)).append('\n');
        System.out.print(sb);
    }
}
