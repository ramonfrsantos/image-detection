package br.com.imagedetection.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.opencv.core.Rect;

import br.com.imagedetection.model.ImageModelDTO;
import br.com.imagedetection.model.ImageResponseDTO;

@RestController
public class ImageDetectionController {
	
	@PostMapping("/image-analysis")
	public ImageResponseDTO getAnalysis(@RequestBody ImageModelDTO model) {
	
		nu.pattern.OpenCV.loadShared();
		
		boolean imageDetected = false;
		
		String inputFilePath = "./test_temp." + model.getFormato();
		
		byte[] data = org.apache.tomcat.util.codec.binary.Base64.decodeBase64(model.getBase64());
		
		try (OutputStream imageInFile = new FileOutputStream(inputFilePath)) {
			imageInFile.write(data);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
        File inputFile = new File(inputFilePath);
		
		Mat src = Imgcodecs.imread(inputFilePath);
		
		String xmlFile = "./cascade.xml";
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
		
		String outputFilePath = "./test_out." + model.getFormato();
		
		Imgcodecs.imwrite(outputFilePath, src);
		
        String base64OutputFile = "";
		
		File outputFile = new File(outputFilePath);
		
        try (FileInputStream imageOutFile = new FileInputStream(outputFile)) {
            // Reading a file from file system
            byte fileData[] = new byte[(int) outputFile.length()];
            imageOutFile.read(fileData);
            base64OutputFile = Base64.getEncoder().encodeToString(fileData);
        } catch (FileNotFoundException e) {
            System.out.println("File not found" + e);
        } catch (IOException ioe) {
            System.out.println("Exception while reading the file " + ioe);
        }
        
        inputFile.delete();
        outputFile.delete();
	    
		System.out.println("Program finished!");
	    		
		return new ImageResponseDTO(base64OutputFile, model.getFormato());		
	}
	
}
