package algorithms.normalization;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import Jama.Matrix;

public class Deconvolve {

	private RealMatrix M;
	public double[][] OD;
	private double[][] DCh;

	public Deconvolve(RealMatrix matrix, double[][] OD)
	{
		this.M = matrix;
		this.OD = OD;

		run();
	}


	private void run()
	{
		if(M.getRowDimension() < 3) 
		{
			double[][] oldM = M.getData();
			double[][] newM = new double[oldM.length+1][oldM[0].length];
			
			Vector3D v1 = new Vector3D(M.getRow(0)); 
			Vector3D v2 = new Vector3D(M.getRow(1));
			
			v1 = Vector3D.crossProduct(v1, v2);

			double[] tmp = v1.toArray();
			
			for(int i = 0 ; i < oldM.length ; i++)
			{
				for(int j = 0 ; j < oldM[0].length ; j++)
				{
					newM[i][j] = oldM[i][j];
				}
			}
			
			for(int i = 0 ; i < tmp.length ; i++)
			{
				newM[2][i] = tmp[i];
			}
			
			M = MatrixUtils.createRealMatrix(newM);
			
		}
			
		
		double[][] datas = M.getData();
		double[][] tmp = new double[M.getRowDimension()][1];
		double var;
		
		for(int i = 0 ; i < datas.length ; i++)
		{
			var = 0;
			for(int j = 0 ; j < datas[i].length ; j++)
			{
				var += Math.pow(datas[i][j], 2);
				tmp[i][0] = Math.sqrt(var);
			}
		}
		
		// repeat the matrix
		tmp = repmat(tmp);

		for(int i = 0 ; i < datas.length ; i++)
		{
			for(int j = 0 ; j < datas[0].length ; j++)
			{
				datas[i][j] = datas[i][j] / tmp[i][j]; 
			}
		}
	
		M = MatrixUtils.createRealMatrix(datas);
		
		//  Using JAMA library to get the pseudo-inverse of the matrix. 
		// The pseudo-inverse is no aviable in Apache Math (I didn't find it)
		Matrix pseudoInv = new Matrix(M.getData());
		RealMatrix invM = MatrixUtils.createRealMatrix(pseudoInv.inverse().getArray());
		
		RealMatrix matrixOD = MatrixUtils.createRealMatrix(OD);
		RealMatrix C = matrixOD.multiply(invM); 

		this.DCh = C.getData();
		
	}

	public static double[][][] reshape(double[][] A, int m, int n) {
		int origM = A.length;
		if(origM != m*n){
			throw new IllegalArgumentException("New matrix must be of same area as matix A");
		}
		double[][][] B = new double[m][n][3];
		double[] A1D = new double[A.length * A[0].length];

		int index = 0;
		for(int i = 0;i<A.length;i++){
			for(int j = 0;j<A[0].length;j++){
				A1D[index++] = A[i][j];
			}
		}

		index = 0;
		for(int i = 0;i<n;i++){
			for(int j = 0;j<m;j++){
				for(int k = 0 ; k < 3 ; k++)
				{
					B[j][i][k] = A1D[index++];
				}
			}
		}
		return B;
	}

	private double[][] repmat(double[][] matrix)
	{
		double[][] result = new double[matrix.length][3];

		for(int i = 0 ; i < result.length ; i++)
		{
			for(int j = 0 ; j < result[0].length ; j++)
			{
				result[i][j] = matrix[i][0];
			}
		}
		
		return result;
	}

	public double[][] getDCH() { return this.DCh; }

	public RealMatrix getM() {  return M;  }

}
