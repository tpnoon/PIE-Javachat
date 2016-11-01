package Microgear;

public interface MicrogearEventListener {
	public void onConnect();
	public void onMessage(String topic,String message);
	public void onPresent(String token);
	public void onAbsent(String token);
	public void onDisconnect();
	public void onError(String error);
}
