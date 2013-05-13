
import java.util.ArrayList;

/**
 *
 * @author AG2 team
 */
public class Datos {

    private CoeficienteOfi coe1 = new CoeficienteOfi(1, 10, 1);
    private CoeficienteOfi coe2 = new CoeficienteOfi(1, 10, 1);
    private CoeficienteOfi coe3 = new CoeficienteOfi(1, 10, 1);
    private ArrayList<Double> combinacion;
    private ArrayList<ArrayList<Double>> totalCombinacion = new ArrayList<ArrayList<Double>>();

    public Datos() {
        iterarCoerficientes();
    }

    private void iterarCoerficientes() {

        int ciclos = 0;
        while (coe1.hasNext()) {
            coe1.next();
            while (coe2.hasNext()) {
                coe2.next();
                while (coe3.hasNext()) {
                    ciclos++;
                    System.out.println("Ciclo:" + ciclos + " Val coe1:" + coe1.getValor() + " Val coe2:" + coe2.getValor() + " Val coe3:" + coe3.next());
                    combinacion = new ArrayList<Double>();
                    combinacion.add(coe1.getValor());
                    combinacion.add(coe2.getValor());
                    combinacion.add(coe3.getValor());

                    totalCombinacion.add(combinacion);
                }
                coe3.reset();
            }
            coe2.reset();
        }
        System.out.println("Total ciclos:" + ciclos);
    }
}
