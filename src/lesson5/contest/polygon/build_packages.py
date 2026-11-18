"""Собирает пакеты в формате Polygon для задач контеста урока 5.

Для каждой задачи получается архив out/lesson5-polygon/<short-name>.zip:
    problem.xml                       — описание задачи
    statements/russian/problem.tex    — условие (olymp.sty), собранное из README.md
    statements/russian/example.NN(.a) — примеры из условия
    statement-sections/russian/*.tex  — то же условие по разделам
    tests/NN, tests/NN.a              — тесты и ответы (первые — примеры, sample)
    files/testlib.h, files/check.cpp  — чекер wcmp (сравнение по словам)
    check.cpp                         — копия чекера
    solutions/*                       — решения на Java: эталон (main) и неверные
                                        (wrong-answer, time-limit-exceeded) для проверки тестов

Перед запуском сгенерируйте тесты: python src/lesson5/contest/generate_tests.py
Запуск из корня репозитория:  python src/lesson5/contest/polygon/build_packages.py

ВНИМАНИЕ: пакеты содержат ответы. Не публиковать.
"""
import re
import shutil
import sys
import zipfile
from pathlib import Path
from xml.sax.saxutils import quoteattr

HERE = Path(__file__).resolve().parent
EXAM = HERE.parent                                   # каталог контеста с папками задач
OUT = EXAM.parent.parent.parent / "out" / "lesson5-polygon"

# каталог, короткое имя, тайм-лимит (мс), чекер, решения [(файл, тип, тег)]
TASKS = [
    ("task1_safe_calc", "safe-calc", 2000, "wcmp",
     [("SafeCalcTask.java", "java17", "main"), ("SafeCalcNoOverflow.java", "java17", "wrong-answer")]),
    ("task2_bank", "bank", 2000, "wcmp",
     [("BankTask.java", "java17", "main"), ("BankNotAtomic.java", "java17", "wrong-answer")]),
    ("task3_minmax_stack", "minmax-stack", 2000, "wcmp",
     [("MinMaxStackTask.java", "java17", "main"), ("MinMaxStackAsStrings.java", "java17", "wrong-answer")]),
    ("task4_lru_cache", "lru-cache", 2000, "wcmp",
     [("LruCacheTask.java", "java17", "main"), ("LruCacheNaive.java", "java17", "time-limit-exceeded")]),
]

# ------------------------------------------------------------ Markdown -> LaTeX
SYMBOLS = {"≤": r"\le", "≥": r"\ge", "·": r"\cdot", "γ": r"\gamma", "α": r"\alpha", "Θ": r"\Theta",
           "≈": r"\approx", "×": r"\times", "←": r"\leftarrow", "→": r"\rightarrow", "′": "'"}
MATH_WORDS = {"mod": r"\bmod", "log": r"\log", "max": r"\max", "min": r"\min"}
FORCE_TT = {"S", "G", ".", "#", "NONE"}
SUPERSCRIPTS = "⁰¹²³⁴⁵⁶⁷⁸⁹"


def escape(s):
    out = []
    for ch in s:
        if ch == "\\":
            out.append(r"\textbackslash{}")
        elif ch in "{}$&#%_":
            out.append("\\" + ch)
        elif ch == "^":
            out.append(r"\^{}")
        elif ch == "~":
            out.append(r"\~{}")
        elif ch == "−":
            out.append("$-$")
        elif ch in SUPERSCRIPTS:
            out.append(f"$^{{{SUPERSCRIPTS.index(ch)}}}$")
        elif ch in SYMBOLS and SYMBOLS[ch] != "'":
            out.append(f"${SYMBOLS[ch]}$")
        else:
            out.append(ch)
    return "".join(out)


def is_math(code):
    if code in FORCE_TT or not re.search("[A-Za-z0-9" + "".join(SYMBOLS) + "]", code) or re.search(r'["%:;\\{}#*]|\.[A-Za-z]', code):
        return False
    return all(len(w) == 1 or w in MATH_WORDS for w in re.findall(r"[A-Za-z]+", code))


