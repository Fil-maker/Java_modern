"""Собирает соревнование из четырёх задач урока 5 «Исключения и обобщения».

Результат в out/lesson5-contest/:
    problems/A-safe-calc.zip ... D-lru-cache.zip — пакеты задач с оценкой по группам
    contest.zip — пакет соревнования: contest.xml и распакованные задачи в problems/<буква>/
    CONTEST.md — какие настройки выставить в тестирующей системе

Оценка: за каждую задачу 10 баллов, штрафов нет. Примеры из условия — 0 баллов,
группа 1 — 6 баллов, группа 2 — 4 балла. Состав групп генератор записывает
в tests/groups.json каждой задачи. Группа засчитывается, только если пройдены все её тесты.

Запуск из корня репозитория (тесты должны быть сгенерированы):
    python src/lesson5/contest/polygon/build_contest.py

ВНИМАНИЕ: пакеты содержат ответы. Не публиковать.
"""
import json
import shutil
import sys
import zipfile
from xml.sax.saxutils import quoteattr

from build_packages import EXAM, OUT, TASKS, build

CONTEST_NAME = "Урок 5: исключения и обобщения"
CONTEST_OUT = OUT.parent / "lesson5-contest"

# буква, короткое имя задачи, описания групп 1 и 2
CONTEST = [
    ("A", "safe-calc", "ошибки формата, чисел, операций и деления на ноль",
     "переполнение и граничные значения long"),
    ("B", "bank", "маленькие и граничные тесты", "большие тесты (10⁵ команд)"),
    ("C", "minmax-stack", "маленькие и граничные тесты", "большие тесты (2 · 10⁵ команд)"),
    ("D", "lru-cache", "маленькие и граничные тесты", "большие тесты (2 · 10⁵ команд, ёмкость до 10⁵)"),
]
POINTS = {"1": 6, "2": 4}


def contest_xml(problems):
    items = "\n".join(f'        <problem index="{letter}" path="problems/{letter}"/>' for letter, _, _ in problems)
    return f"""<?xml version="1.0" encoding="utf-8" standalone="no"?>
<contest>
    <names>
        <name language="russian" main="true" value={quoteattr(CONTEST_NAME)}/>
    </names>
    <problems>
{items}
    </problems>
</contest>
"""


def contest_md(problems):
    rows = "\n".join(f"| {letter} | {name} | 10 |" for letter, name, _ in problems)
    groups = "\n".join(f"| {letter} | {g1} | {g2} |" for letter, _, (g1, g2) in problems)
    files = "\n".join(f"| {letter} | `problems/{letter}-{name.split(' — ')[0]}.zip` |" for letter, name, _ in problems)
    return f"""# {CONTEST_NAME}

ВНИМАНИЕ: пакеты содержат ответы. Не публиковать и не загружать в открытый доступ.

## Как загружать

Импорт задачи из Polygon («polygon problem import») принимает **один пакет задачи** —
архив, в корне которого лежит `problem.xml`. Это файлы из `problems/`:

| Буква | Файл |
|---|---|
{files}

Загрузите их по одному и добавьте задачи в соревнование под буквами A–D.

`contest.zip` — пакет соревнования целиком (`contest.xml` и задачи в `problems/<буква>/`).
`problem.xml` в его корне нет, поэтому импорт задачи его не примет
(ошибка «problem.xml not found»). Он нужен только системам, которые умеют импортировать
соревнование из Polygon целиком.

## Задачи

| Буква | Пакет | Баллы |
|---|---|---|
{rows}

Всего 40 баллов.

## Оценка внутри задачи

- Тесты из условия — 0 баллов (группа 0, участнику показывается полный протокол).
- Группа 1 — 6 баллов, группа 2 — 4 балла.
- Баллы за группу начисляются, только если пройдены все её тесты.

| Задача | Группа 1 (6 баллов) | Группа 2 (4 балла) |
|---|---|---|
{groups}

Баллы и группы записаны в `problem.xml` каждой задачи (атрибуты `group` и `points` у тестов,
`points-policy="complete-group"` у групп). Система оценки описана и в «Примечании» условий.

## Решения в пакетах

Все решения на Java 17. В каждом пакете есть эталон (`main`) и решение с ошибкой,
на котором видно, что тесты её ловят:

- A — без проверки переполнения (`wrong-answer`, теряет группу 2);
- B — перевод не атомарный (`wrong-answer`);
- C — числа сравниваются как строки (`wrong-answer` на тестах `INT`);
- D — LRU на `ArrayList`, `O(c)` на команду (`time-limit-exceeded` на группе 2).

## Что выставить в тестирующей системе вручную

- Правила соревнования: олимпиадные (IOI, по баллам), а не ACM/ICPC.
- Штрафы: выключить — ни штрафного времени, ни штрафа за неудачные попытки.
- В зачёт идёт лучшая (или последняя) посылка по задаче — как принято у вас.
- Длительность соревнования и список участников.
- Язык: Java (17 или новее). Решения читают стандартный ввод и пишут в стандартный вывод,
  класс без пакета (строку `package` тестирующая система не примет).
"""


def main():
    by_short = {t[1]: t for t in TASKS}
    # чистим содержимое, а не саму папку: её может держать открытой проводник или терминал
    CONTEST_OUT.mkdir(parents=True, exist_ok=True)
    for item in CONTEST_OUT.iterdir():
        shutil.rmtree(item) if item.is_dir() else item.unlink()
    names = []
    contest_zip = CONTEST_OUT / "contest.zip"
    with zipfile.ZipFile(contest_zip, "w", zipfile.ZIP_DEFLATED) as cz:
        for letter, short, desc1, desc2 in CONTEST:
            task_dir, _, tl, checker, solutions = by_short[short]
            members = json.loads((EXAM / task_dir / "tests" / "groups.json").read_text(encoding="utf-8"))
            scoring = [("1", POINTS["1"], members["1"], desc1), ("2", POINTS["2"], members["2"], desc2)]
            pkg = build(task_dir, short, tl, checker, solutions,
                        out_path=CONTEST_OUT / "problems" / f"{letter}-{short}.zip", scoring=scoring)
            with zipfile.ZipFile(pkg) as pz:
                for item in pz.infolist():
                    cz.writestr(f"problems/{letter}/{item.filename}", pz.read(item))
                name = pz.read("statement-sections/russian/name.tex").decode("utf-8").strip()
            names.append((letter, f"{short} — {name}", (desc1, desc2)))
        cz.writestr("contest.xml", contest_xml(names))
    (CONTEST_OUT / "CONTEST.md").write_text(contest_md(names), encoding="utf-8")
    print("Готово:", CONTEST_OUT)


if __name__ == "__main__":
    sys.stdout.reconfigure(encoding="utf-8")
    main()
