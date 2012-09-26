/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator11.Hybrid.HybridTestCase;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class CalculateBandwidth {

    public static double ERLANG = 0.3;
    public static double LOAD = 0.09;
    public static double delta = 0.1;

    public static void main(String[] args) {

    System.out.println(ErlangB(100/1, 61));
        
        //calculateSymmetricBlocking1(3, 0.1, 0.01, 5);
        

//        for (int i = 1; i < 10; i++) {
//            double erlangB = ErlangB(LOAD, i);
//            System.out.println(erlangB);
//            if (Math.abs(erlangB - ERLANG) <= delta) {
//                System.out.println("Number of wavelengths needed : " + i);
//                break;
//            }   
//        }
    }

    /**
     * Calculates the factorial of a number.
     * @param i The number to factor
     * @return The factorial
     */
    public static int calculateFactorial(int i) {
        if (i == 1) {
            return i;
        } else {
            return i * calculateFactorial(i - 1);
        }
    }

    /**
     * Calculates the ErlangB given E = lamda/mu, m number of servers.
     * @param E = lambda/mu
     * @param m the number of servers
     * @return Erlang B of the given paramters.
     */
    public static double ErlangB(double E, int m) {
        double InvB = 1.0;
        for (int j = 1; j <= m; j++) {
            InvB = 1.0 + (j / E) * InvB;
        }
        return 1.0 / InvB;
    }

    public static void calculateSymmetricBlocking1(int depth, double lambda, double mu, int servers) {
        double erlang = 0;
        for (int i = 0; i < depth-1; i++) {
            if (i == 0) {
                erlang = ErlangB(lambda / mu, servers);
                System.out.println("Depth\t:\t" + i + "\tIAT\t:\t" + lambda + "\terlang\t:\t" + erlang);
            } else {
                lambda = 2 * (1 - erlang) * lambda;
                erlang = ErlangB(lambda / mu, servers);
                System.out.println("Depth\t:\t" + i + "\tIAT\t:\t" + lambda + "\terlang\t:\t" + erlang);
            }
        }
        
        //calculation of the center
        lambda = 2 * (1 - erlang) * lambda;
        erlang = ErlangB((lambda/2) / mu, servers);
        System.out.println("Depth\t:\t" + -1 + "\tIAT\t:\t" + lambda + "\terlang\t:\t" + erlang);
        
        for (int i = 0; i < depth; i++) {
            lambda = ((1 - erlang) * lambda/2);
            erlang = ErlangB((lambda/2) / mu, servers);
            System.out.println("Depth\t:\t" + i + "\tIAT\t:\t" + lambda + "\terlang\t:\t" + erlang);
        }

    }
    



}
