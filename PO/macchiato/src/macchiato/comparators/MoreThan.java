package macchiato.comparators;

public class MoreThan extends Comparator {
    @Override
    protected String symbol() {
        return ">";
    }

    @Override
    public boolean compare(int left, int right) {
        return left > right;
    }
}
