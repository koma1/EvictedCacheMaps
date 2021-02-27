package pw.komarov.caches;

import java.util.Comparator;

@FunctionalInterface
public interface EvictionComparator extends Comparator<EvictedCache.CacheEntry> {
    int compare(EvictedCache.CacheEntry right, EvictedCache.CacheEntry left);
}