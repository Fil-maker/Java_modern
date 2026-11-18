package lesson5;

import java.io.*;
import java.nio.file.*;
import java.time.Year;
import java.util.*;

/*
 * Задача «Загрузка с проверкой»
 *
 * Файл (аргумент командной строки, по умолчанию src/lesson5/students.csv)
 * содержит строку заголовка и строки вида
 *   Фамилия;Группа;Год рождения;Средний балл
 * Часть строк испорчена. Правила корректной строки:
 *   - ровно 4 поля, фамилия и группа не пустые;
 *   - год — целое число от 1950 до текущего года;
 *   - средний балл — число от 2 до 5 (допускается запятая вместо точки).
 *
 * Загрузите все корректные строки и соберите ВСЕ ошибки, а не только первую.
 * Для ошибки строки заведите проверяемое исключение BadRecordException
 * с номером строки и названием поля. Выведите:
 *   1) загруженных студентов, отсортированных по убыванию балла;
 *   2) список ошибок в порядке строк;
 *   3) итог: сколько загружено и сколько отклонено.
 *
 * Пример вывода для приложенного students.csv (в 2026 году):
 *   Загружено:
 *     Кузнецов   ПМИ-1  2004  5.00
 *     Петрова    ИВТ-2  2006  4.90
 *     Фёдоров    ИВТ-1  2006  4.70
 *     Иванов     ИВТ-1  2005  4.20
 *     Алексеева  ИВТ-2  2006  3.60
 *   Ошибки:
 *     строка 4, поле «год»: не целое число «две тысячи пятый»
 *     строка 6: ожидалось 4 поля, получено 3
 *     строка 7, поле «группа»: пустое значение
 *     строка 9, поле «год»: 1890 вне диапазона 1950..2026
 *     строка 10, поле «балл»: 6.5 вне диапазона 2..5
 *   Загружено 5, отклонено 5
 */
public class StudentLoader {

    record Student(String name, String group, int birthYear, double avg) {}

    static class BadRecordException extends Exception {
        final int line;
        final String field;   // null, если ошибка не в конкретном поле

        BadRecordException(int line, String field, String message) {
            super(message);
            this.line = line;
            this.field = field;
        }

        @Override
        public String toString() {
            return "строка " + line + (field == null ? "" : ", поле «" + field + "»") + ": " + getMessage();
        }
    }

    static Student parse(String line, int lineNo) throws BadRecordException {
        String[] p = line.split(";", -1);   // -1: не терять пустые поля в конце
        if (p.length != 4) {
            throw new BadRecordException(lineNo, null, "ожидалось 4 поля, получено " + p.length);
        }
        String name = requireNotBlank(p[0], lineNo, "фамилия");
        String group = requireNotBlank(p[1], lineNo, "группа");

        int year;
        try {
            year = Integer.parseInt(p[2].trim());
        } catch (NumberFormatException e) {
            throw new BadRecordException(lineNo, "год", "не целое число «" + p[2].trim() + "»");
        }
        int now = Year.now().getValue();
        if (year < 1950 || year > now) {
            throw new BadRecordException(lineNo, "год", year + " вне диапазона 1950.." + now);
        }

        double avg;
        try {
            avg = Double.parseDouble(p[3].trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new BadRecordException(lineNo, "балл", "не число «" + p[3].trim() + "»");
        }
        if (avg < 2 || avg > 5) {
            throw new BadRecordException(lineNo, "балл", avg + " вне диапазона 2..5");
        }
        return new Student(name, group, year, avg);
    }

    static String requireNotBlank(String s, int lineNo, String field) throws BadRecordException {
        if (s.isBlank()) throw new BadRecordException(lineNo, field, "пустое значение");
        return s.trim();
    }

    public static void main(String[] args) {
        Path path = Path.of(args.length > 0 ? args[0] : "src/lesson5/students.csv");

        List<Student> students = new ArrayList<>();
        List<BadRecordException> errors = new ArrayList<>();

        try (BufferedReader in = Files.newBufferedReader(path)) {
            in.readLine();                     // заголовок
            String line;
            int lineNo = 1;
            while ((line = in.readLine()) != null) {
                lineNo++;
                if (line.isBlank()) continue;
                try {
                    students.add(parse(line, lineNo));
                } catch (BadRecordException e) {
                    errors.add(e);             // запоминаем и идём дальше
                }
            }
        } catch (NoSuchFileException e) {
            System.out.println("Файл не найден: " + path);
            return;
        } catch (IOException e) {
            System.out.println("Ошибка чтения: " + e);
            return;
        }

        students.sort(Comparator.comparingDouble(Student::avg).reversed()
                .thenComparing(Student::name));
        System.out.println("Загружено:");
        for (Student s : students) {
            System.out.printf(Locale.ROOT, "  %-10s %-6s %d  %.2f%n", s.name(), s.group(), s.birthYear(), s.avg());
        }
        System.out.println("Ошибки:");
        for (BadRecordException e : errors) {
            System.out.println("  " + e);
        }
        System.out.println("Загружено " + students.size() + ", отклонено " + errors.size());
    }
}
