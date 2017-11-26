import com.bigsort.util.IndexMinPQ;
import org.junit.Before;
import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class IndexMinPQTest {
    protected String[] strings;
    protected IndexMinPQ<String> idxMinPQ;
    protected String[] equalStrings;

    @Before
    public void setUp() {
        strings = new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i", "j"};
        equalStrings = new String[]{"b", "b", "b", "b", "b", "b", "b", "b", "b", "b"};
        idxMinPQ = new IndexMinPQ<String>(strings.length);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testIndexMinPQ() {
        for (int i = 0; i > -3; --i) {
            idxMinPQ = new IndexMinPQ<String>(i);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertNegativeIndex() {
        idxMinPQ.insert(-1, strings[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertTooBigIndex() {
        idxMinPQ.insert(strings.length + 1, strings[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertDuplicate() {
        idxMinPQ.insert(1, strings[0]);
        idxMinPQ.insert(1, strings[0]);
    }

    @Test
    public void testIsEmpty() {
        assertTrue(idxMinPQ.isEmpty());
        idxMinPQ.insert(1, strings[0]);
        assertFalse(idxMinPQ.isEmpty());
        idxMinPQ.delMin();
        assertEquals(0, idxMinPQ.size());

    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testContainsExceptionNegative() {
        idxMinPQ.contains(-1);
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testContainsExceptionOverflow() {
        idxMinPQ.contains(1000);
    }

    @Test
    public void testContains() {
        assertFalse(idxMinPQ.contains(0));
        idxMinPQ.insert(0, strings[0]);
        assertTrue(idxMinPQ.contains(0));
        idxMinPQ.delMin();
        assertFalse(idxMinPQ.contains(0));
    }

    @Test
    public void testSize() {
        assertEquals(0, idxMinPQ.size());
        for (int i = 0; i < strings.length; ++i) {
            idxMinPQ.insert(i, strings[i]);
            assertEquals(i + 1, idxMinPQ.size());
        }
        for (int i = strings.length - 1; i >= 0; i--) {
            idxMinPQ.delMin();
            assertEquals(i, idxMinPQ.size());
        }
    }

    @Test
    public void testMinKeyDifferent() {
        for (int i = strings.length - 1; i >= 0; i--) {
            idxMinPQ.insert(i, strings[i]);
            assertEquals(strings[i], idxMinPQ.minKey());
        }
    }

    @Test
    public void testMinKeyEqual() {
        for (int i = equalStrings.length - 1; i > 0; i--) { // one vacant place
            idxMinPQ.insert(i, equalStrings[i]);
            assertEquals(equalStrings[0], idxMinPQ.minKey());
        }

        idxMinPQ.insert(0, "a");
        assertEquals("a", idxMinPQ.minKey());
        idxMinPQ.delMin();
        assertEquals(equalStrings[0], idxMinPQ.minKey());
    }

    @Test
    public void testDelMin() {
        for (int i = strings.length - 1; i >= 0; i--) {
            idxMinPQ.insert(i, strings[i]);
            assertEquals(strings[i], idxMinPQ.minKey());
            assertEquals(i, idxMinPQ.delMin());
        }
        assertTrue(idxMinPQ.isEmpty());
    }


    @Test(expected = NoSuchElementException.class)
    public void testMinKeyAfterManipulation() {
        for (int i = strings.length - 1; i >= 0; i--) {
            idxMinPQ.insert(i, strings[i]);
        }
        for (int i = strings.length - 1; i >= 0; i--) {
            idxMinPQ.delMin();
        }
        idxMinPQ.minKey();
    }

    @Test(expected = NoSuchElementException.class)
    public void testMinKeyExceptionEmpty() {
        idxMinPQ.minKey();
    }

    @Test(expected = NoSuchElementException.class)
    public void testDelMinExceptionAfterManipulation() {
        for (int i = strings.length - 1; i >= 0; i--) {
            idxMinPQ.insert(i, strings[i]);
            assertEquals(strings[i], idxMinPQ.minKey());
            assertEquals(i, idxMinPQ.delMin());
        }
        idxMinPQ.delMin();
    }

    @Test(expected = NoSuchElementException.class)
    public void testDelMinExceptionEmpty() {
        idxMinPQ.delMin();
    }

}
