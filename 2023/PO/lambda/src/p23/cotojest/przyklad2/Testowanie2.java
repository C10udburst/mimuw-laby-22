package p23.cotojest.przyklad2;

class Testowanie2 {
    static boolean rzucaCoś(JakisKod2 kod){
        try {
            kod.rób();
            return false;
        } catch (Exception e) {
            return true;
        }
    }
}

