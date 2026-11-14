package lesson4;

import java.io.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

/*
 * Задача «Ближайший автобус»
 *
 * Сначала идёт расписание остановки: строки вида
 *   <номер маршрута> <время> <время> ...      (время в формате HH:mm)
 * до пустой строки. Затем до конца ввода — запросы: время прихода на остановку.
 * Для каждого запроса выведите ближайшее отправление не раньше этого времени,
 * все маршруты, которые уходят в эту минуту (по возрастанию номера),
 * и сколько минут ждать. Если автобусов больше нет —
 * «HH:mm → сегодня автобусов больше нет».
 *
 * Ввод:                  Вывод:
 * 12 07:05 07:35 08:15   07:00 → 07:05, маршрут 12, ждать 5 мин
 * 7 07:35 08:40          07:35 → 07:35, маршрут 7, 12, ждать 0 мин
 * 3 07:20 07:50          07:51 → 08:15, маршрут 12, ждать 24 мин
 *                        09:00 → сегодня автобусов больше нет
 * 07:00
 * 07:35
 * 07:51
 * 09:00
 */
public class NextBus {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        // время отправления → номера маршрутов (TreeMap хранит ключи по порядку)
        TreeMap<LocalTime, TreeSet<Integer>> schedule = new TreeMap<>();
        String line;
        while ((line = br.readLine()) != null && !line.isBlank()) {
            String[] parts = line.trim().split("\\s+");
            int route = Integer.parseInt(parts[0]);
            for (int i = 1; i < parts.length; i++) {
                // computeIfAbsent создаёт множество для нового времени
                schedule.computeIfAbsent(LocalTime.parse(parts[i]), t -> new TreeSet<>()).add(route);
            }
        }

        StringBuilder sb = new StringBuilder();
        while ((line = br.readLine()) != null) {
            if (line.isBlank()) continue;
            LocalTime now = LocalTime.parse(line.trim());

            // ceilingEntry — наименьший ключ >= now, за O(log n) вместо перебора
            Map.Entry<LocalTime, TreeSet<Integer>> next = schedule.ceilingEntry(now);
            sb.append(now).append(" → ");
            if (next == null) {
                sb.append("сегодня автобусов больше нет");
            } else {
                String routes = next.getValue().toString().replaceAll("[\\[\\]]", "");
                long wait = ChronoUnit.MINUTES.between(now, next.getKey());
                sb.append(next.getKey()).append(", маршрут ").append(routes)
                  .append(", ждать ").append(wait).append(" мин");
            }
            sb.append('\n');
        }
        System.out.print(sb);
    }
}
