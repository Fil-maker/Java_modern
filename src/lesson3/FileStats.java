package lesson3;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/*
 * Задача «Статистика файла» (аналог утилиты wc)
 *
 * Имя файла передаётся аргументом командной строки (по умолчанию input.txt).
 * Выведите число строк, слов, символов и байт в файле, а также номер
 * и длину самой длинной строки. Файл читается в UTF-8.
 * Если файла нет — выведите «Файл не найден: <абсолютный путь>».
 *
 * Пример для файла из двух строк «Привет, мир» и «Java»:
 *   Строк:    2
 *   Слов:     3
 *   Символов: 15
 *   Байт:     26
 *   Самая длинная строка: №1, 11 символов
 */
public class FileStats {
    public static void main(String[] args) {
        Path path = Path.of(args.length > 0 ? args[0] : "input.txt");

        long lines = 0, words = 0, chars = 0;
        long longestNo = 0, longestLen = -1;

        // newBufferedReader держит файл открытым — закрываем через try-with-resources
        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            while ((line = br.readLine()) != null) {
                lines++;
                chars += line.length();
                StringTokenizer st = new StringTokenizer(line);
                words += st.countTokens();
                if (line.length() > longestLen) {
                    longestLen = line.length();
                    longestNo = lines;
                }
            }

            System.out.println("Строк:    " + lines);
            System.out.println("Слов:     " + words);
            System.out.println("Символов: " + chars);
            // байты и символы различаются: кириллица в UTF-8 занимает 2 байта
            System.out.println("Байт:     " + Files.size(path));
            if (lines > 0) {
                System.out.println("Самая длинная строка: №" + longestNo + ", " + longestLen + " символов");
            }
        } catch (NoSuchFileException e) {
            System.out.println("Файл не найден: " + path.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("Ошибка чтения: " + e.getMessage());
        }
    }
}
