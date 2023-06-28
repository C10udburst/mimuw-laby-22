package zadanie.input;

import zadanie.Samochod;

@FunctionalInterface
public interface IntWyciagacz {
    int wyciagnij(Samochod s);
}