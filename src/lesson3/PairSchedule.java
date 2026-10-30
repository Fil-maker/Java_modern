package lesson3;

import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

/*
 * Задача «Расписание пар»
 *
 * Пара длится 90 минут. Даны время начала первой пары и количество пар n,
 * во второй строке — n-1 чисел: длины перемен в минутах.
 * Выведите время начала и конца каждой пары, время окончания занятий
 * и сколько всего времени студент провёл в университете.
 *
 * Ввод:            Вывод:
 * 09:00 3          1 пара: 09:00–10:30
 * 10 20            2 пара: 10:40–12:10
 *                  3 пара: 12:30–14:00
 *                  Конец занятий: 14:00, всего 5 ч 0 мин
 */
public class PairSchedule {
    static final Duration PAIR = Duration.ofMinutes(90);
    static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        LocalTime start = LocalTime.parse(st.nextToken(), HM);
        int n = Integer.parseInt(st.nextToken());

        // при n = 1 строки с переменами может не быть
        String line = br.readLine();
        st = new StringTokenizer(line == null ? "" : line);

        StringBuilder sb = new StringBuilder();
        LocalTime t = start;
        for (int i = 1; i <= n; i++) {
            LocalTime end = t.plus(PAIR);
            sb.append(i).append(" пара: ")
              .append(t.format(HM)).append('–').append(end.format(HM)).append('\n');
            t = end;
            if (i < n) t = t.plusMinutes(Long.parseLong(st.nextToken()));
        }

        // LocalTime живёт в пределах суток: занятия после полуночи здесь не учитываем
        Duration total = Duration.between(start, t);
        sb.append("Конец занятий: ").append(t.format(HM))
          .append(", всего ").append(total.toHours()).append(" ч ")
          .append(total.toMinutesPart()).append(" мин");
        System.out.println(sb);
    }
}
