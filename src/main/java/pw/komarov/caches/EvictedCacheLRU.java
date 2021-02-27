package pw.komarov.caches;

public class EvictedCacheLRU<K, V> extends EvictedCache<K,V> {
    private static class ComparatorLRU implements EvictionComparator {
        @Override
        public int compare(EvictedCache.CacheEntry right, EvictedCache.CacheEntry left) {
            return Long.compare(right.getAccessedAt(), left.getAccessedAt());
        }
    }

    public EvictedCacheLRU(int initialCapacity) {
        super(initialCapacity, new ComparatorLRU());
    }
}
