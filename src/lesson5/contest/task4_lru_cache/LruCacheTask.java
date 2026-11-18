package lesson5.contest.task4_lru_cache;

import java.io.*;
import java.util.*;

public class LruCacheTask {

    static class LruCache<K, V> {
        private final int capacity;
        private final LinkedHashMap<K, V> map;   // порядок обращений: первый — самый давний
        private int hits, misses;

        LruCache(int capacity) {
            if (capacity <= 0) throw new IllegalArgumentException("ёмкость должна быть > 0");
            this.capacity = capacity;
            this.map = new LinkedHashMap<>(capacity, 0.75f, true);
        }

        Optional<V> get(K key) {
            V v = map.get(key);           // get делает ключ самым свежим
            if (v == null) misses++;
            else hits++;
            return Optional.ofNullable(v);
        }

        // возвращает вытесненный ключ, если он был
        Optional<K> put(K key, V value) {
            Optional<K> evicted = Optional.empty();
            if (!map.containsKey(key) && map.size() == capacity) {
                K eldest = map.keySet().iterator().next();
                map.remove(eldest);
                evicted = Optional.of(eldest);
            }
            map.put(key, value);          // и put делает ключ самым свежим
            return evicted;
        }

        int hits() { return hits; }
        int misses() { return misses; }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer head = new StringTokenizer(in.readLine());
        int c = Integer.parseInt(head.nextToken());
        int q = Integer.parseInt(head.nextToken());
        LruCache<String, Integer> cache = new LruCache<>(c);
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < q; i++) {
            String[] p = in.readLine().split(" ");
            if (p[0].equals("GET")) {
                out.append(cache.get(p[1]).map(String::valueOf).orElse("MISS"));
            } else {
                out.append(cache.put(p[1], Integer.parseInt(p[2])).map(e -> "EVICT " + e).orElse("OK"));
            }
            out.append('\n');
        }
        out.append("HITS ").append(cache.hits()).append(" MISSES ").append(cache.misses()).append('\n');
        System.out.print(out);
    }
}
