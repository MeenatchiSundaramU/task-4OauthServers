
public class RefreshTokenModel 
{
	int uid,tokenindex;
    String clientId,refreshToken,scope;
	public RefreshTokenModel(int uid, int tokenindex, String clientId, String refreshToken, String scope) {
		this.uid = uid;
		this.tokenindex = tokenindex;
		this.clientId = clientId;
		this.refreshToken = refreshToken;
		this.scope = scope;
	}
	public int getUid() {
		return uid;
	}
	public void setUid(int uid) {
		this.uid = uid;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getRefreshToken() {
		return refreshToken;
	}
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	public int getTokenindex() {
		return tokenindex;
	}
	public void setTokenindex(int tokenindex) {
		this.tokenindex = tokenindex;
	}
}
