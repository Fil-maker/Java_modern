"""Генерирует тесты для контеста урока 5.

Для каждой задачи создаётся папка tests/ с файлами:
    01, 02, ...       — входные данные;
    01.a, 02.a, ...   — правильные ответы;
    groups.json       — какие тесты входят в группы оценки 1 и 2.

Ответ — вывод эталонного решения на Java из папки задачи. Каждый ответ
дополнительно сверяется с независимой проверкой на Python из этого файла.
Генерация детерминирована (фиксированный seed).

Нужен JDK 17 или новее: берётся из JAVA_HOME, иначе самый новый из ~/.jdks, иначе из PATH.

Запуск из корня репозитория:
    python src/lesson5/contest/generate_tests.py            — сгенерировать тесты
    python src/lesson5/contest/generate_tests.py --check    — прогнать все решения из TASKS
                                                             (включая неверные) по готовым тестам

ВНИМАНИЕ: тесты содержат ответы.
"""
import json
import os
import random
import re
import subprocess
import sys
import time
from collections import OrderedDict
from pathlib import Path

CONTEST = Path(__file__).resolve().parent
SRC = CONTEST.parent.parent                      # каталог src
CLASSES = SRC.parent / "out" / "lesson5-contest" / "classes"
LONG_MIN, LONG_MAX = -2 ** 63, 2 ** 63 - 1


# ---------------------------------------------------------------- Java
def find_jdk():
    candidates = []
    if os.environ.get("JAVA_HOME"):
        candidates.append(Path(os.environ["JAVA_HOME"]))
    jdks = Path.home() / ".jdks"
    if jdks.is_dir():
        candidates += sorted(jdks.iterdir(), reverse=True)
    for home in candidates:
        if (home / "bin" / ("javac.exe" if os.name == "nt" else "javac")).exists():
            return home / "bin"
    return None


JDK_BIN = find_jdk()


def tool(name):
    return str(JDK_BIN / name) if JDK_BIN else name


def compile_all():
    sources = [str(p) for p in CONTEST.rglob("*.java")]
    CLASSES.mkdir(parents=True, exist_ok=True)
    # --release 17: решения должны собираться компилятором Java 17 тестирующей системы
    subprocess.run([tool("javac"), "--release", "17", "-encoding", "UTF-8", "-d", str(CLASSES)] + sources, check=True)


def java_class(task_dir, file):
    return f"lesson5.contest.{task_dir}.{Path(file).stem}"


def run_java(task_dir, file, inp, timeout=None):
    p = subprocess.run([tool("java"), "-Xss64m", "-cp", str(CLASSES), java_class(task_dir, file)],
                       input=inp.encode(), capture_output=True, timeout=timeout)
    if p.returncode != 0:
        raise RuntimeError(p.stderr.decode(errors="replace")[-2000:])
    return p.stdout.decode().replace("\r\n", "\n")


# ---------------------------------------------------------------- общее
def readme_examples(task_dir):
    text = (CONTEST / task_dir / "README.md").read_text(encoding="utf-8")
    pairs = re.findall(r"Ввод:\s*```\n(.*?)```\s*Вывод:\s*```\n(.*?)```", text, re.S)
    return [(inp, out.strip()) for inp, out in pairs]


def write_tests(task_dir, main, samples, group1, group2):
    """samples, group1, group2 — списки пар (ввод, ожидаемый ответ от независимой проверки)."""
    out_dir = CONTEST / task_dir / "tests"
    out_dir.mkdir(exist_ok=True)
    for old in out_dir.iterdir():
        if re.fullmatch(r"\d\d(\.a)?", old.name) or old.name == "groups.json":
            old.unlink()
    cases = samples + group1 + group2
    for i, (inp, expected) in enumerate(cases, 1):
        answer = run_java(task_dir, main, inp)
        if answer.split() != expected.split():
            raise SystemExit(f"{task_dir} тест {i:02d}: эталон и проверка расходятся")
        (out_dir / f"{i:02d}").write_bytes(inp.encode())
        (out_dir / f"{i:02d}.a").write_bytes(answer.encode())
    s, a = len(samples), len(group1)
    groups = {"1": list(range(s + 1, s + a + 1)), "2": list(range(s + a + 1, len(cases) + 1))}
    (out_dir / "groups.json").write_text(json.dumps(groups) + "\n", encoding="utf-8")
    print(f"{task_dir}: {len(cases)} тестов (примеров {s}, группа 1 — {a}, группа 2 — {len(group2)})")


