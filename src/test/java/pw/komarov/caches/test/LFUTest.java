package pw.komarov.caches.test;

import org.junit.jupiter.api.Test;
import pw.komarov.caches.EvictedMap;
import pw.komarov.caches.EvictedMapLFU;

import static org.junit.jupiter.api.Assertions.*;

class LFUTest {
    @Test
    void lfuTest() {
        EvictedMap<Integer,Integer> cache = new EvictedMapLFU<>(10);
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
}
