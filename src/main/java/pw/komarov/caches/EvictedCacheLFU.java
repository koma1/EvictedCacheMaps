package pw.komarov.caches;

public class EvictedCacheLFU<K, V> extends EvictedCache<K,V> {
    private static class ComparatorLFU implements EvictionComparator {
        @Override
        public int compare(EvictedCache.CacheEntry right, EvictedCache.CacheEntry left) {
            return right.getAccessedCount() - left.getAccessedCount();
        }
    }

    public EvictedCacheLFU(int initialCapacity) {
        super(initialCapacity, new ComparatorLFU());
    }
}