def to_math(code):
    s = code.replace("−", "-").replace("...", r"\ldots ")
    s = re.sub(r"\b(" + "|".join(MATH_WORDS) + r")\b", lambda m: MATH_WORDS[m.group(1)] + " ", s)
    s = re.sub(r"\^(\([^()]*\)|-?\w+)", lambda m: "^{" + m.group(1) + "}", s)
    s = re.sub(r"(?<=\d) (?=\d{3}\b)", r"\\,", s)
    for ch, cmd in SYMBOLS.items():
        s = s.replace(ch, cmd + (" " if cmd[-1].isalpha() else ""))
    return s


def code_span(code):
    return f"${to_math(code)}$" if is_math(code) else r"\texttt{" + escape(code) + "}"


def inline(text):
    parts, pos = [], 0
    for m in re.finditer(r"`([^`]*)`|\*\*(.+?)\*\*", text):
        parts.append(escape(text[pos:m.start()]))
        parts.append(code_span(m.group(1)) if m.group(1) is not None else r"\textbf{" + inline(m.group(2)) + "}")
        pos = m.end()
    parts.append(escape(text[pos:]))
    return "".join(parts)


def blocks(md):
    """Делит Markdown на блоки: ('code', текст), ('table', строки), ('ul'|'ol', пункты), ('p', текст)."""
    lines, i, res = md.split("\n"), 0, []
    while i < len(lines):
        line = lines[i]
        if line.startswith("```"):
            j = i + 1
            while not lines[j].startswith("```"):
                j += 1
            res.append(("code", "\n".join(lines[i + 1:j])))
            i = j + 1
        elif line.startswith("|"):
            j = i
            while j < len(lines) and lines[j].startswith("|"):
                j += 1
            res.append(("table", lines[i:j]))
            i = j
        elif re.match(r"(- |\d+\. )", line):
            kind = "ul" if line.startswith("- ") else "ol"
            items = []
            while i < len(lines) and re.match(r"(- |\d+\. )", lines[i]):
                items.append(re.sub(r"^(- |\d+\. )", "", lines[i]))
                i += 1
            res.append((kind, items))
        elif line.strip():
            j = i
            while j < len(lines) and lines[j].strip() and not re.match(r"(```|\||- |\d+\. )", lines[j]):
                j += 1
            res.append(("p", " ".join(lines[i:j])))
            i = j
        else:
            i += 1
    return res


def render(bl):
    out = []
    for kind, data in bl:
        if kind == "p":
            out.append(inline(data))
        elif kind in ("ul", "ol"):
            env = "itemize" if kind == "ul" else "enumerate"
            out.append(f"\\begin{{{env}}}\n" + "\n".join(r"\item " + inline(x) for x in data) + f"\n\\end{{{env}}}")
        elif kind == "table":
            rows = [[c.strip() for c in r.strip().strip("|").split("|")] for r in data if not re.match(r"^\|[\s:|-]+\|$", r)]
            cols = len(rows[0])
            body = [" & ".join(r"\textbf{" + inline(c) + "}" for c in rows[0]) + r" \\ \hline"]
            body += [" & ".join(inline(c) for c in r) + r" \\ \hline" for r in rows[1:]]
            out.append("\\begin{center}\n\\begin{tabular}{|" + "l|" * cols + "}\n\\hline\n" + "\n".join(body) + "\n\\end{tabular}\n\\end{center}")
        elif kind == "code":
            if is_math(data):
                out.append(r"\[ " + to_math(data) + r" \]")
            else:
                out.append("\\begin{center}\n" + r"\texttt{" + escape(data) + "}\n\\end{center}")
    return "\n\n".join(out)


def statement(task_dir):
    md = (EXAM / task_dir / "README.md").read_text(encoding="utf-8").replace("\r\n", "\n")
    title = re.match(r"# Задача [\w.]+\. (.+)", md).group(1).strip()
    chunks = re.split(r"^## (.+)$", md, flags=re.M)
    legend_md, sections = chunks[0].split("\n", 1)[1], dict(zip(chunks[1::2], chunks[2::2]))

    examples = re.findall(r"Ввод:\s*```\n(.*?)```\s*Вывод:\s*```\n(.*?)```", md, re.S)
    ex_md = sections.get("Примеры", sections.get("Пример", ""))
    ex_blocks, skip = [], False
    for kind, data in blocks(ex_md):
        if kind == "p" and data.strip() in ("Ввод:", "Вывод:"):
            skip = True
            continue
        if kind == "code" and skip:
            skip = False
            continue
        ex_blocks.append((kind, data))

    input_tex = render(blocks(sections["Ввод"]))
    if "Ограничения" in sections:
        input_tex += "\n\n\\textbf{Ограничения}\n\n" + render(blocks(sections["Ограничения"]))
    notes = []
    if ex_blocks:
        notes.append(render(ex_blocks))
    for head in ("Ожидаемая сложность", "Подсказка"):
        if head in sections:
            notes.append(f"\\textbf{{{head}.}} " + render(blocks(sections[head])))
    return {
        "name": title,
        "legend": render(blocks(legend_md)),
        "input": input_tex,
        "output": render(blocks(sections["Вывод"])),
        "notes": "\n\n".join(notes),
        "examples": examples,
    }


