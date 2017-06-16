package algorithms.normalization;

import java.lang.reflect.Array;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import icy.image.IcyBufferedImage;
import icy.image.IcyBufferedImageUtil;
import icy.sequence.Sequence;
import icy.type.DataType;
import icy.type.collection.array.Array1DUtil;

public class NormReinhard
{
	private IcyBufferedImage target;
	private IcyBufferedImage source;
	private Sequence newSeq;
	private final static double oneThird = 1.0 / 3.0;
	private final static double sixteenOnehundredsixteen = 16.0 / 116.0;
	
	
	public NormReinhard(IcyBufferedImage target, IcyBufferedImage source)
	{		
		this.target = target; 
		this.source = source;
		this.newSeq = new Sequence();
		run();
	}


	private void run()
	{
		StandardDeviation sd = new StandardDeviation();
		
		/**
		 * Source
		 */
		IcyBufferedImage sourceLab = this.convert_rgb2Lab(source);
		double[][] dataSourceLab = this.getImageData(sourceLab);

		double[] ms = mean(dataSourceLab);
		
		RealMatrix matrixSourceLab = MatrixUtils.createRealMatrix(dataSourceLab);

		double[] stds = new double[3];
		
		for(int i = 0 ; i < 3 ; i++)
			stds[i] = sd.evaluate(matrixSourceLab.getColumn(i), ms[i]); 
		
		
		/**
		 * Target
		 */
		IcyBufferedImage targetLab = this.convert_rgb2Lab(target);
		double[][] dataTargetLab = this.getImageData(targetLab);
		
		double[] mt = mean(dataTargetLab);
		
		RealMatrix matrixTargetLab = MatrixUtils.createRealMatrix(dataTargetLab);
		
		double[] stdt = new double[3];
		
		for(int i = 0 ; i < 3 ; i++)
			stdt[i] = sd.evaluate(matrixTargetLab.getColumn(i), ms[i]); 
		
		/**
		 * Normalisation
		 */
		double[][] normLab = new double[dataSourceLab.length][3];
		
		for(int i = 0 ; i < dataSourceLab.length ; i++) {
			for(int j = 0 ; j < 3 ; j++) {
				normLab[i][j] = ( dataSourceLab[i][j] - ms[j] ) * ( stdt[j] / stds[j] ) + mt[j];
			}
		}		
		
		RealMatrix tmp = MatrixUtils.createRealMatrix(normLab);
		
		IcyBufferedImage imageNormaliseLab = new IcyBufferedImage(source.getSizeX(), source.getSizeY(), 3, DataType.DOUBLE);
		imageNormaliseLab.setDataXY(0, Array1DUtil.doubleArrayToArray(tmp.getColumn(0), imageNormaliseLab.getDataXY(0)));
		imageNormaliseLab.setDataXY(1, Array1DUtil.doubleArrayToArray(tmp.getColumn(1), imageNormaliseLab.getDataXY(1)));
		imageNormaliseLab.setDataXY(2, Array1DUtil.doubleArrayToArray(tmp.getColumn(2), imageNormaliseLab.getDataXY(2)));
		
		IcyBufferedImage newImg = this.convert_Lab2RGB(imageNormaliseLab);
		
		/**
		 * CrŽation image
		 */

		newImg = IcyBufferedImageUtil.convertToType(newImg, DataType.UBYTE, false);

		this.newSeq.addImage(newImg);

	}
	

	
	
	/**
	 * ================================
	 * 		CONVERSION COULEUR
	 * ================================
	 */


