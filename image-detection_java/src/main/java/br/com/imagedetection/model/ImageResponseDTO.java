package br.com.imagedetection.model;

public class ImageResponseDTO {
	private String base64Convertido;
	private String formato;
	
	public ImageResponseDTO(String base64Convertido, String formato) {
		super();
		this.base64Convertido = base64Convertido;
		this.formato = formato;
	}

	public String getBase64Convertido() {
		return base64Convertido;
	}

	public void setBase64Convertido(String base64Convertido) {
		this.base64Convertido = base64Convertido;
	}

	public String getFormato() {
		return formato;
	}

	public void setFormato(String formato) {
		this.formato = formato;
	}
	
}
