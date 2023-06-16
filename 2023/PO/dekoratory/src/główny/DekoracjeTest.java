package główny;

import dekoracje.DekoratorNumerujący;
import dekoracje.DekoratorOdwracający;
import dekoracje.DekoratorZamieniający;
import dekoracje.WirtualnyPlik;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

@Execution(SAME_THREAD) // używamy pliku, więc lepiej nie wykonywać testów równolegle
public class DekoracjeTest {

    private static final String TEST = """
Colorless green ideas sleep furiously.

	Hued ideas mock the brain,
	Notions of color not yet color,
		Of pure, touchless, branching pallor
		Of invading, essential Green
			Tortured my mind's eye at its small peephole
			sees through the virid glass
				the endless ghostly oscillographic stream
				Furiously sleep ideas green colorless
					Madly awake am I at my small window
""".strip();

    @BeforeAll
    public static void generujPlik() throws IOException {
        File file = new File("test.txt");
        if (file.exists()) {
            if (!file.delete()) throw new IOException("Nie udało się usunąć pliku");
        }
        file.createNewFile();
        FileWriter writer = new FileWriter(file);
        writer.write(TEST);
        writer.close();
        file.deleteOnExit();
    }

    @Test
    public void tylkoNumeracja() {
        WirtualnyPlik plik = new WirtualnyPlik();
        DekoratorNumerujący dekorator = new DekoratorNumerujący(plik);
        dekorator.wypiszZPliku("test.txt");
        String[] linie = plik.pobierzZawartość().split("\n");
        assertEquals("1: Colorless green ideas sleep furiously.", linie[0]);
        assertEquals("2: ", linie[1]);
        for (int i = 2; i < linie.length; i++) {
            assertTrue(linie[i].startsWith(i + 1 + ": "), "Błąd w linii: " + linie[i]);
        }
        assertEquals("11: \t\t\t\t\tMadly awake am I at my small window", linie[10]);
        System.out.println(plik.pobierzZawartość());
    }

    @Test
    public void tylkoOdwracanie() {
        WirtualnyPlik plik = new WirtualnyPlik();
        DekoratorOdwracający dekorator = new DekoratorOdwracający(plik);
        dekorator.wypiszZPliku("test.txt");
        String[] linie = plik.pobierzZawartość().split("\n");
        assertEquals(".ylsuoiruf peels saedi neerg sselroloC", linie[0]);
        assertEquals("", linie[1]);
        assertEquals("wodniw llams ym ta I ma ekawa yldaM\t\t\t\t\t", linie[10]);
        System.out.println(plik.pobierzZawartość());
    }

    @Test
    public void tylkoZamiana() {
        String znak = "-";
        WirtualnyPlik plik = new WirtualnyPlik();
        DekoratorZamieniający dekorator = new DekoratorZamieniający(plik, znak);
        dekorator.wypiszZPliku("test.txt");
        String[] linie = plik.pobierzZawartość().split("\n");
        assertEquals("Colorless green ideas sleep furiously.", linie[0]);
        assertEquals("", linie[1]);
        for (int i = 2; i < linie.length; i++) {
            String count = znak.repeat(i/2);
            assertTrue(linie[i].startsWith(count), "Błąd w linii: " + linie[i]);
        }
        assertEquals(znak.repeat(5) + "Madly awake am I at my small window", linie[10]);
        System.out.println(plik.pobierzZawartość());
    }

    @Test
    public void zamianaNumeracjaOdwracanie() {
        String znak = "*";
        WirtualnyPlik plik = new WirtualnyPlik();
        DekoratorZamieniający dekoratorZamieniający = new DekoratorZamieniający(plik, znak);
        DekoratorNumerujący dekoratorNumerujący = new DekoratorNumerujący(dekoratorZamieniający);
        DekoratorOdwracający dekoratorOdwracający = new DekoratorOdwracający(dekoratorNumerujący);
        dekoratorOdwracający.wypiszZPliku("test.txt");

        String[] linie = plik.pobierzZawartość().split("\n");

        for (int i = 0; i < linie.length; i++) {
            String count = znak.repeat(i/2);
            assertTrue(linie[i].startsWith(i + 1 + ": "), "Błąd w linii: " + linie[i]);
            assertTrue(linie[i].endsWith(count), "Błąd w linii: " + linie[i]);
        }

        assertEquals("11: wodniw llams ym ta I ma ekawa yldaM*****", linie[10]);

        System.out.println(plik.pobierzZawartość());
    }
}
