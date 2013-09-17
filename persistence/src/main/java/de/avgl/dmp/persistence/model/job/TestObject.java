package de.avgl.dmp.persistence.model.job;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TestObject {
	
	private String message = "Hello World";

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
