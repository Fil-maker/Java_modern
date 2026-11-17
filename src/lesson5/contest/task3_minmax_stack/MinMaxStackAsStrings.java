package lesson5.contest.task3_minmax_stack;

import java.io.*;
import java.util.*;

// Неверное решение для проверки тестов: всегда хранит элементы как строки,
// поэтому для INT сравнивает "10" и "9" лексикографически.
public class MinMaxStackAsStrings {

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int q = Integer.parseInt(in.readLine().split(" ")[1]);
        ArrayList<String[]> st = new ArrayList<>();   // {value, min, max}
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < q; i++) {
            String line = in.readLine();
            if (line.startsWith("PUSH ")) {
                String x = line.substring(5);
                if (st.isEmpty()) {
                    st.add(new String[]{x, x, x});
                } else {
                    String[] t = st.get(st.size() - 1);
                    st.add(new String[]{x, x.compareTo(t[1]) < 0 ? x : t[1], x.compareTo(t[2]) > 0 ? x : t[2]});
                }
                continue;
            }
            if (line.equals("SIZE")) {
                out.append(st.size()).append('\n');
                continue;
            }
            if (st.isEmpty()) {
                out.append("EMPTY\n");
                continue;
            }
            String[] t = st.get(st.size() - 1);
            switch (line) {
                case "POP": st.remove(st.size() - 1); out.append(t[0]); break;
                case "TOP": out.append(t[0]); break;
                case "MIN": out.append(t[1]); break;
                default: out.append(t[2]);
            }
            out.append('\n');
        }
        System.out.print(out);
    }
}
