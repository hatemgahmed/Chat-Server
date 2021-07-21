import java.net.Socket;

public class NetSocket extends Socket{
	private String clientName;

	String getClientName() {
		return clientName;
	}
	protected NetSocket(String n) {
		super();
		this.clientName=n;
	}
	void setClientName(String clientName) {
		this.clientName = clientName;
	}
	
}
