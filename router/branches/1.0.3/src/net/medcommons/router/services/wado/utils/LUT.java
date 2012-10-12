/*
 * Created on May 10, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.medcommons.router.services.wado.utils;

/**
 * Extremely primitive LUT class.
 * Pretty much useful for CT images only.
 * Hard coded offset of 1024 for offset.
 *
 * @author sean
 *
 * 
 */
public class LUT {

	boolean windowLevelChanged = false;
	short[] lutArray = new short[2 << 16];
	final static String PI_MONOCHROME1 = "MONOCHROME1";
	final static String PI_MONOCHROME2 = "MONOCHROME2";
	final static String PI_RGB = "RGB";
	private String photometricInterpretation = PI_MONOCHROME2;
	private int window = Integer.MIN_VALUE;
	private int level = Integer.MIN_VALUE;

	public LUT(int window, int level, String photometricInterpretation) {
		this.window = window;
		this.level = level;
		this.photometricInterpretation = photometricInterpretation;
		this.lutArray = calculateLUT(window, level);

	}
	public short[] getLUTArray() {
		return (lutArray);
	}

	/**
	 *  This is very primitive. 
	 * Doesn't take into account rescale slope/intercept, suitable only 
	 * for CT images in current form.
	 * @param window
	 * @param level
	 */
	short[] calculateLUT(int window, int level) {

		int loIndexValue, hiIndexValue;
		int min, max;
		int hackedLevel = level + 1024;
		// HACK: shifting table elements so that all are positive.

		min = hackedLevel - (window / 2);
		max = hackedLevel + (window / 2);
		//System.out.println("Window=" + window+ ",level=" + level);
		//System.out.println("min1 = " + min + ", max1 = " + max);

		//min+= offset;
		//max+=offset;
		boolean needToInvert = false;

		try {

			// The output will be according to the standard for display devices, 
			// which is that black is zero and white is 255.  This is the same as
			// MONOCHROME2 input.  MONOCHROME1 input is the inverse, and 
			// therefore the window-levelling process will also have to
			// invert the values.
			if (photometricInterpretation.equals(PI_MONOCHROME2)) {
				loIndexValue = 0;
				hiIndexValue = 255;
			} else if (photometricInterpretation.equals(PI_MONOCHROME1)) {
				loIndexValue = 255;
				hiIndexValue = 0;
			} else {
				throw new RuntimeException(
					"Unknown photometric interpretation:"
						+ photometricInterpretation);
			}

			// Allocate memory for the lookup table.

			// Correct the min and max Pixel values so that they are
			// reasonable.
			if (min < 0)
				min = 0;
			if (max <= min)
				max = min + 1;

			int lookupTableSize = 4096;
			if (max < lookupTableSize)
				lookupTableSize = max + 1;

			int maxLookupValue = lookupTableSize - 1;

			if (max > maxLookupValue)
				max = maxLookupValue;

			if (min > max)
				min = max - 1;

			// Set the lookup table values lying outside the window.
			//lookupTable = new unsigned char[lookupTableSize];
			//memset(lookupTable, 0, lookupTableSize);
			if (min > 0)
				for (int i = 0; i < min; i++)
					lutArray[i] = (short) loIndexValue;
			//memset(lookupTable, loIndexValue, min);
			if (max < 4096)
				for (int i = max; i < 4096; i++)
					lutArray[i] = (short) hiIndexValue;

			lutArray[maxLookupValue] = (short) hiIndexValue;

			// Set the window values in the lookup table. MONOCHROME1
			// (white is low) needs to invert the values.
			double scale = 255.0 / ((double) (max - min));
			//System.out.println("scale is " + scale + ", min = " + min  +", max =" + max);
			if (photometricInterpretation.equals(PI_MONOCHROME1)) {
				for (int i = min; i <= max; i++)
					lutArray[i] = (short) ((max - i) * scale);
			} else if (photometricInterpretation.equals(PI_MONOCHROME2)) {
				for (int i = min; i <= max; i++)
					lutArray[i] = (short) ((i - min) * scale);
			}

			if (needToInvert) {
				for (int i = 0; i < lookupTableSize; i++)
					lutArray[i] = (short) (255 - lutArray[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (lutArray);
	}
}
