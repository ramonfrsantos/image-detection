package com.imagedetection;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

public class ImageDetection {
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		boolean imageDetected = false; 
		
		String img1File = "images/in/test5.jpeg";
		Mat src = Imgcodecs.imread(img1File);
		
		String xmlFile = "xml/cascade.xml";
		CascadeClassifier cc = new CascadeClassifier(xmlFile);
		
		MatOfRect imgDetection = new MatOfRect();
		cc.detectMultiScale(src, imgDetection);
		if(imgDetection.toArray().length > 0) {
			imageDetected = true;
		}
		System.out.println("Image detected: " + imageDetected);
		
		for(Rect rect: imgDetection.toArray()) {
			Imgproc.rectangle(src, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y+rect.height), new Scalar(0,0,255), 3);
		}
		
		Imgcodecs.imwrite("images/out/test5_out.jpeg", src);
		System.out.println("Program finished!");
	}
}