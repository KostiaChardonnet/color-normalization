package algorithms.normalization;
import java.util.ArrayList;

import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

public class EstimationUsingMacenko {

	private double minIntensity;
	private double[][] OD;
	private RealMatrix M;
	private int alpha = 1; // TODO 


	public EstimationUsingMacenko(double[][] OD, double i) 
	{
		this.OD = OD;
		this.minIntensity = i;
		try {
			M = run();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private RealMatrix run() throws InterruptedException
	{

		double[][] ODhat = removeOD();
		RealMatrix matrixODhat = MatrixUtils.createRealMatrix(ODhat);


		Covariance covtmp = new Covariance(matrixODhat);
		RealMatrix cov = covtmp.getCovarianceMatrix(); 
		

		EigenDecomposition eigein = new EigenDecomposition(cov); 
		RealMatrix V = eigein.getV();

		V = V.getSubMatrix(0,2,0,1); // get the 2 first column of V  

		double[][] dataV = V.getData();
		double[][] newV = new double[dataV.length][2];

		for(int i = 0 ; i < newV.length ; i++)
		{
			for(int j = 0 ; j < 2; j++)
			{
				if(j!=1)
					newV[i][j] = Math.abs(dataV[i][j]); // get the absolute value of the data, since the first column is negative because of the way Apache compute.
				else
					newV[i][j] = dataV[i][j];
			}
		}

		// Create the final matrix of vector
		for(int i = 0 ; i < newV.length ; i++) 
		{
			double tmp1 = newV[i][0];
			double tmp2 = newV[i][1];
			newV[i][0] = tmp2;
			newV[i][1] = tmp1;
		}

		V = MatrixUtils.createRealMatrix(newV);

		// Compute the THETA Matrix
		RealMatrix THETA = matrixODhat.multiply(V);
		double[][] thetaData = THETA.getData();

		// Compute the PI array
		double[] PHI = new double[THETA.getRowDimension()];
		for(int i = 0 ; i < thetaData.length; i++)
		{
			PHI[i] =  Math.atan2(thetaData[i][1],  thetaData[i][0]) ;  
		}

		
		Percentile p = new Percentile();
		double minPhi = p.evaluate(PHI, this.alpha);
		double maxPhi = p.evaluate(PHI, (100-this.alpha));

		// Get vector VEC1 and 2 
		RealMatrix tmpMatrix;
		tmpMatrix = MatrixUtils.createRealMatrix(new double[][] { {Math.cos(minPhi)}, {Math.sin(minPhi)} } );
		RealMatrix VEC1 = V.multiply(tmpMatrix);
		tmpMatrix = MatrixUtils.createRealMatrix(new double[][] { {Math.cos(maxPhi)}, {Math.sin(maxPhi)} } );
		RealMatrix VEC2 = V.multiply(tmpMatrix);


		double[][] vec1 = VEC1.getData();
		double[][] vec2 = VEC2.getData();
		double[][] m = new double[3][2];

		if(vec1[0][0] > vec2[0][0])
		{
			for(int j = 0 ; j < vec1[0].length; j++)
			{
				for(int i = 0 ; i <vec1.length; i++)
				{
					m[i][0] = vec1[i][j];
					m[i][1] = vec2[i][j];
				}
			}
		}
		else
		{
			for(int j = 0 ; j < vec1[0].length; j++)
			{
				for(int i = 0 ; i <vec1.length; i++)
				{
					m[i][0] = vec2[i][j];
					m[i][1] = vec1[i][j];
				}
			}
		}

		RealMatrix M = MatrixUtils.createRealMatrix(m);
		return M.transpose();

	}


	/*
	 * Remove OD if intensity is less than the minIntensity.
	 * @return the new Od
	 */
	public double[][] removeOD(){

		double[][] result = null;	
		ArrayList<double[]> tmp = new ArrayList<double[]>();

		for(double[] column : OD)
		{
			boolean found = false;

			for(double data : column)
			{
				if(Double.compare(data, this.minIntensity) < 0) // if the data is inferior or equals to the minIntensity
				{
					found = true; 
					break;
				}
			}

			if(!found)
			{
				tmp.add(column);
			}
		}

		if(tmp.size() == OD.length) // no data has been deleted
		{
			return OD; 
		}
		else if(tmp.size() != 0) // some datas has been delete
		{
			int nbPixel = tmp.size();		
			int C = tmp.get(0).length;
			result = new double[nbPixel][C];

			for(int l = 0 ; l < nbPixel; l++)
			{
				result[l] = tmp.get(l);
			}

			return result; 
		}
		else // all data has been deleted
		{
			return new double[0][0];
		}
	}

	public RealMatrix getM() { return this.M; } 

}