def seconds(ms):
    s = ms // 1000
    return f"{s} секунд" + ("а" if s == 1 else "ы" if s in (2, 3, 4) else "")


def problem_tex(st, tl):
    ex = "\n".join(f"\\exmpfile{{example.{i:02d}}}{{example.{i:02d}.a}}%" for i in range(1, len(st["examples"]) + 1))
    notes = f"\\Note\n{st['notes']}\n\n" if st["notes"] else ""
    return (f"\\begin{{problem}}{{{escape(st['name'])}}}{{стандартный ввод}}{{стандартный вывод}}{{{seconds(tl)}}}{{256 мегабайт}}\n\n"
            f"{st['legend']}\n\n\\InputFile\n{st['input']}\n\n\\OutputFile\n{st['output']}\n\n"
            f"\\Examples\n\n{ex}\n\n{notes}\\end{{problem}}\n")


# ------------------------------------------------------------ problem.xml
def num(x):
    return f"{x:.4f}".rstrip("0").rstrip(".") if x != int(x) else f"{x:.1f}"


def problem_xml(short, name, tl, n_tests, n_samples, checker, solutions, groups=None):
    """groups — список (имя, баллы, [номера тестов с 1]) для оценки по группам; примеры — группа 0."""
    if groups is None:
        tests = "\n".join('                <test method="manual" sample="true"/>' if i < n_samples else
                          '                <test method="manual"/>' for i in range(n_tests))
        groups_xml = ""
    else:
        meta = {i: ("0", 0.0) for i in range(1, n_samples + 1)}
        for gname, points, members in groups:
            for t in members:
                meta[t] = (gname, points / len(members))
        assert sorted(meta) == list(range(1, n_tests + 1)), "каждый тест должен входить ровно в одну группу"
        sample = ' sample="true"'
        tests = "\n".join(f'                <test method="manual"{sample if i <= n_samples else ""} group="{meta[i][0]}" points="{num(meta[i][1])}"/>'
                          for i in range(1, n_tests + 1))
        groups_xml = ("\n            <groups>\n"
                      '                <group feedback-policy="complete" name="0" points="0.0" points-policy="each-test"/>\n'
                      + "\n".join(f'                <group feedback-policy="points" name="{g}" points="{num(p)}" points-policy="complete-group"/>' for g, p, _ in groups)
                      + "\n            </groups>")
    checker_name = ' name="std::wcmp.cpp"' if checker == "wcmp" else ""
    sols = "\n".join(f'            <solution tag="{tag}">\n                <source path="solutions/{f}" type="{t}"/>\n            </solution>'
                     for f, t, tag in solutions)
    return f"""<?xml version="1.0" encoding="utf-8" standalone="no"?>
<problem revision="1" short-name="{short}" url="">
    <names>
        <name language="russian" value={quoteattr(name)}/>
    </names>
    <statements>
        <statement charset="UTF-8" language="russian" mathjax="true" path="statements/russian/problem.tex" type="application/x-tex"/>
    </statements>
    <judging input-file="" output-file="">
        <testset name="tests">
            <time-limit>{tl}</time-limit>
            <memory-limit>268435456</memory-limit>
            <test-count>{n_tests}</test-count>
            <input-path-pattern>tests/%02d</input-path-pattern>
            <answer-path-pattern>tests/%02d.a</answer-path-pattern>
            <tests>
{tests}
            </tests>{groups_xml}
        </testset>
    </judging>
    <files>
        <resources>
            <file path="files/testlib.h" type="h.g++"/>
        </resources>
    </files>
    <assets>
        <checker{checker_name} type="testlib">
            <source path="files/check.cpp" type="cpp.g++17"/>
            <copy path="check.cpp"/>
        </checker>
        <solutions>
{sols}
        </solutions>
    </assets>
</problem>
"""