	private IcyBufferedImage convert_rgb2Lab(IcyBufferedImage image){

		// ImageIn
		byte[] R = image.getDataXYAsByte(0);
		byte[] G = image.getDataXYAsByte(1);
		byte[] B = image.getDataXYAsByte(2);

		int w = image.getSizeX();
		int h = image.getSizeY();

		// ImageOut
		IcyBufferedImage imageOut = new IcyBufferedImage(image.getSizeX(), image.getSizeY(), 3, DataType.DOUBLE);


		double[] L = new double[w*h];
		double[] ca = new double[w*h];
		double[] cb = new double[w*h];

		for ( int x=0; x<w; x++ )
			for ( int y=0; y<h; y++ )
			{

				int r = R[x+w*y]& 0xFF;
				int g = G[x+w*y]& 0xFF;
				int b = B[x+w*y]& 0xFF;

				double[] xyz = fromRGBtoXYZ(r, g, b);

				double[] Lab = fromXYZtoLab(xyz);

				L[x+w*y] = Lab[0];
				ca[x+w*y] = Lab[1];
				cb[x+w*y] = Lab[2];
			}
	
		imageOut.setDataXY(0, Array1DUtil.doubleArrayToArray(L, imageOut.getDataXY(0)));
		imageOut.setDataXY(1, Array1DUtil.doubleArrayToArray(ca, imageOut.getDataXY(1)));
		imageOut.setDataXY(2, Array1DUtil.doubleArrayToArray(cb, imageOut.getDataXY(2)));

		return imageOut;
	
	}


	private IcyBufferedImage convert_Lab2RGB(IcyBufferedImage image){

		// ImageIn
		double[] L = image.getDataXYAsDouble(0);
		double[] ca = image.getDataXYAsDouble(1);
		double[] cb = image.getDataXYAsDouble(2);

		int w = image.getSizeX();
		int h = image.getSizeY();

		// ImageOut
		IcyBufferedImage imageOut = new IcyBufferedImage(image.getSizeX(), image.getSizeY(), 3, DataType.DOUBLE);

		double[] R = new double[w*h];
		double[] G = new double[w*h];
		double[] B = new double[w*h];

		for ( int x=0; x<w; x++ )
			for ( int y=0; y<h; y++ )
			{

				double[] xyz = fromLabtoXYZ(L[x+y*w], ca[x+y*w], cb[x+y*w]);

				double[] rgb = fromXYZtoRGB(xyz);

				R[x+w*y] = rgb[0];
				G[x+w*y] = rgb[1];
				B[x+w*y] = rgb[2];
			}


		imageOut.setDataXY(0, Array1DUtil.doubleArrayToArray(R, imageOut.getDataXY(0)));
		imageOut.setDataXY(1, Array1DUtil.doubleArrayToArray(G, imageOut.getDataXY(1)));
		imageOut.setDataXY(2, Array1DUtil.doubleArrayToArray(B, imageOut.getDataXY(2)));

		return imageOut;
		
		
	}

	// RGB->XYZ
	double [] fromRGBtoXYZ(int R,int G,int B){
		double r = ((double) R)/255.;
		double g = ((double) G)/255.;
		double b = ((double) B)/255.;

		double c1 = 0.04045;
		double c2 = 0.055;
		double c3 = 1.055;
		double c4 = 12.92;

		if (r>c1) r=Math.pow((r+c2)/c3, 2.4);
		else r=r/c4;
		if (g>c1) g=Math.pow((g+c2)/c3, 2.4);
		else g=g/c4;
		if (b>c1) b=Math.pow((b+c2)/c3, 2.4);
		else b=b/c4;

		r = r*100;
		g = g*100;
		b = b*100;

		//Observer. = 2¡, Illuminant = D65
		double[] result = new double[3];
		result[0] = r * 0.4124 + g * 0.3576 + b * 0.1805;
		result[1] = r * 0.2126 + g * 0.7152 + b * 0.0722;
		result[2] = r * 0.0193 + g * 0.1192 + b * 0.9505;
		// System.out.println(" X: "+result[0]+" Y: "+result[1]+" Z: "+result[2]);
		return(result);
	}


	//XYZ Ñ> CIE-L*ab
	double [] fromXYZtoLab(double[] XYZ){

		double ref_X =  95.047; double ref_Y = 100.000; double ref_Z = 108.883;
		double var_X = XYZ[0] / ref_X;          //ref_X =  95.047   Observer= 2¡, Illuminant= D65
		double var_Y = XYZ[1] / ref_Y;          //ref_Y = 100.000
		double var_Z = XYZ[2] / ref_Z;          //ref_Z = 108.883


		if ( var_X > 0.008856 ) var_X = Math.pow(var_X, oneThird);
		else     var_X = ( 7.787 * var_X ) + (sixteenOnehundredsixteen );
		if ( var_Y > 0.008856 ) var_Y = Math.pow(var_Y, oneThird );
		else    var_Y = ( 7.787 * var_Y ) + ( sixteenOnehundredsixteen );
		if ( var_Z > 0.008856 )  var_Z = Math.pow(var_Z, oneThird );
		else     var_Z = ( 7.787 * var_Z ) + ( sixteenOnehundredsixteen );

		double[] result = new double[3];
		result[0] = ( 116 * var_Y ) - 16;
		result[1] =  500 * ( var_X - var_Y );
		result[2] =  200 * ( var_Y - var_Z );
		return(result);
	}

