package głowny;

import pojemniki.BufCykl;

public class Main {
    public static void main(String[] args) {
        BufCykl buf = new BufCykl(1);
        int n = 100;
        for(int i=0; i<n; i++)
            buf.wstawK(i);
        for(int i=0; i<n/2; i++)            
            System.out.print(buf.pobierzP()+",");
        System.out.println();
            for(int i=0; i<n; i++)
            buf.wstawP(i);
        for(int i=0; i<n/2; i++)            
            System.out.print(buf.pobierzK()+",");
        System.out.println();
        System.out.println("buf = " + buf);
    }
}