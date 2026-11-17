# Контест к уроку 5: исключения и обобщения

**Формат:** 4 задачи по 10 баллов, язык — Java. Решение читает данные из стандартного ввода и печатает ответ в стандартный вывод.

> Это папка преподавателя: решения и тесты с ответами лежат рядом с условиями. Студентам выдаются только файлы `README.md` из папок задач.

## Задачи

| Буква | Задача | Что проверяется | Неверное решение в пакете |
|-------|--------|-----------------|---------------------------|
| A | [Безопасный калькулятор](task1_safe_calc/README.md) | своё исключение, `NumberFormatException`, `Math.*Exact` | без проверки переполнения |
| B | [Банк](task2_bank/README.md) | checked и unchecked исключения, атомарность операции | перевод не атомарный |
| C | [Обобщённый стек с минимумом](task3_minmax_stack/README.md) | `<T extends Comparable<? super T>>`, один код для `Long` и `String` | числа сравниваются как строки |
| D | [LRU-кеш](task4_lru_cache/README.md) | обобщённый класс `LruCache<K, V>`, `LinkedHashMap` | `O(c)` на команду, не проходит по времени |

Оценка внутри задачи: тесты из условия — 0 баллов, группа 1 — 6 баллов, группа 2 — 4 балла. Группа засчитывается, только если пройдены все её тесты.

## Файлы

- `taskN_*/README.md` — условие; из него собирается условие в Polygon.
- `taskN_*/*Task.java` — эталонное решение.
- `taskN_*/` — второе решение с ошибкой: на нём видно, что тесты её ловят.
- `taskN_*/tests/` — тесты (`01`, `01.a`, …) и состав групп `groups.json`.
- `generate_tests.py` — генератор тестов: ответ даёт эталон на Java, каждый ответ сверяется с независимой проверкой на Python.
- `polygon/` — сборщик пакетов Polygon, `testlib.h` и чекер `wcmp.cpp`.

## Сборка

Нужны Python 3 и JDK 17 или новее. JDK берётся из `JAVA_HOME`, иначе самый новый из `~/.jdks`, иначе из `PATH`. Команды выполняются из корня репозитория.

Сгенерировать тесты:

```bash
python src/lesson5/contest/generate_tests.py
```

Прогнать все решения по тестам: эталоны должны пройти всё, неверные — упасть:

```bash
python src/lesson5/contest/generate_tests.py --check
```

Собрать пакеты отдельных задач в `out/lesson5-polygon/`:

```bash
python src/lesson5/contest/polygon/build_packages.py
```

Собрать соревнование в `out/lesson5-contest/` (пакеты с группами, `contest.zip` и памятка `CONTEST.md` с настройками тестирующей системы):

```bash
python src/lesson5/contest/polygon/build_contest.py
```

В тестирующую систему загружайте пакеты задач по одному: `out/lesson5-contest/problems/A-safe-calc.zip` … `D-lru-cache.zip`. В корне каждого лежит `problem.xml`. Импорт задачи из Polygon не примет `contest.zip` с ошибкой «problem.xml not found»: это пакет всего соревнования, задачи в нём лежат в `problems/<буква>/`.

## Запуск решения

На примере задачи A:

```bash
javac -encoding UTF-8 -d out src/lesson5/contest/task1_safe_calc/SafeCalcTask.java
```

```bash
java -cp out lesson5.contest.task1_safe_calc.SafeCalcTask
```

В пакетах Polygon строка `package` из решений удаляется автоматически.
