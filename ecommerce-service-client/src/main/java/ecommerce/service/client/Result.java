package ecommerce.service.client;

import java.io.Serializable;

public class Result<T> implements Serializable {
	private static final long serialVersionUID = -8906331159333838057L;

	public enum Code {
		OK(0), Error(-1);
		public int value=0;
		private Code(int s){this.value=s;}
	}

	private int code;
	private String message;
	private T data;
	
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public T getData() {
		return data;
	}
	public void setData(T data) {
		this.data = data;
	}

}
