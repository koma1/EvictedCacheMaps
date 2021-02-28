import org.junit.jupiter.api.Test;
import pw.komarov.caches.EvictedCache;
import pw.komarov.caches.EvictedCacheLFU;
import pw.komarov.caches.EvictedCacheLRU;

import static org.junit.jupiter.api.Assertions.*;

class LFULRUTest {
    @Test
    void lfuTest() {
        EvictedCache<Integer,Integer> cache = new EvictedCacheLFU<>(10);
        cache.put(1,1); //accessed 1 times
        cache.put(2,4); //accessed 0 times (must be evicted first)
        cache.put(3,6); //accessed 2 times

        cache.get(3);
        cache.get(1);
        cache.get(3);

        assertEquals(cache.size(), 3);
        assertTrue(cache.containsKey(2));

        cache.evict();

        assertEquals(cache.size(), 2);
        assertFalse(cache.containsKey(2));

        cache.evict();
        assertEquals(cache.size(), 1);
        assertFalse(cache.containsKey(1));

        cache.evict();
        assertEquals(cache.size(), 0);
    }

    @Test
    void lruTest() throws InterruptedException {
        EvictedCache<Integer,Integer> cache = new EvictedCacheLRU<>(10);
        cache.put(1,1); //accessed last (newly) (must be evicted third)
        cache.put(2,4); //(must be evicted second)
        cache.put(3,6); //accessed first (oldest) (must be evicted first)

        cache.get(3);
        Thread.sleep(100);
        cache.get(2);
        Thread.sleep(100);
        cache.get(1);
        Thread.sleep(100);

        assertEquals(cache.size(), 3);
        assertTrue(cache.containsKey(3));

        cache.evict();

        assertEquals(cache.size(), 2);
        assertFalse(cache.containsKey(3));

        cache.evict();
        assertEquals(cache.size(), 1);
        assertFalse(cache.containsKey(2));

        cache.evict();
        assertEquals(cache.size(), 0);
    }
}
