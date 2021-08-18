package ecommerce.service.client.base;

public class User implements java.io.Serializable {

	private static final long serialVersionUID = 1821901254631880819L;

	private Long id;
	
	private String username;
	
	private String password;
	
	private String sessionId;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

}
