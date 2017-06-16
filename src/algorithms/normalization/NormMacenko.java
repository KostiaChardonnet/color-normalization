package algorithms.normalization;

import java.lang.reflect.Array;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import icy.image.IcyBufferedImage;
import icy.image.IcyBufferedImageUtil;
import icy.sequence.Sequence;
import icy.type.DataType;
import icy.type.collection.array.Array1DUtil;

public class NormMacenko {

	private IcyBufferedImage target;
	private IcyBufferedImage source;
	private double Io = 255;
	private Sequence newSeq;
	private double minIntensity;

	public NormMacenko(IcyBufferedImage target, IcyBufferedImage source)//, double minIntensity)
	{
		this.target = target; 
		this.source = source;
		this.newSeq = new Sequence("Image normalisé ");
		this.minIntensity = 0.15;
		
		run();
	}


	private void run()
	{
		// Get the OD for both the target and source image.
		double[][] ODtarget = this.computeOD(target);
		double[][] ODsource = this.computeOD(source);
		
		
		RealMatrix MTarget = new EstimationUsingMacenko(ODtarget, this.minIntensity ).getM();

		Deconvolve deconvolveTarget = new Deconvolve(MTarget, ODtarget);

		MTarget = deconvolveTarget.getM();
		double[][] C = deconvolveTarget.getDCH();


		double[][] maxCTarget = new double[3][1];
		RealMatrix matrixCtmp = MatrixUtils.createRealMatrix(C);
		
		Percentile p = new Percentile();

		for(int i = 0 ; i < matrixCtmp.getColumnDimension() ; i++)
		{
			maxCTarget[i][0] = p.evaluate(matrixCtmp.getColumn(i), 99);
		}


		RealMatrix MSource = new EstimationUsingMacenko(ODsource, this.minIntensity).getM(); 

		Deconvolve deconvolveSource = new Deconvolve(MSource, ODsource);

		C = deconvolveSource.getDCH();

		matrixCtmp = MatrixUtils.createRealMatrix(C);
		double[][] maxCSource = new double[3][1];

		for(int i = 0 ; i < matrixCtmp.getColumnDimension() ; i++)
		{
			maxCSource[i][0] = p.evaluate(matrixCtmp.getColumn(i), 99);
		}


		// Divice each value of C by each value of maxCSource 
		// then multiply them by each value of maxCTarget
		double tmp;
		for(int i = 0 ; i < C.length ; i++)
		{
			for(int j = 0 ; j < C[0].length ; j++) // 3, r g b
			{
				tmp = C[i][j];
				tmp /= maxCSource[j][0];
				tmp *= maxCTarget[j][0];
				C[i][j] = tmp;
			}
		}

		// Reconstruction of the RGB image
		RealMatrix matrixC = MatrixUtils.createRealMatrix(C);
		MTarget = MTarget.scalarMultiply(-1);
		double[][] dataexp = matrixC.multiply(MTarget).getData();

		for(int i = 0 ; i < dataexp.length ; i++)
		{
			for(int j = 0 ; j < dataexp[0].length ; j++)
			{
				tmp = dataexp[i][j];
				tmp = Math.exp(tmp);
				tmp *= Io;
				dataexp[i][j] = tmp;
			}
		}


		RealMatrix matrixTmp = MatrixUtils.createRealMatrix(dataexp);


		IcyBufferedImage newImg = new IcyBufferedImage(source.getWidth(), source.getHeight(), matrixTmp.transpose().getData(), source.isSignedDataType());

		newImg =  IcyBufferedImageUtil.convertToType(newImg, DataType.UBYTE, false);

		this.newSeq.addImage(newImg);

	}

	public Sequence getSeq() { return this.newSeq; } 


	/**
	 * 
	 * @param image wich we will compute the Optimal Dentisy
	 * @return OD
	 */
	private double[][] computeOD(IcyBufferedImage image)
	{
		int sizeX = image.getSizeX(); 
		int sizeY = image.getSizeY();
		int sizeC = image.getSizeC();
		int nbPixel = sizeX * sizeY;

		double[][] OD = new double[nbPixel][sizeC];
		Object data = image.getDataXYC();

		double tmp;
		Object tab;

		int xy; 
		for(int x = 0 ; x < sizeX ; x++)
		{

			for(int y = 0 ; y < sizeY ; y++)
			{
				xy = y + x * sizeY; 

				for(int rgb = 0 ; rgb < sizeC ; rgb++)
				{
					tab = Array.get(data,rgb);						
				//	I[xy][rgb] = Array1DUtil.getValue(tab, xy, image.getDataType_());
					tmp = (Array1DUtil.getValue(tab, xy, image.getDataType_()) +1) / Io ;	
					OD[xy][rgb] = -Math.log(tmp);
				} 
			} 
		}

		return OD;
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


}
