
public class ReuseLoginValidModel 
{
       String email,pass,clientid,redirecturi,scope;

	public ReuseLoginValidModel(String email, String pass, String clientid, String redirecturi, String scope) {
		this.email = email;
		this.pass = pass;
		this.clientid = clientid;
		this.redirecturi = redirecturi;
		this.scope = scope;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public String getClientid() {
		return clientid;
	}

	public void setClientid(String clientid) {
		this.clientid = clientid;
	}

	public String getRedirecturi() {
		return redirecturi;
	}

	public void setRedirecturi(String redirecturi) {
		this.redirecturi = redirecturi;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}
}
