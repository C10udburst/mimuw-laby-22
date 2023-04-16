package main;

import expressions.*;

public class Main {
    public static void main(String[] args) {
        test(Constant.fromDouble(42.0));
        test(new Variable());
        test((new Variable()).add(Constant.fromDouble(5.0)));
        test(Constant.fromDouble(2023.0).multiply((new Variable().add(Constant.fromDouble(5.0)))));
        test(new Sine(new Variable()));
        test(new Cosine(Constant.fromDouble(2.0).multiply(new Variable())));
        test(Constant.fromDouble(0.0).multiply(new Variable()));
        test(Constant.fromDouble(5.0).add(Constant.fromDouble(2.0)).add(new Variable()));
    }

    public static void test(Expression expr) {
        assert expr != null;
        System.out.println("expr(x)="+expr);
        double x = 12.0;
        System.out.println("\texpr("+x+")="+expr.evaluate(x));
        System.out.println("\texpr'(x)="+expr.derivative());
        double[] a = {0.0, 10.0};
        int n = 100;
        System.out.println("\tintegral from "+a[0]+" to "+a[1]+" is "+expr.integrate(a[0], a[1], n));
    }
}