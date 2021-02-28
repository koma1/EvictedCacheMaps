package pw.komarov.caches;

import java.util.Comparator;

@FunctionalInterface
public interface EvictionComparator<K> extends Comparator<EvictedMap.CacheEntry<K>> {
    int compare(EvictedMap.CacheEntry<K> right, EvictedMap.CacheEntry<K> left);
}