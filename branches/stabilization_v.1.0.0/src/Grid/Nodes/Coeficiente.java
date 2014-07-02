package Grid.Nodes;

/**
 * Inner class to iterate over the coefficient values
 *
 * @author AG2 team
 */
public class Coeficiente {

    private double valor;//Valor en lo q va
    private double paso;
    private double intvalIzq;
    private double intvalDer;

    public Coeficiente(double intervalIzq, double intervalDer, double paso) {
        this.intvalDer = intervalDer;
        this.intvalIzq = intervalIzq;
        this.paso = paso;
        valor = intvalIzq;
    }

    public void reset() {
        valor = intvalIzq;
    }

    public boolean hasNext() {

        if ( valor+paso  <=  intvalDer) {
            return true;
        }
        return false;
    }

    public double next() {
        valor += paso;
        return valor;
    }

    public double getIntvalIzq() {
        return intvalIzq;
    }

    public void setIntvalIzq(double intvalIzq) {
        this.intvalIzq = intvalIzq;
    }

    public double getIntvalDer() {
        return intvalDer;
    }

    public void setIntvalDer(double intvalDer) {
        this.intvalDer = intvalDer;
    }

    public double getValor() {
        return valor;
    }
}