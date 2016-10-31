/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gmmain;

import java.text.DecimalFormat;
import javax.swing.JOptionPane;

/**
 *
 * @author vagisha
 */
public class GMModel {
    public String fileName = "samples_add_time.txt";
    public int NumOfDataPoints = CountTotalDataPointsInFile(fileName);
	public double[] MainDataSet;
	
	public double[] Threshold = {0.1, 0.01, 0.001,0.0001};
	public double[] Pi_k, Mu_k, Sigma_k;
	
	public int NumOfEMIterations = 0;
	public double rough, TotalLoglikelihood = 0;
	
	public GMModel(){                                      // Constructor
		MainDataSet = readFile(fileName, NumOfDataPoints);
                
                //MainDataSet[0] = 4.3;
                //MainDataSet[1] = 
		Pi_k  = new double[3];        
		Mu_k  = new double[3];
		Sigma_k = new double[3];
        }
        
        public void GaussianMixtureModel(int index){
		
		InitializeParameters(index);	        
		double 	ChangeInLogLikelihood = 1;
		
		while(ChangeInLogLikelihood > Threshold[index])
		{
			   double LogLikelihood=0, ProbOfXn=0;
				
				// Total LogLikelihood...
				for(int i=0; i< MainDataSet.length; i++){
					ProbOfXn = 0;
					for(int j=0; j<3; j++){            // 3 as we have 3 Gaussian Mixture Components
						ProbOfXn = ProbOfXn + Pi_k[j]*GaussianFunction(MainDataSet[i], Mu_k[j], Sigma_k[j]);  		
					}	
					LogLikelihood = LogLikelihood + log2(ProbOfXn);
                                        
				}
				
				// E- Step   Calculating Responsibilities...
				
		        double[][] Rspb = new double[MainDataSet.length][3];          // Tau_nk: n data points and k component mixture
				for(int i=0; i< MainDataSet.length; i++){
					ProbOfXn =0;
					for(int j=0; j<3; j++){
						Rspb[i][j] = Pi_k[j]*GaussianFunction(MainDataSet[i], Mu_k[j], Sigma_k[j]);
						ProbOfXn = ProbOfXn + Rspb[i][j]; 
					}
					Rspb[i][0] = Rspb[i][0]/ProbOfXn;
					Rspb[i][1] = Rspb[i][1]/ProbOfXn;
					Rspb[i][2] = Rspb[i][2]/ProbOfXn;
                                //        Rspb[i][3] = Rspb[i][3]/ProbOfXn;
					
				}
							
				// M-Step     Re-estimating Parameters...
				
				double[] N_k = new double[3];
	            
				for(int k=0; k<3 ; k++){             // Calculating N_k's
					for(int n=0; n< MainDataSet.length; n++){
						N_k[k] = N_k[k] + Rspb[n][k];
					}
				}
				
				// Calculating new Mu_k's
				for(int k=0; k<3; k++){
					Mu_k[k] = 0 ;
					for(int n=0; n< MainDataSet.length; n++){
						Mu_k[k] = Mu_k[k] + Rspb[n][k]*MainDataSet[n];
					}
					Mu_k[k] = Mu_k[k]/N_k[k] ;
				}
				
				// Calculating new Sigma_k's                 // Confusion...NOT SURE...in Norm Implementation...
				for(int k=0; k<3; k++){
					Sigma_k[k] = 0;
					for(int n=0; n< MainDataSet.length; n++){
						Sigma_k[k] = Sigma_k[k] + Rspb[n][k]*(MainDataSet[n]-Mu_k[k])*(MainDataSet[n]-Mu_k[k]);
					}
				   	Sigma_k[k] = Sigma_k[k]/N_k[k];
				}
				
				// Calculating new Pi_k's
				for(int k=0; k<3; k++){
					Pi_k[k] = N_k[k]/MainDataSet.length;
				}
				
	            double NewLogLikelihood=0, ProbOfX=0;
				
				// New Total LogLikelihood...
				for(int i=0; i< MainDataSet.length; i++){
					ProbOfX = 0;
					for(int j=0; j<3; j++){            // 3 as we have 3 Gaussian Mixture Components
						ProbOfX = ProbOfX + Pi_k[j]*GaussianFunction(MainDataSet[i], Mu_k[j], Sigma_k[j]);  		
					}	
					NewLogLikelihood = NewLogLikelihood + log2(ProbOfX);
				}
				
				// ChangeInTotalLogLikelihood
				ChangeInLogLikelihood = NewLogLikelihood - LogLikelihood;
				TotalLoglikelihood = NewLogLikelihood;
				
				System.out.println("Total LogLikelihood: "+TotalLoglikelihood);
				System.out.println("Change in LogLikelihood: "+ChangeInLogLikelihood);
				System.out.println("EM Iteration #: "+NumOfEMIterations++);
				
		}// While	
		
   } // function GaussianMixtureModel
	public double GaussianFunction(double x_n, double Mu_k , double Sigma_k ){     // return N(x_n|...)
		
		double Prob = 0;
		Prob = Math.pow(2*3.14159265*Sigma_k ,-0.5)*Math.exp(-(Math.pow(x_n-Mu_k, 2))/(2*Sigma_k)); 
	    return Prob;
	}
	