def lines_input(head, lines):
    return f"{head}\n" + "\n".join(lines) + "\n"


# ---------------------------------------------------------------- A. калькулятор
def calc_eval(line):
    p = line.split(" ")
    if len(p) != 3:
        return "ERROR FORMAT"

    def num(s):
        if not re.fullmatch(r"[+-]?[0-9]+", s):
            return None
        v = int(s)
        return v if LONG_MIN <= v <= LONG_MAX else None

    a, op, b = num(p[0]), p[1], num(p[2])
    if a is None or b is None:
        return "ERROR NUMBER"
    if op not in ("+", "-", "*", "/", "%"):
        return "ERROR OPERATION"
    if op in "/%" and b == 0:
        return "ERROR DIVISION"
    if op == "+":
        r = a + b
    elif op == "-":
        r = a - b
    elif op == "*":
        r = a * b
    else:
        quot = abs(a) // abs(b) * (1 if (a < 0) == (b < 0) else -1)   # к нулю, как в Java
        r = quot if op == "/" else a - b * quot
    return str(r) if LONG_MIN <= r <= LONG_MAX else "ERROR OVERFLOW"


def calc_case(lines):
    assert all(0 < len(x) <= 60 and x == x.strip() and "  " not in x for x in lines)
    return lines_input(len(lines), lines), "\n".join(calc_eval(x) for x in lines)


def rnd_number(big):
    r = random.random()
    if r < 0.1:
        return str(random.choice([0, 1, -1]))
    if r < 0.2:
        return random.choice(["+", "-", ""]) + "0" * random.randint(1, 3) + str(random.randint(0, 999))
    return str(random.randint(-big, big))


def rnd_bad_number():
    return random.choice(["abc", "1.5", "1e5", "0x1F", "--3", "+-3", "12a", "-", "+", "1_000", "(5)", "five",
                          "9223372036854775808", "-9223372036854775809", "99999999999999999999"])


def rnd_expression(big, p_err):
    if random.random() >= p_err:
        return f"{rnd_number(big)} {random.choice('+-*/%')} {rnd_number(big)}"
    kind = random.randint(0, 4)
    if kind == 0:
        k = random.choice([1, 2, 4, 5])
        return " ".join(rnd_number(1000) if i % 2 == 0 else random.choice("+-*/") for i in range(k))
    if kind == 1:
        a, b = rnd_number(big), rnd_bad_number()
        return f"{a} + {b}" if random.random() < 0.5 else f"{b} * {a}"
    if kind == 2:
        return f"{rnd_number(big)} {random.choice(['^', '&', 'x', '//', '**', '+-', 'mod', '?'])} {rnd_number(big)}"
    if kind == 3:
        return f"{rnd_number(big)} {random.choice('/%')} {random.choice(['0', '-0', '+0', '000'])}"
    return f"{rnd_bad_number()} {random.choice(['?', '/'])} 0"


