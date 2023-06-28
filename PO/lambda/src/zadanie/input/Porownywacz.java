package zadanie.input;

@FunctionalInterface
public interface Porownywacz<O> {
    int compare(O o1, O o2);
}
