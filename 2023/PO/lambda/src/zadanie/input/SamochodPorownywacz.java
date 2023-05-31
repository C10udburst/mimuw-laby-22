package zadanie.input;

import zadanie.Samochod;

@FunctionalInterface
public interface SamochodPorownywacz {
    int compare(Samochod a, Samochod b);
}