def safe_calc_tests():
    task = "task1_safe_calc"
    M, m = str(LONG_MAX), str(LONG_MIN)
    group1 = [
        calc_case(["2 + 2"]),
        calc_case(["0 / 5", "5 / 1", "-7 / 2", "7 / -2", "-7 / -2", "-7 % 2", "7 % -2", "-7 % -2", "6 % 3", "0 % 7"]),
        calc_case(["1", "1 +", "+ 1 2", "1 + 2 + 3", "1 + 2 3", "x"]),
        calc_case(["abc + 1", "1 + abc", "1.5 * 2", "1e3 - 1", "0x10 + 1", "-- 1 2", "--5 + 1", "1_000 + 1",
                   "+ + +", "- - -", "-5 - -5", "+5 + +5", "-0 * 7", "007 % 4"]),
        calc_case(["3 ^ 2", "3 x 2", "3 // 2", "3 ** 2", "3 mod 2", "3 = 3", "3 \\ 2", "3 +- 2"]),
        calc_case(["1 / 0", "1 % 0", "0 / 0", "-5 % -0", "5 / +0", "abc / 0", "1 ? 0", "a ? 0", "1 + 2 / 0"]),
        calc_case([rnd_expression(10 ** 6, 0.5) for _ in range(300)]),
        calc_case([rnd_expression(1000, 0.7) for _ in range(2000)]),
    ]
    group2 = [
        calc_case([f"{M} + 0", f"{m} + 0", f"{M} + 1", f"{m} - 1", f"{m} + {M}", f"{M} - {m}", f"0 - {m}",
                   f"-{M} - 1", f"{m} * 1", f"{m} * -1", f"{M} * -1", f"{m} / -1", f"{m} % -1", f"{m} / 1",
                   f"{M} * 2", f"{m} / 2", "9223372036854775808 + 0", "-9223372036854775809 - 0"]),
        calc_case(["3037000499 * 3037000499", "3037000500 * 3037000500", "-3037000500 * 3037000500",
                   "4294967296 * 2147483648", "4294967296 * -2147483648", "-4294967296 * -2147483648",
                   "4611686018427387904 + 4611686018427387904", "4611686018427387904 + 4611686018427387903",
                   "-4611686018427387904 - 4611686018427387904", "-4611686018427387904 - 4611686018427387905"]),
        calc_case([rnd_expression(LONG_MAX, 0.2) for _ in range(5000)]),
        calc_case([rnd_expression(random.choice([10 ** 9, 10 ** 12, 3 * 10 ** 9, LONG_MAX]), 0.3)
                   for _ in range(100_000)]),
    ]
    write_tests(task, "SafeCalcTask.java", readme_examples(task), group1, group2)


# ---------------------------------------------------------------- B. банк
def bank_eval(commands):
    bal, out = {}, []
    for c in commands:
        p = c.split()
        cmd = p[0]
        if cmd == "BALANCE":
            out.append(str(bal[p[1]]) if p[1] in bal else "ERROR NO_ACCOUNT")
            continue
        x = int(p[-1])
        ids = p[1:-1]
        if (x < 0) if cmd == "OPEN" else (x <= 0):
            out.append("ERROR AMOUNT")
        elif cmd == "OPEN":
            if ids[0] in bal:
                out.append("ERROR EXISTS")
            else:
                bal[ids[0]] = x
                out.append("OK")
        elif any(i not in bal for i in ids):
            out.append("ERROR NO_ACCOUNT")
        elif cmd == "DEPOSIT":
            bal[ids[0]] += x
            out.append("OK")
        elif bal[ids[0]] < x:
            out.append(f"ERROR FUNDS {x - bal[ids[0]]}")
        else:
            bal[ids[0]] -= x
            if cmd == "TRANSFER":
                bal[ids[1]] += x
            out.append("OK")
    return out


def bank_case(commands):
    return lines_input(len(commands), commands), "\n".join(bank_eval(commands))


def random_bank(q, names, max_x, p_bad=0.1):
    cmds, opened = [], []
    for _ in range(q):
        def amount():
            r = random.random()
            if r < p_bad:
                return random.choice([0, -random.randint(1, max_x)])
            return random.randint(1, max_x)

        def name():
            if opened and random.random() > p_bad:
                return random.choice(opened)
            return random.choice(names)

        kind = random.choices(["OPEN", "DEPOSIT", "WITHDRAW", "TRANSFER", "BALANCE"], [2, 3, 3, 4, 2])[0]
        if kind == "OPEN" or not opened:
            n = random.choice(names)
            cmds.append(f"OPEN {n} {random.choice([0, amount(), random.randint(0, max_x)])}")
            opened.append(n)
        elif kind == "BALANCE":
            cmds.append(f"BALANCE {name()}")
        elif kind == "TRANSFER":
            a = name()
            b = a if random.random() < 0.05 else name()
            cmds.append(f"TRANSFER {a} {b} {amount()}")
        else:
            cmds.append(f"{kind} {name()} {amount()}")
    return cmds


def rnd_name(k=None):
    return "".join(random.choice("abcdefghijklmnopqrstuvwxyz") for _ in range(k or random.randint(1, 10)))


