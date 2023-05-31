package p48.cotomoze.przyklad4;

class Testowanie4 {
    static boolean rzucaCoś(JakisKod4 kod){
        try {
            kod.rób();
            return false;
        } catch (Exception e) {
            return true;
        }
    }
}

