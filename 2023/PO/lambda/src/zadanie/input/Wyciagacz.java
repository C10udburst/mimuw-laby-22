package zadanie.input;

@FunctionalInterface
public interface Wyciagacz<O, W> {
    W wyciągnij(O o);
}