def bank_tests():
    task = "task2_bank"
    T = 10 ** 12
    group1 = [
        bank_case(["BALANCE x"]),
        bank_case(["OPEN a 0", "BALANCE a", "WITHDRAW a 1", "DEPOSIT a 1", "WITHDRAW a 1", "BALANCE a"]),
        bank_case(["OPEN a -1", "OPEN a 5", "OPEN a 7", "DEPOSIT a 0", "WITHDRAW a 0", "TRANSFER a a 0",
                   "DEPOSIT b 0", "DEPOSIT b 5", "BALANCE a"]),
        bank_case(["OPEN a 100", "TRANSFER a b 50", "BALANCE a", "TRANSFER b a 50", "TRANSFER b c 50",
                   "OPEN b 0", "TRANSFER a b 150", "TRANSFER a b 100", "BALANCE a", "BALANCE b"]),
        bank_case(["OPEN a 10", "TRANSFER a a 10", "TRANSFER a a 11", "BALANCE a", "WITHDRAW a 10", "BALANCE a"]),
        bank_case([f"OPEN big {T}", f"DEPOSIT big {T}", f"DEPOSIT big {T}", f"WITHDRAW big {T}", "OPEN small 0",
                   f"TRANSFER small big {T}", f"TRANSFER big small {T}", f"TRANSFER big small {T}",
                   f"TRANSFER big small {T}", "BALANCE big", "BALANCE small", f"DEPOSIT small -{T}"]),
        bank_case(random_bank(200, ["ann", "bob", "cat", "dan", "eve"], 100, 0.15)),
        bank_case(random_bank(2000, [rnd_name() for _ in range(30)], 1000, 0.1)),
    ]
    group2 = [
        bank_case(random_bank(100_000, [rnd_name() for _ in range(50)], T, 0.05)),
        bank_case(random_bank(100_000, [rnd_name() for _ in range(5000)], 10 ** 6, 0.1)),
        bank_case(random_bank(100_000, [rnd_name(10) for _ in range(30000)], T, 0.02)),
        bank_case([f"OPEN a{chr(97 + i % 26)}{rnd_name(5)} {T}" for i in range(1000)]
                  + random_bank(99_000, ["zz", "zy"], T, 0.3)),
    ]
    write_tests(task, "BankTask.java", readme_examples(task), group1, group2)


# ---------------------------------------------------------------- C. стек с минимумом
def stack_eval(kind, commands):
    st, out = [], []
    key = int if kind == "INT" else str
    for c in commands:
        if c.startswith("PUSH "):
            x = c[5:]
            st.append(x)
        elif c == "SIZE":
            out.append(str(len(st)))
        elif not st:
            out.append("EMPTY")
        elif c == "POP":
            out.append(st.pop())
        elif c == "TOP":
            out.append(st[-1])
        elif c == "MIN":
            out.append(min(st, key=key))
        else:
            out.append(max(st, key=key))
    return out


def stack_case(kind, commands, fast=False):
    if fast:   # для больших тестов — та же логика с кэшем минимумов, без O(n) на запрос
        out, st = [], []
        conv = int if kind == "INT" else (lambda s: s)
        for c in commands:
            if c.startswith("PUSH "):
                v = conv(c[5:])
                if st:
                    st.append((c[5:], v, min(v, st[-1][2]), max(v, st[-1][3])))
                else:
                    st.append((c[5:], v, v, v))
            elif c == "SIZE":
                out.append(str(len(st)))
            elif not st:
                out.append("EMPTY")
            elif c in ("POP", "TOP"):
                out.append(st[-1][0])
                if c == "POP":
                    st.pop()
            else:
                out.append(str(st[-1][2 if c == "MIN" else 3]))
        expected = out
    else:
        expected = stack_eval(kind, commands)
    return f"{kind} {len(commands)}\n" + "\n".join(commands) + "\n", "\n".join(expected)


def random_stack(q, values, p_push=0.5):
    cmds = []
    for _ in range(q):
        r = random.random()
        if r < p_push:
            cmds.append(f"PUSH {values()}")
        else:
            cmds.append(random.choice(["POP", "TOP", "MIN", "MAX", "MIN", "MAX", "SIZE"]))
    return cmds


