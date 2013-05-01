/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author franklin
 */
public class Coeficiente {
    
    private double valor;//Valor en lo q va
    private double paso;
    private double intvalIzq;
    private double intvalDer;
    
    public Coeficiente(double intervalIzq,double intervalDer,double paso)
    {
        this.intvalDer = intervalDer;
        this.intvalIzq = intervalIzq;
        this.paso = paso;
        reset();
    }
    
    public void reset()
    {
        valor = intvalIzq; 
    }
    
    public boolean hasNext(){
    
        if(valor<=intvalDer){
            return true;
        }
        return false;
    }
    
    public double next()
    {
        double coeficiente = valor; 
        valor = valor+paso;
        return coeficiente;
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