	public void ResetValues(){
		NumOfEMIterations = 0;
		TotalLoglikelihood = 0;
	}
        
        
        public void OutputFinalReport(int i){
		
		System.out.println("\nTotalLogLikelihood: "+TotalLoglikelihood);
		System.out.println("Number of EM Iterations: "+ NumOfEMIterations+"\n");
		for(int j=0; j<3; j++){
		   System.out.println(j+" Pi_k: "+Pi_k[j] +"\nMu_k: "+ Mu_k[j] +"\nSigma_k: "+ Sigma_k[j]+"\n");	
		}
		
		StringBuilder builder= new StringBuilder();
    	DecimalFormat df = new DecimalFormat("#.#######");
    	
    	builder.append("Project 3: EM Algorithm & Gaussian Mixture Model \n\n");
    	builder.append("3 Component Mixture\n");
    	builder.append("Total DataPoints: "+ NumOfDataPoints + "\n\n");
    	
    	builder.append("Best Set of Parameters For Threshold = " + Threshold[i]+"\n\n");
    	builder.append("Total LogLikelihood : "+df.format(TotalLoglikelihood)+"\n");
    	builder.append("Number of EM Iterations : "+NumOfEMIterations+"\n\n");
    	
    	for(int j=0; j<3; j++){
    		builder.append("Group: "+j+"\n\n");
    	    builder.append("Pi for Group "+j+"         :  "+df.format(Pi_k[j])+"\n");
    	    builder.append("Mu for Group "+j+"       :  "+df.format(Mu_k[j])+"\n");
    	    builder.append("Sigma for Group "+j+" :  "+df.format(Sigma_k[j])+"\n\n");
    	    
    	}
    	
    	JOptionPane.showMessageDialog(null, builder.toString());
    	
	}
        
        public static double log2(double num)
	{ if(num==0)
		return 0;
	  else 
	    return (Math.log(num)/Math.log(2));
	}
	
   public void InitializeParameters(int index){
	 	
		Pi_k[0] = 0.33;                              
		Pi_k[1] = 0.33;                              
		Pi_k[2] = 0.34;   
                //Pi_k[3] = 0.35;   
		
		Mu_k[0] = 1;                               
		Mu_k[1] = 8;                               
		Mu_k[2] = 18;  
                //Mu_k[3] = 2;  
		
		Sigma_k[0] = 3;                             
		Sigma_k[2] = 4;
		
		if(index == 0){
		  Sigma_k[1] =  3;  
		  rough = Sigma_k[1]; 
		}
		else
		  Sigma_k[1] = rough;
                
               // Sigma_k[3] = 5;
		
	}
	
	public double[] readFile(String fileName, int TotalDataPoints)        // Length of array to be filled  
	{   
	    double[] alldatapoints = new double[TotalDataPoints];
	    
	    TextFileInput tfi = new TextFileInput(fileName);
		String line = tfi.readLine(); 
	    int count=0;
            System.out.println("Okay till 209");
		while(line!=null)
		{
                    
                    System.out.println("Reading line : " +line);
			alldatapoints[count] =  Double.parseDouble(line);
			 count++;      
			 line=tfi.readLine();
		}
		System.out.println("Okay till 216");
		return alldatapoints;        
  	}// Read File
	
	public static int CountTotalDataPointsInFile(String fileName)        // length of array to be filled  
    {   
		    int CountLine=0;
		    TextFileInput tfi = new TextFileInput(fileName);
			String line = tfi.readLine(); 
		    while(line!=null)
			{	line=tfi.readLine();
				CountLine++;
			}
		    System.out.println("Countline : "+ String.valueOf(CountLine));
			return CountLine;                                         
	 }
	
	public int random(){
    	int temp = 0;
		temp = (int)(Math.random() * 1000) + 1;
    	return temp;
    } 
    
    public int random_Num_between(int a, int b){
		
		int num;
		while(true){
		    num = a+random()/100;
			if(num >=a && num<=b){
			   break;	
			}
		 }	
		return num;	
	}
    
    public void getProb(){
    
        double x_n = 16;
        
        for(int j=0; j<3 ; j++){
        
            double prob = Math.pow(2*3.14159265*Sigma_k[j] ,-0.5)*Math.exp(-(Math.pow(x_n-Mu_k[j], 2))/(2*Sigma_k[j])); 
            System.out.println(String.valueOf(prob));
        
        }
    
    }
	
    
}

