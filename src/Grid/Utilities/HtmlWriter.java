/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.Utilities;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class HtmlWriter  {

    private PrintStream printStream;
    double salidas =0; 
    double maximoSalidas = 5000; 
    int pagina=1; 
    String fileName; 
    
    public HtmlWriter(String fileName) throws FileNotFoundException 
    {
        this.fileName = fileName; 
        init();
    }

    private void init() throws FileNotFoundException 
    {
        String nombreArchivo = "" ; 
        int totalCeros = 10-String.valueOf(pagina).length(); 
        for(int i =0; i<totalCeros; i++)
        {
            nombreArchivo+="0"; 
        }    
        nombreArchivo+= pagina; 
        
        printStream = new PrintStream(new FileOutputStream(fileName.replace(".html",  nombreArchivo+".html")), false);            
        printStream.println(" <html>");
        printStream.println("<head>");
        printStream.println("<title>");
        printStream.print("Output for " + fileName);
        printStream.println("</title>");
        printStream.println("</head>");
        printStream.println("<body>");
        printStream.println("<FONT FACE=\"courier\">");
    }

    public void fini() {
        printStream.println("</FONT>");
        printStream.println("</body>");
        printStream.println("</html>");
    }
    public void println(String log) 
    {
        salidas++; 
        if(salidas>maximoSalidas)
        {
            pagina++; 
            System.out.println("MAX "+pagina);
            salidas=0; 
            fini();
            printStream.flush();
            printStream.close();           
            try {
                init();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(HtmlWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }    
        printStream.println(log);         
    }        
}
