/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gmmain;

/**
 *
 * @author vagisha
 */
public class GMMain {

    public static GMModel Gmm;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Gmm = new GMModel();             
		 
		 for(int i=0; i< Gmm.Threshold.length; i++){
		    Gmm.GaussianMixtureModel(i);             // To build GM Model
		    Gmm.OutputFinalReport(i);                // To Output Final Report
		    Gmm.ResetValues();                       // Resetting Value for Next Threshold GMM
		 }
    }
    
}
