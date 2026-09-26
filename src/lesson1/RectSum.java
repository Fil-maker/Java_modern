package lesson1;

/**
 * Статические суммы в прямоугольнике через 2D префиксные суммы.
 *
 * Построение: O(n * m)
 * Запрос:     O(1)
 * Память:     O(n * m)
 *
 * Индексация: 0-based, границы [x1..x2] и [y1..y2] включительно.
 * Внутри хранится массив (n+1) x (m+1) — так формулы чище.
 */
public class RectSum {

    private final int n;          // число строк
    private final int m;          // число столбцов
    private final long[][] pref;  // размер (n+1) x (m+1); нулевые строка/столбец — «бортик»

    /** Строим структуру по матрице a (n x m). */
    public RectSum(long[][] a) {
        this.n = a.length;
        this.m = a[0].length;
        this.pref = new long[n + 1][m + 1];

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                pref[i][j] = a[i - 1][j - 1]
                        + pref[i - 1][j]
                        + pref[i][j - 1]
                        - pref[i - 1][j - 1];
            }
        }
    }

    /**
     * Сумма элементов матрицы на прямоугольнике
     *   x ∈ [x1..x2], y ∈ [y1..y2]  (включительно, 0-based).
     */
    public long query(int x1, int y1, int x2, int y2) {
        if (x1 > x2) { int t = x1; x1 = x2; x2 = t; }
        if (y1 > y2) { int t = y1; y1 = y2; y2 = t; }

        return pref[x2 + 1][y2 + 1]
                - pref[x1][y2 + 1]
                - pref[x2 + 1][y1]
                + pref[x1][y1];
    }

    /** Сумма всей матрицы. */
    public long total() {
        return pref[n][m];
    }

    /** Размеры матрицы — удобно для отладки и тестов. */
    public int rows() { return n; }
    public int cols() { return m; }

    // ======================================================================
    //                          ДЕМОНСТРАЦИЯ
    // ======================================================================

    public static void main(String[] args) {
        // ---- 1. Исходная матрица 4 x 5 --------------------------------------
        long[][] a = {
                {  1,  2,  3,  4,  5 },
                {  6,  7,  8,  9, 10 },
                { 11, 12, 13, 14, 15 },
                { 16, 17, 18, 19, 20 }
        };

        System.out.println("Исходная матрица 4 x 5:");
        printMatrix(a);

        RectSum rs = new RectSum(a);

        // ---- 2. Сумма всей матрицы -----------------------------------------
        // Ожидание: (1+20)*20/2 = 210
        System.out.println("\n[1] Сумма всей матрицы: " + rs.total());
        System.out.println("    Ожидание: 210");

        // ---- 3. Сумма одного элемента ---------------------------------------
        // a[2][3] = 14
        System.out.println("\n[2] a[2][3] = " + rs.query(2, 3, 2, 3));
        System.out.println("    Ожидание: 14");

        // ---- 4. Сумма строки -------------------------------------------------
        // Строка 1: 6+7+8+9+10 = 40
        System.out.println("\n[3] Сумма строки 1 (весь диапазон столбцов): "
                + rs.query(1, 0, 1, 4));
        System.out.println("    Ожидание: 40");

        // ---- 5. Сумма столбца ------------------------------------------------
        // Столбец 2: 3+8+13+18 = 42
        System.out.println("\n[4] Сумма столбца 2 (весь диапазон строк): "
                + rs.query(0, 2, 3, 2));
        System.out.println("    Ожидание: 42");

        // ---- 6. Сумма центрального прямоугольника ---------------------------
        // [1..2] x [1..3]: 7 + 8 + 9 + 12 + 13 + 14 = 63
        System.out.println("\n[5] Сумма прямоугольника [1..2] x [1..3]: "
                + rs.query(1, 1, 2, 3));
        System.out.println("    Ожидание: 63");

        // ---- 7. Сумма верхнего левого угла 2 x 2 ----------------------------
        // 1 + 2 + 6 + 7 = 16
        System.out.println("\n[6] Сумма угла [0..1] x [0..1]: "
                + rs.query(0, 0, 1, 1));
        System.out.println("    Ожидание: 16");

        // ---- 8. Сумма правого нижнего угла 2 x 2 ----------------------------
        // 14 + 15 + 19 + 20 = 68
        System.out.println("\n[7] Сумма угла [2..3] x [3..4]: "
                + rs.query(2, 3, 3, 4));
        System.out.println("    Ожидание: 68");

        // ---- 9. Проверка согласованности: сумма кусков = сумма целого ------
        long part1 = rs.query(0, 0, 1, 1); // 16
        long part2 = rs.query(0, 2, 1, 4); // 3+4+5+8+9+10 = 39
        long part3 = rs.query(2, 0, 3, 4); // 11..20 = 155
        System.out.println("\n[8] Разбили матрицу на 3 части:");
        System.out.println("    [0..1]x[0..1] = " + part1);
        System.out.println("    [0..1]x[2..4] = " + part2);
        System.out.println("    [2..3]x[0..4] = " + part3);
        System.out.println("    Сумма частей = " + (part1 + part2 + part3));
        System.out.println("    total()      = " + rs.total());
        System.out.println("    Совпадает?   = " + (part1 + part2 + part3 == rs.total()));

        // ---- 10. Обратный порядок границ (защита от перепутывания) ---------
        // Должно вернуть то же, что и query(1, 1, 2, 3) = 63
        System.out.println("\n[9] query(2, 3, 1, 1) (границы наоборот): "
                + rs.query(2, 3, 1, 1));
        System.out.println("    Ожидание: 63");
    }

    // ---- Вспомогательный метод: красивый вывод матрицы ---------------------
    private static void printMatrix(long[][] a) {
        for (long[] row : a) {
            StringBuilder sb = new StringBuilder("  ");
            for (long v : row) {
                sb.append(String.format("%4d", v));
            }
            System.out.println(sb);
        }
    }
}