	//CIE-L*ab Ñ>  XYZ
	double [] fromLabtoXYZ(double L, double ca, double cb){
		double ref_X =  95.047; double ref_Y = 100.000; double ref_Z = 108.883;
		double var_Y = ( L + 16 ) / 116;
		double var_X = ca / 500 + var_Y;
		double var_Z = var_Y - cb / 200;

		double tmp = Math.pow(var_Y,3);
		if ( tmp > 0.008856 ) var_Y = tmp;
		else    var_Y = ( var_Y - 16. / 116. ) / 7.787;
		tmp = Math.pow(var_X,3);
		if ( tmp > 0.008856 ) var_X = tmp;
		else     var_X = ( var_X - 16. / 116. ) / 7.787;
		tmp = Math.pow(var_Z,3);
		if ( tmp > 0.008856 ) var_Z = tmp;
		else    var_Z = ( var_Z - 16 / 116 ) / 7.787;

		double[] result = new double[3];
		result[0] = ref_X * var_X;     //ref_X =  95.047     Observer= 2¡, Illuminant= D65
		result[1] = ref_Y * var_Y;     //ref_Y = 100.000
		result[2] = ref_Z * var_Z;     //ref_Z = 108.883
		return(result);
	}

	double[] fromXYZtoRGB(double [] XYZ){
		double var_X = XYZ[0] / 100;        //X from 0 to  95.047      (Observer = 2¡, Illuminant = D65)
		double var_Y = XYZ[1] / 100;        //Y from 0 to 100.000
		double var_Z = XYZ[2] / 100;        //Z from 0 to 108.883

		double var_R = var_X *  3.2406 + var_Y * -1.5372 + var_Z * -0.4986;
		double var_G = var_X * -0.9689 + var_Y *  1.8758 + var_Z *  0.0415;
		double var_B = var_X *  0.0557 + var_Y * -0.2040 + var_Z *  1.0570;

		if ( var_R > 0.0031308 ) var_R = 1.055 * Math.pow( var_R , ( 1. / 2.4 ) ) - 0.055;
		else                     var_R = 12.92 * var_R;
		if ( var_G > 0.0031308 ) var_G = 1.055 * Math.pow( var_G , ( 1. / 2.4 ) ) - 0.055;
		else                     var_G = 12.92 * var_G;
		if ( var_B > 0.0031308 ) var_B = 1.055 *  Math.pow(var_B , ( 1. / 2.4 ) ) - 0.055;
		else                     var_B = 12.92 * var_B;

		double []result=new double[3];
		result[0] = var_R * 255;
		result[1] = var_G * 255;
		result[2] = var_B * 255;
		return(result);

	}
	
	/**
	 * ================================
	 * 		 OTHER FONCTIONS
	 * ================================
	 */
	
	private double[][] getImageData(IcyBufferedImage img)
	{
		int sizeX = img.getSizeX(); 
		int sizeY = img.getSizeY();
		int sizeC = img.getSizeC();
		int nbPixel = sizeX * sizeY;

		double[][] result = new double[nbPixel][sizeC];
		Object data = img.getDataXYC();
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
					result[xy][rgb] = Array1DUtil.getValue(tab, xy, img.getDataType_());
				} 
			} 
		}

		return result;
	}
	
	
	double[] mean(double[][] tab)
	{
		double[] mean = new double[] {0,0,0};
		for(int j = 0 ; j < 3; j++)
		{
			for(int i = 0 ; i < tab.length ; i++)
			{
				mean[j] += tab[i][j];
			}
			
			mean[j] /= tab.length;
		}
		return mean;
	}
	
	
	/**
	 * ================================
	 * 			GETTER
	 * ================================
	 */
	public Sequence getSeq(){ return this.newSeq; }

	
}