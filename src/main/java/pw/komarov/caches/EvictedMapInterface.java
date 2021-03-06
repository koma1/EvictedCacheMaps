package pw.komarov.caches;

import java.util.Map;

public interface EvictedMapInterface<K,V> extends Map<K,V> {
    default void evict(int count) {
        for(int i = 1; i <= count; i++) {
            evict();
        }
    }

    void evict();
}
