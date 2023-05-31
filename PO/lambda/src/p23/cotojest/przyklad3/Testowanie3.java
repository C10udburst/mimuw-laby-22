package p23.cotojest.przyklad3;

class Testowanie3 {
    static boolean rzucaCoś(JakisKod3 kod){
        try {
            kod.rób();
            return false;
        } catch (Exception e) {
            return true;
        }
    }
}

