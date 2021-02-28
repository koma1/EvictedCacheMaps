package pw.komarov.caches;

import java.util.Comparator;

@FunctionalInterface
public interface EvictionComparator extends Comparator<EvictedMap.CacheEntry> {
    int compare(EvictedMap.CacheEntry right, EvictedMap.CacheEntry left);
}