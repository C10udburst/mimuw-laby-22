package główny;

import bst.BST;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello BST world!");
        BST<Integer> bi = new BST<>();
        BST<String> bs = new BST<String>();

        // uzupełnić kodem testującym dla bs
        // opcja: uogólninie nie testu do metody

        Integer[] danei = {23,-22,345,122,2,2,1};
        Integer[] niedanei = {23+3,-22+3,345+3,122+3,2+3,2+3,1+3};
        testBST(bi, danei, niedanei);

        String[] danes = {"lorem", "ipsum", "dolor", "sit", "amet", "consectetur", "adipiscing", "elit"};
        String[] niedanes = {"a", "b", "c", "d", "e", "f", "g", "h", "i"};
        testBST(bs, danes, niedanes);
    }

    private static <T extends Comparable<T>> void testBST(BST<T> bst, T[] dane, T[] nieistniejące) {
        System.out.println("Test BST dla " + dane.getClass().getSimpleName());
        System.out.println("Czy puste? " + bst.czyPuste());
        for (T elt : dane)
            bst.wstaw(elt);
        System.out.println("rozmiar drzewa = " +
                bst.ileWęzłów());
        System.out.println("Istniejące");
        for (T elt : dane)
            System.out.println("\t"+bst.czyJest(elt));
        System.out.println("Chyba nie istniejące");
        for (T elt : nieistniejące)
            System.out.println("\t"+bst.czyJest(elt));
    }
}