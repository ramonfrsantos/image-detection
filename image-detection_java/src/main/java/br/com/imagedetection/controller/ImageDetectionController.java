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
	
		// carrega a biblioteca opencv
		nu.pattern.OpenCV.loadShared();
		
		// declara se semelhanca detectada como falso
		boolean imageDetected = false;
		
		//caminho arquivo de entrada
		String inputFilePath = "./test_temp." + model.getFormato();
		
		// --- converte o base64 enviado no body da requisicao para arquivo ---
		byte[] data = org.apache.tomcat.util.codec.binary.Base64.decodeBase64(model.getBase64());
		
		try (OutputStream imageInFile = new FileOutputStream(inputFilePath)) {
			imageInFile.write(data);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
        File inputFile = new File(inputFilePath);
        
        //-----
		
        // le o arquivo de entrada e converte para o formato Mat
		Mat src = Imgcodecs.imread(inputFilePath);
		
		// caminho do arquivo resultado do treinamento, com especificacoes do modelo treinado
		String xmlFile = "./cascade.xml";
		
		CascadeClassifier cc = new CascadeClassifier(xmlFile);
		
		MatOfRect imgDetection = new MatOfRect();
		
		// detecta se a imagem de entrada da 'match' com o modelo
		cc.detectMultiScale(src, imgDetection);
		
		// se acha alguma semelhanca, seta o boolean para true
		if(imgDetection.toArray().length > 0) {
			imageDetected = true;
		}
		
		System.out.println("Image detected: " + imageDetected);
		
		// cria retangulos para indicar as semelhancas na imagem
		for(Rect rect: imgDetection.toArray()) {
			Imgproc.rectangle(src, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y+rect.height), new Scalar(0,0,255), 3);
		}
		
		// --- escreve na imagem de saida a imagem pos-analise
		String outputFilePath = "./test_out." + model.getFormato();
		
		Imgcodecs.imwrite(outputFilePath, src);
		//----
		
		// --- converte o arquivo de saida em base64 para retorno
        String base64OutputFile = "";
		
		File outputFile = new File(outputFilePath);
		
        try (FileInputStream imageOutFile = new FileInputStream(outputFile)) {
            // lendo arquivo do sistema
            byte fileData[] = new byte[(int) outputFile.length()];
            imageOutFile.read(fileData);
            base64OutputFile = Base64.getEncoder().encodeToString(fileData);
        } catch (FileNotFoundException e) {
            System.out.println("File not found" + e);
        } catch (IOException ioe) {
            System.out.println("Exception while reading the file " + ioe);
        }
        //----
        
        // exclui os arquivos temporarios criados        
        inputFile.delete();
        outputFile.delete();
	    
		System.out.println("Program finished!\n");
		
		// retorna a imagem criada pos-analise	    		
		return new ImageResponseDTO(base64OutputFile, model.getFormato());		
	}
}