def solution_source(task_dir, file):
    src = (EXAM / task_dir / file).read_bytes()
    if file.endswith(".java"):  # тестирующие системы компилируют класс без пакета
        src = re.sub(rb"^package [\w.]+;\r?\n\r?\n?", b"", src, count=1, flags=re.M)
    return src.replace(b"\r\n", b"\n")


def scoring_note(groups):
    parts = [f"группа {g} — {escape(desc)}, {num(p).removesuffix('.0')} балл{'а' if p in (2, 3, 4) else 'ов'}" for g, p, _, desc in groups]
    return (r"\textbf{Система оценки.} Тесты из условия баллов не дают. Остальные тесты разбиты на группы: "
            + "; ".join(parts) + ". Баллы за группу начисляются, только если пройдены все её тесты.")


def build(task_dir, short, tl, checker, solutions, out_path=None, scoring=None):
    """scoring — список (группа, баллы, [номера исходных тестов], описание). Тесты в пакете
    перенумеровываются: примеры, затем группы по порядку."""
    st = statement(task_dir)
    tests_dir = EXAM / task_dir / "tests"
    tests = sorted(p.name for p in tests_dir.iterdir() if p.name.isdigit())
    if not tests:
        raise SystemExit(f"{task_dir}: нет тестов, сначала запустите generate_tests.py")
    n_samples = len(st["examples"])
    for i, (inp, out) in enumerate(st["examples"], 1):  # примеры — первые тесты
        if (tests_dir / f"{i:02d}").read_text() != inp:
            raise SystemExit(f"{task_dir}: тест {i:02d} не совпадает с примером из README")
    check_src = (HERE / ("wcmp.cpp" if checker == "wcmp" else "check_real.cpp")).read_bytes()

    order, groups = list(range(1, len(tests) + 1)), None
    if scoring:
        order = list(range(1, n_samples + 1))
        groups = []
        for g, points, members, _ in scoring:
            groups.append((g, points, list(range(len(order) + 1, len(order) + len(members) + 1))))
            order += members
        if sorted(order) != list(range(1, len(tests) + 1)):
            raise SystemExit(f"{task_dir}: группы должны покрывать все тесты ровно по одному разу")
        if abs(sum(p for _, p, _, _ in scoring) - 10) > 1e-9:
            raise SystemExit(f"{task_dir}: сумма баллов не равна 10")
        st = dict(st, notes=(st["notes"] + "\n\n" if st["notes"] else "") + scoring_note(scoring))

    path = Path(out_path) if out_path else OUT / f"{short}.zip"
    path.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(path, "w", zipfile.ZIP_DEFLATED) as z:
        z.writestr("problem.xml", problem_xml(short, st["name"], tl, len(tests), n_samples, checker, solutions, groups))
        z.writestr("statements/russian/problem.tex", problem_tex(st, tl))
        for sec in ("legend", "input", "output", "notes"):
            z.writestr(f"statement-sections/russian/{sec}.tex", st[sec] + "\n")
        z.writestr("statement-sections/russian/name.tex", escape(st["name"]) + "\n")
        for i, (inp, out) in enumerate(st["examples"], 1):
            for d in ("statements/russian", "statement-sections/russian"):
                z.writestr(f"{d}/example.{i:02d}", inp)
                z.writestr(f"{d}/example.{i:02d}.a", out)
        for new, old in enumerate(order, 1):
            z.write(tests_dir / f"{old:02d}", f"tests/{new:02d}")
            z.write(tests_dir / f"{old:02d}.a", f"tests/{new:02d}.a")
        z.writestr("files/testlib.h", (HERE / "testlib.h").read_bytes())
        z.writestr("files/check.cpp", check_src)
        z.writestr("check.cpp", check_src)
        for f, _, _ in solutions:
            z.writestr(f"solutions/{f}", solution_source(task_dir, f))
    extra = "" if not scoring else ", группы " + ", ".join(f"{g}: {len(m)} тестов / {num(p)}" for g, p, m, _ in scoring)
    print(f"{path.name}: {len(tests)} тестов, примеров {n_samples}, чекер {checker}{extra}")
    return path


if __name__ == "__main__":
    if OUT.exists():
        shutil.rmtree(OUT)
    for task in TASKS:
        build(*task)
    print("Готово:", OUT)
