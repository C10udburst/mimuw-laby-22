package pojemniki;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StosTest {

    @Test
    void testToString() {
        try {
            final int rozmiar = 13;
            Stos st = new Stos(rozmiar);
            int[] tstTbl = new int[]{-3, 12, 0, 1234, -3, -3};
            String tstStr = "Stos(" + rozmiar + ";" + tstTbl.length + ": ";
            for (int tstElt : tstTbl) {
                st.wstaw(tstElt);
                tstStr += tstElt + ",";
            }
            tstStr = tstStr.substring(0, tstStr.length() - 1); // ostatni przecinek
            tstStr += ")";
            // System.out.println(st.toString());
            // System.out.println(tstStr);
            assertTrue(st.toString().equals(tstStr));
        } catch (Exception e) {
            throw new RuntimeException(e); // to się nie może zdarzyć
        }

    }

    @Test
    void pobierz() {
        try {
            final int rozmiar = 13;
            Stos st = new Stos(rozmiar);
            int[] tstTbl = new int[]{-3, 12, 0, 1234, -3, -3};
            for (int tstElt : tstTbl) {
                st.wstaw(tstElt);
            }
            int[] wyniki = new int[tstTbl.length];
            int i = tstTbl.length - 1;
            while (!st.pusty()) {
                wyniki[i--] = st.pobierz();
                // nie muszę sprawdzać, czy jestem w tablicy, bo to test
            }
            assertArrayEquals(tstTbl, wyniki);
        } catch (Exception e) {
            fail(); // to się nie może zdarzyć
        }

    }

    @Test
    void wstaw() {
        try {
            Stos st = new Stos(13);
            final int tstElt = -76;
            st.wstaw(tstElt);
            int pom = st.pobierz();
            assertTrue(pom == tstElt);
        } catch (Exception e) {
            throw new RuntimeException(e); // to się nie może zdarzyć
        }

    }

    @Test
    void pusty() {
        try {
            Stos st = new Stos(13);
            assertTrue(st.pusty());
            st.wstaw(0);
            assertFalse(st.pusty());
        } catch (Exception e) {
            throw new RuntimeException(e); // to się nie może zdarzyć
        }

    }

    @Test
    void wyjątek() {
        Exception wyj = null;
        try {
            Stos st = new Stos(-1);
        }
        catch(Exception e){
            wyj = e;
        }
        assertTrue(wyj != null && wyj.getClass() == ZłyRozmiar.class);

        wyj = null;
        try {
            Stos st = new Stos(10);
            st.pobierz();
        }
        catch(Exception e){
            wyj = e;
        }
        assertTrue(wyj != null &&
                wyj.getClass().getSimpleName().equals("PustyPojemnik"));
                // wyj.getClass() == PustyPojemnik.class);

        wyj = null;
        try {
            Stos st = new Stos(2);
            st.wstaw(1);
            st.wstaw(1);
            st.wstaw(1);
        }
        catch(Exception e){
            wyj = e;
        }
        assertTrue(wyj != null && wyj.getClass() == BrakMiejsca.class);

    }

}