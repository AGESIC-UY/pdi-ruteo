package uy.gub.agesic.pdi.backoffice.utiles.ui.components;

import java.io.Serializable;

public class CustomSelectOption implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String text;
	private Object value;

	public CustomSelectOption(String text, Object value) {
		this.text = text;
		this.value = value;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
}