def minmax_stack_tests():
    task = "task3_minmax_stack"
    E = 10 ** 18
    ints = lambda lo, hi: (lambda: random.randint(lo, hi))
    words = lambda k: (lambda: rnd_name(random.randint(1, k)))
    group1 = [
        stack_case("INT", ["POP", "TOP", "MIN", "MAX", "SIZE"]),
        stack_case("STRING", ["PUSH a", "MIN", "MAX", "POP", "MIN", "SIZE"]),
        stack_case("INT", ["PUSH 9", "PUSH 10", "PUSH 100", "PUSH 2", "MIN", "MAX", "POP", "MIN", "MAX"]),
        stack_case("INT", [f"PUSH {E}", f"PUSH -{E}", "PUSH 0", "MIN", "MAX", "POP", "POP", "MIN", "MAX"]),
        stack_case("STRING", ["PUSH b", "PUSH ab", "PUSH abc", "PUSH ba", "PUSH z", "MIN", "MAX", "POP", "POP", "MIN", "MAX"]),
        stack_case("INT", ["PUSH 5", "PUSH 5", "PUSH 5", "MIN", "POP", "MAX", "POP", "POP", "POP", "MIN", "PUSH -1", "TOP"]),
        stack_case("INT", random_stack(300, ints(-20, 20))),
        stack_case("STRING", random_stack(300, words(3))),
        stack_case("INT", random_stack(2000, ints(-E, E), 0.6)),
        stack_case("STRING", random_stack(2000, words(20), 0.6)),
    ]
    group2 = [
        stack_case("INT", random_stack(200_000, ints(-E, E), 0.5), fast=True),
        stack_case("INT", [f"PUSH {random.randint(-E, E)}" for _ in range(100_000)]
                   + random.choices(["MIN", "MAX"], k=99_999) + ["SIZE"], fast=True),
        stack_case("STRING", random_stack(200_000, words(20), 0.55), fast=True),
        stack_case("INT", [f"PUSH {i}" for i in range(1, 100_001)] + ["MIN", "MAX", "POP"] * 33_333 + ["SIZE"], fast=True),
        stack_case("STRING", [f"PUSH {rnd_name(20)}" for _ in range(100_000)]
                   + random.choices(["MIN", "MAX", "POP", "TOP"], [3, 3, 1, 1], k=100_000), fast=True),
    ]
    write_tests(task, "MinMaxStackTask.java", readme_examples(task), group1, group2)


# ---------------------------------------------------------------- D. LRU-кеш
def lru_eval(c, commands):
    cache, out, hits, misses = OrderedDict(), [], 0, 0
    for cmd in commands:
        p = cmd.split()
        k = p[1]
        if p[0] == "GET":
            if k in cache:
                hits += 1
                cache.move_to_end(k)
                out.append(str(cache[k]))
            else:
                misses += 1
                out.append("MISS")
        else:
            res = "OK"
            if k in cache:
                cache.move_to_end(k)
            elif len(cache) == c:
                e, _ = cache.popitem(last=False)
                res = f"EVICT {e}"
            cache[k] = int(p[2])
            out.append(res)
    out.append(f"HITS {hits} MISSES {misses}")
    return out


def lru_case(c, commands):
    return f"{c} {len(commands)}\n" + "\n".join(commands) + "\n", "\n".join(lru_eval(c, commands))


def random_lru(q, keys, p_get=0.5):
    return [f"GET {random.choice(keys)}" if random.random() < p_get
            else f"PUT {random.choice(keys)} {random.randint(-10 ** 9, 10 ** 9)}" for _ in range(q)]


