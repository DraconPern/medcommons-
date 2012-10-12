/*
 * Created on May 10, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.medcommons.router.services.wado.utils;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.awt.image.DataBufferByte;

import javax.crypto.CipherInputStream;

import net.medcommons.modules.crypto.AES;


/**
 * @author sean
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class WindowLevelTransform {

	final static int defaultBufferSize = 8096;
	public BufferedImage window12to8Bits(
		File imageFile,
		int window,
		int level,
		int width, 
		int height,
		String photometricInterpretation,
		long startBytes,
		long endBytes)
		throws java.io.IOException {
				int pixelOffset = 0;
		byte[] byte_buffer = new byte[defaultBufferSize];
		short[] shortPixels = new short[defaultBufferSize / 2];
	
		short[] lutArray;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		byte[] bytePixels = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
		int imageSize = Integer.MIN_VALUE; // # pixels
		final ShortBuffer srcRowBuf =
			ByteBuffer
				.wrap(byte_buffer)
				.order(ByteOrder.LITTLE_ENDIAN)
				.asShortBuffer();
		FileInputStream in = new FileInputStream(imageFile);
		
		LUT lut = new LUT(window, level, photometricInterpretation);
		lutArray = lut.getLUTArray();

		long fileSize = imageFile.length();
		if (startBytes != Integer.MIN_VALUE) {
			long skipBytes = startBytes;
			if (skipBytes < 0)
				skipBytes = imageFile.length() + skipBytes;
			if (skipBytes < 0)
				throw new RuntimeException(
					imageFile.getName()
						+ " skip bytes"
						+ startBytes
						+ " is longer than file: "
						+ imageFile.length());
			in.skip(skipBytes);
		}
		if (endBytes != Integer.MIN_VALUE) {
			imageSize = (int) ((endBytes - startBytes) / 2);
		}
		//System.out.println("imageSize is " + imageSize);
		int totalBytes = 0;
		int n;
		int nPixels;
		int totalPixels = 0;
		boolean exit = false;
		while ((n = in.read(byte_buffer, 0, defaultBufferSize)) != -1) {
			totalBytes += n;
			nPixels = n / 2;
			totalPixels += nPixels;
			//if (totalBytes >)
			srcRowBuf.rewind();
			srcRowBuf.get(shortPixels, 0, nPixels);
			if (imageSize != Integer.MIN_VALUE) {
				if (totalPixels > imageSize) {
					nPixels = totalPixels - imageSize;
					exit = true;
				}
			}

			for (int i = 0; i < nPixels; i++) {
				short pixelValue = (short) (shortPixels[i] );
				if (pixelValue < 0)
					pixelValue = 0;
				bytePixels[pixelOffset++] = (byte) lutArray[pixelValue];

			}
			
		}
		return (image);
	

	}
}
