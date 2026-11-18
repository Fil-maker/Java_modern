package lesson5.contest.task4_lru_cache;

import java.io.*;
import java.util.*;

// Медленное решение для проверки тестов: ответы верные, но порядок обращений
// хранится в ArrayList, и каждое обращение ищет ключ перебором — O(c) на команду.
public class LruCacheNaive {

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer head = new StringTokenizer(in.readLine());
        int c = Integer.parseInt(head.nextToken());
        int q = Integer.parseInt(head.nextToken());
        ArrayList<String> order = new ArrayList<>();   // от давнего к свежему
        HashMap<String, Integer> values = new HashMap<>();
        int hits = 0, misses = 0;
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < q; i++) {
            String[] p = in.readLine().split(" ");
            String k = p[1];
            if (p[0].equals("GET")) {
                if (values.containsKey(k)) {
                    hits++;
                    order.remove(k);
                    order.add(k);
                    out.append(values.get(k));
                } else {
                    misses++;
                    out.append("MISS");
                }
            } else {
                String res = "OK";
                if (values.containsKey(k)) {
                    order.remove(k);
                } else if (values.size() == c) {
                    String e = order.remove(0);
                    values.remove(e);
                    res = "EVICT " + e;
                }
                order.add(k);
                values.put(k, Integer.parseInt(p[2]));
                out.append(res);
            }
            out.append('\n');
        }
        out.append("HITS ").append(hits).append(" MISSES ").append(misses).append('\n');
        System.out.print(out);
    }
}
