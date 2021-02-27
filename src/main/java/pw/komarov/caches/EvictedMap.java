package pw.komarov.caches;

import java.util.Map;

public interface EvictedMap<K,V> extends Map<K,V> {
    default void evict(int count) {
        for(int i = 1; i <= count; i++) {
            evictItem();
        }
    }

    void evictItem();
}