def lru_cache_tests():
    task = "task4_lru_cache"
    group1 = [
        lru_case(1, ["GET a"]),
        lru_case(1, ["PUT a 1", "PUT a 2", "GET a", "PUT b 3", "GET a", "GET b", "PUT a -5", "GET a"]),
        lru_case(3, ["PUT a 1", "PUT b 2", "PUT c 3", "GET a", "GET b", "PUT d 4", "GET c", "PUT e 5", "GET a", "GET b", "GET d", "GET e"]),
        lru_case(2, ["PUT a 1", "PUT b 2", "PUT a 3", "PUT c 4", "GET a", "GET b", "GET c"]),
        lru_case(2, ["PUT x 1000000000", "PUT y -1000000000", "GET x", "GET y", "GET z", "GET z"]),
        lru_case(5, random_lru(300, [rnd_name(1) for _ in range(8)])),
        lru_case(1, random_lru(300, ["a", "b", "c"])),
        lru_case(50, random_lru(2000, [rnd_name() for _ in range(80)], 0.6)),
        lru_case(100_000, random_lru(2000, [rnd_name() for _ in range(500)])),
    ]
    keys_big = [rnd_name(10) for _ in range(150_000)]
    group2 = [
        lru_case(100_000, random_lru(200_000, keys_big[:120_000], 0.5)),
        lru_case(100_000, [f"PUT {k} {i}" for i, k in enumerate(keys_big[:100_000])]
                 + [f"GET {k}" for k in keys_big[:100_000]]),
        lru_case(50_000, random_lru(200_000, keys_big[:60_000], 0.3)),
        lru_case(1000, random_lru(200_000, keys_big[:1500], 0.5)),
        lru_case(99_999, [f"PUT {k} 1" for k in keys_big[:100_000]] + [f"GET {k}" for k in keys_big[:100_000]]),
    ]
    write_tests(task, "LruCacheTask.java", readme_examples(task), group1, group2)


# ---------------------------------------------------------------- проверка всех решений
# каталог, [(файл, ожидаемый результат)]: ok — все тесты, wa — неверный ответ хотя бы на одном, tl — долго
SOLUTIONS = [
    ("task1_safe_calc", [("SafeCalcTask.java", "ok"), ("SafeCalcNoOverflow.java", "wa")]),
    ("task2_bank", [("BankTask.java", "ok"), ("BankNotAtomic.java", "wa")]),
    ("task3_minmax_stack", [("MinMaxStackTask.java", "ok"), ("MinMaxStackAsStrings.java", "wa")]),
    ("task4_lru_cache", [("LruCacheTask.java", "ok"), ("LruCacheNaive.java", "tl")]),
]
TIME_LIMIT = 2.0


def check_solutions():
    ok = True
    for task_dir, sols in SOLUTIONS:
        tests_dir = CONTEST / task_dir / "tests"
        groups = json.loads((tests_dir / "groups.json").read_text(encoding="utf-8"))
        group_of = {t: g for g, ts in groups.items() for t in ts}
        tests = sorted(int(p.name) for p in tests_dir.iterdir() if p.name.isdigit())
        for file, expect in sols:
            failed, slowest = {}, 0.0
            for t in tests:
                inp = (tests_dir / f"{t:02d}").read_text()
                ans = (tests_dir / f"{t:02d}.a").read_text()
                start = time.perf_counter()
                try:
                    got = run_java(task_dir, file, inp, timeout=TIME_LIMIT * 3)
                    verdict = "ok" if got.split() == ans.split() else "wa"
                except subprocess.TimeoutExpired:
                    verdict = "tl"
                spent = time.perf_counter() - start
                slowest = max(slowest, spent)
                if verdict == "ok" and spent > TIME_LIMIT:
                    verdict = "tl"
                if verdict != "ok":
                    failed[t] = verdict
            groups_lost = sorted({group_of.get(t, "0") for t in failed})
            got_kind = "ok" if not failed else ("tl" if "tl" in failed.values() else "wa")
            mark = "✓" if got_kind == expect else "✗"
            ok &= got_kind == expect
            detail = "все тесты" if not failed else f"не прошли {len(failed)} тестов ({', '.join(sorted(set(failed.values())))}), группы {', '.join(groups_lost)}"
            print(f"{mark} {task_dir}/{file}: ожидалось {expect}, {detail}; максимум {slowest:.2f} с")
    if not ok:
        raise SystemExit("Есть решения с неожиданным результатом")


TASKS = [safe_calc_tests, bank_tests, minmax_stack_tests, lru_cache_tests]

if __name__ == "__main__":
    sys.stdout.reconfigure(encoding="utf-8")
    compile_all()
    if "--check" in sys.argv:
        check_solutions()
    else:
        for i, make in enumerate(TASKS):
            random.seed(20261117 + i)
            make()
