package lesson3;

import java.io.*;
import java.time.*;
import java.time.format.*;

/*
 * Задача «Мировые часы»
 *
 * В первой строке — дата, время и часовой пояс события: dd.MM.yyyy HH:mm Zone.
 * Во второй — число n, дальше n строк с идентификаторами поясов (Europe/London, ...).
 * Для каждого пояса выведите, сколько там будет времени в этот момент, и смещение от UTC.
 * Если пояс неизвестен — выведите «неизвестный пояс».
 *
 * Ввод:                              Вывод:
 * 30.10.2026 14:05 Europe/Moscow     UTC               30.10.2026 11:05  Z
 * 4                                  Asia/Vladivostok  30.10.2026 21:05  +10:00
 * UTC                                America/New_York  30.10.2026 07:05  -04:00
 * Asia/Vladivostok                   Mars/Olympus      неизвестный пояс
 * America/New_York
 * Mars/Olympus
 */
public class WorldClock {
    static final DateTimeFormatter F = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String[] parts = br.readLine().trim().split("\\s+");
        LocalDateTime local = LocalDateTime.parse(parts[0] + " " + parts[1], F);
        ZonedDateTime event = local.atZone(ZoneId.of(parts[2]));

        int n = Integer.parseInt(br.readLine().trim());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            String id = br.readLine().trim();
            sb.append(String.format("%-18s", id));
            try {
                // тот же момент времени, другой пояс
                ZonedDateTime there = event.withZoneSameInstant(ZoneId.of(id));
                sb.append(there.format(F)).append("  ").append(there.getOffset());
            } catch (DateTimeException e) {
                sb.append("неизвестный пояс");
            }
            sb.append('\n');
        }
        System.out.print(sb);
    }
}
