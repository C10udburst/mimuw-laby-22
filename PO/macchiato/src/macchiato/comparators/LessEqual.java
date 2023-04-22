package macchiato.comparators;

public class LessEqual extends Comparator {
    @Override
    protected String symbol() {
        return "<=";
    }

    @Override
    public boolean compare(int left, int right) {
        return left <= right;
    }
}
