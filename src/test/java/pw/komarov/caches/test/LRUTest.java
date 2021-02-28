package pw.komarov.caches.test;

import org.junit.jupiter.api.Test;
import pw.komarov.caches.EvictedMap;
import pw.komarov.caches.EvictedMapLRU;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LRUTest {
    @Test
    void lruTest() throws InterruptedException {
        EvictedMap<Integer,Integer> cache = new EvictedMapLRU<>(10);
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
