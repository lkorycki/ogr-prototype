package ogr;

import java.io.File;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class OCR {
	
	public static String ocr(String imagePath, String lang) {
		File imageFile = new File(imagePath);
        Tesseract instance = new Tesseract();
        instance.setLanguage(lang);
        try {
            return instance.doOCR(imageFile);
        } catch (TesseractException e) {
            System.err.println(e.getMessage());
            return null;
        }
	}
	
	public static String ocr(String imagePath) {
		return ocr(imagePath, "ogr");
	}

	public static void main(String[] args) {
		String result = OCR.ocr("src/main/resources/ocr_samples/test3.bmp");
		System.out.println(result);
	}

}
