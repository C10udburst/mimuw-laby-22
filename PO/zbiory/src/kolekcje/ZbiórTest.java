package kolekcje;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ZbiórTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testToString() {
        String[] tab = new String[]{"1", "2"};
        ArrayList<String> dane = new ArrayList<>();
        for (String s: tab)
            dane.add(s);
        Zbiór<String> zbs1 = new Zbiór<>(dane);
        assertEquals("Zbiór{1,2}", zbs1.toString());
        Zbiór<String> zbs2 = new Zbiór<>();
        assertEquals("Zbiór{}", zbs2.toString());
    }

    @Test
    void czyPusty() {
        Zbiór<String> zb1 = new Zbiór<>();
        assertTrue(zb1.czyPusty());
        Zbiór<String> zb2 = new Zbiór<>(new ArrayList<>());
        assertTrue(zb2.czyPusty());
    }

    @Test
    void rozmiar() {
        String[] tab = new String[]{"1", "2"};
        ArrayList<String> dane = new ArrayList<>();
        for (String s: tab)
            dane.add(s);
        Zbiór<String> zbs1 = new Zbiór<>(dane);
        assertTrue(zbs1.rozmiar() == tab.length);
        Zbiór<String> zbs2 = new Zbiór<>();
        assertTrue(zbs2.rozmiar() == 0);
    }

    @Test
    void wstaw() {
        String[] tab = new String[]{"1", "2", "1",
                                    "ala", "ola", "ala" };
        Zbiór<String> zb = new Zbiór<>();
        for (String s: tab)
            zb.wstaw(s);

        assertTrue(zb.rozmiar()==4);

        for (String s: tab){
            assertTrue(zb.czyIstnieje(s));
            assertTrue(!zb.czyIstnieje(s+"#"));
        }

    }

    @Test
    void czyIstnieje() {
        String[] tab = new String[]{"1", "2", "1",
                "ala", "ola", "ala" };
        Zbiór<String> zb = new Zbiór<>();
        for (String s: tab)
            zb.wstaw(s);

        assertTrue(zb.rozmiar()==4);

        for (String s: tab){
            assertTrue(zb.czyIstnieje(s));
            assertTrue(!zb.czyIstnieje(s+"#"));
        }
    }

    @Test
    void różnica() {
        String[] tab1 = new String[]{"1", "2", "1",
                "ala", "ola", "ala" };
        String[] tab2 = new String[]{
                "ala", "ola", "ala", "ola" };
        Zbiór<String> zb1 = new Zbiór<>();
        Zbiór<String> zb2 = new Zbiór<>();
        for (String s: tab1)
            zb1.wstaw(s);
        for (String s: tab2)
            zb2.wstaw(s);

        zb1.różnica(zb2);

        assertEquals(2, zb1.rozmiar());
        assertTrue(zb1.czyIstnieje("1"));
        assertTrue(zb1.czyIstnieje("2"));
        assertFalse(zb1.czyIstnieje("ala"));
        assertFalse(zb1.czyIstnieje("ola"));
        assertFalse(zb1.czyIstnieje("ala#"));
    }

    @Test
    void equals() {
        String[] tab1 = new String[]{"1", "2", "1",
                "ala", "ola", "ala" };
        String[] tab2 = new String[]{
                "ala", "ola", "ala", "ola" };
        String[] tab3 = new String[]{
                "ala", "ola", "ala", "ola" };
        Zbiór<String> zb1 = new Zbiór<>();
        Zbiór<String> zb2 = new Zbiór<>();
        Zbiór<String> zb3 = new Zbiór<>();
        for (String s: tab1)
            zb1.wstaw(s);
        for (String s: tab2)
            zb2.wstaw(s);
        for (String s: tab3)
            zb3.wstaw(s);

        assertNotEquals(zb1, zb2);
        assertEquals(zb2, zb2);
        assertEquals(zb2, zb3);
        assertEquals(zb3, zb2);
    }
}