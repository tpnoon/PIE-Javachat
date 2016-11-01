package Microgear;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import org.json.JSONException;
import org.json.JSONObject;

import Decoder.BASE64Encoder;

public class Microgear implements MqttCallback {

	private JSONObject add_microgear1 = new JSONObject();
	private JSONObject add_microgear2 = new JSONObject();
	private JSONObject microgear = new JSONObject();
	private JSONObject cache_microgear;
	private static String token;
	private static String microgear_key;
	private static String microgear_secret;
	private static String microgear_revokecode;
	private static String mqttuser;
	private static String mqttpassword;
	private static MqttClient mqtt;
	private static BufferedReader bufferedReader;
	private static String appid;
	private static String key;
	private static String secret;
	private static String authorize_callback;
	private static String Request_token;
	private static Vector<String> vec_subscribe = new Vector<String>();
	private static List<String> republish_topic = new ArrayList<String>();
	private static List<String> republish_message = new ArrayList<String>();
	private static String vec_setalias;
	private static String status = "0";
	private final String broker = "tcp://gb.netpie.io:1883";
	private final String resettoken_url = "http://ga.netpie.io:8080/api/revoke/";
	private final String endpoint = "pie://gb.netpie.io:1883";
	private final String dir = "microgear.cache";
	private boolean status_vac = true;
	private int qos = 0;
	private MicrogearEventListener EventListener;

	private class PublishThread extends Thread {
		String Topic;
		MqttMessage Message;
		boolean Retainde = false;

		public void run() {
			if (Retainde) {
				try {
					mqtt.publish(this.Topic, this.Message.getPayload(), 0, Retainde);
				} catch (MqttPersistenceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				try {
					mqtt.publish(this.Topic, this.Message);
				} catch (MqttPersistenceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

		public PublishThread(String topic, MqttMessage message, boolean retainde) {
			this.Topic = topic;
			this.Message = message;
			this.Retainde = retainde;
		}

		public PublishThread(String topic, MqttMessage message) {
			this.Topic = topic;
			this.Message = message;
		}
	}
	
	private class DisconnectThread extends Thread {
		public void run() {
			try {
				mqtt.disconnect();
				EventListener.onDisconnect();
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	
	private class SubscribeThread extends Thread {
		String Topic;
		public void run() {
			try {
				mqtt.subscribe(Topic);
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		public SubscribeThread(String topic){
			this.Topic = topic;
		}
	}
	
	private class UnsubscribeThread extends Thread {
		String Topic;
		public void run() {
			try {
				mqtt.unsubscribe(Topic);
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		public UnsubscribeThread(String topic){
			this.Topic = topic;
		}
	}

	public void setCallback(MicrogearEventListener EventListener) {
		this.EventListener = EventListener;
	}

	public void connect(String input_appID, String input_Key, String input_Secret) {
		appid = input_appID;
		key = input_Key;
		secret = input_Secret;
		authorize_callback = "scope=&appid=" + appid + "&mgrev=JVM1a&verifier=JVM1a";
		this.GetItem();
	}

	private void GetItem() {
		StringBuilder sb = new StringBuilder();
		String line;
		FileInputStream fis;
		try {
			
		//	fis = getClass().getClassLoader().getResourceAsStream(dir);
			fis = new FileInputStream(dir);
			bufferedReader = new BufferedReader(new InputStreamReader(fis));
			while ((line = bufferedReader.readLine()) != null) {
				sb.append(line);
			}
			cache_microgear = new JSONObject(sb.toString());
			microgear_key = cache_microgear.getJSONObject("_").getString("key");
			token = cache_microgear.getJSONObject("_").getJSONObject("accesstoken").getString("token");
			microgear_secret = cache_microgear.getJSONObject("_").getJSONObject("accesstoken").getString("secret");
			microgear_revokecode = cache_microgear.getJSONObject("_").getJSONObject("accesstoken")
					.getString("revokecode");
			Check();
		} catch (FileNotFoundException e) {
			Build_microgear();
			Reconnect();
		} catch (JSONException e) {
			Build_microgear();
			Reconnect();
		} catch (Exception e) {
		}
	}

	private void Check() throws Exception {
		if (key.equals(microgear_key)) {
			if (getStatus().equals("0")) {
				Connect_Broker();
			}
		} else if (!microgear_key.equals(key) && !microgear_key.isEmpty()) {
			Build_microgear();
			if (getStatus().equals("0")) {
				Resettoken();
				Reconnect();
			}
		} else if (microgear_key.isEmpty()) {
			Writefile(microgear_secret, token, microgear_revokecode);
			Reconnect();
		}
	}

	private void Create(String App_ID, String App_KEY, String App_SECRET) {
		try {
			Request_token = new request().OAuth(key, secret, authorize_callback);
			Access_Token(Request_token);
		} catch (Exception e) {
		}
	}

	private void Access_Token(String Request_token) {
		Map<String, String> request;
		Map<String, String> access;
		try {
			request = splitQuery_Request(Request_token);
			String request_token = request.get("oauth_token");
			String request_token_secret = request.get("oauth_token_secret");
			JSONObject Request_Access_token = new access().OAuth(key, secret, request_token, request_token_secret);
			access = splitQuery_Access(Request_Access_token);
			String access_token = access.get("oauth_token");
			String access_token_secret = access.get("oauth_token_secret");
			String revokecode = Signature(access_token_secret, access_token);
			
			
			//edit
		//	File checkfile = new File(getClass().getResource(dir).getFile());
			File checkfile = new File(dir);
			if (checkfile.exists()) {
				checkfile.setWritable(true);
				Writefile(access_token_secret, access_token, revokecode);
			} else {
				Writefile(access_token_secret, access_token, revokecode);
			}
		} catch (Exception e) {
		}
	}

	private String Signature(String access_token_secret, String access_token) {
		String hkey = access_token_secret + "&" + secret;
		String hash = "";
		SecretKeySpec keySpec = new SecretKeySpec(hkey.getBytes(), "HmacSHA1");
		Mac mac;
		try {
			mac = Mac.getInstance("HmacSHA1");
			mac.init(keySpec);
			byte[] result = mac.doFinal(access_token.getBytes());
			BASE64Encoder encoder = new BASE64Encoder();
			hash = encoder.encode(result);
		} catch (NoSuchAlgorithmException e) {
		} catch (InvalidKeyException e) {
		}
		return hash.toString();
	}

	private Map<String, String> splitQuery_Request(String request_token) throws UnsupportedEncodingException {
		Map<String, String> query_pairs = new LinkedHashMap<String, String>();
		String[] pairs = request_token.toString().split("&");
		int i = 0;
		for (String pair : pairs) {
			int idxs = pair.indexOf("o");
			int idxe = pair.indexOf("=");
			if (i == 2) {
				query_pairs.put(pair.substring(idxs, idxe), pair.substring(idxe + 1, pair.length() - 2));
			} else {
				query_pairs.put(pair.substring(idxs, idxe), pair.substring(idxe + 1));
			}
			i++;
		}
		return query_pairs;
	}

	private Map<String, String> splitQuery_Access(JSONObject request_token) throws UnsupportedEncodingException {
		Map<String, String> query_pairs = new LinkedHashMap<String, String>();
		String[] pairs = request_token.toString().split("&");
		int i = 0;
		for (String pair : pairs) {
			int idxs = pair.indexOf("o");
			int idxe = pair.indexOf("=");
			if (i == 3) {
				query_pairs.put(pair.substring(idxs, idxe), pair.substring(idxe + 1, pair.length() - 2));
			} else if (i != 0) {
				query_pairs.put(pair.substring(idxs, idxe), pair.substring(idxe + 1));
			}
			i++;
		}
		return query_pairs;
	}

	private void Connect_Broker() throws Exception {
		String hkey = microgear_secret + "&" + secret;
		mqttuser = appid + "%" + Math.toIntExact(new Date().getTime() / 1000);
		SecretKeySpec keySpec = new SecretKeySpec(hkey.getBytes(), "HmacSHA1");
		Mac mac;
		try {
			mac = Mac.getInstance("HmacSHA1");
			mac.init(keySpec);
			String stoken = token + "%" + mqttuser;
			byte[] result = mac.doFinal(stoken.getBytes());
			BASE64Encoder encoder = new BASE64Encoder();
			mqttpassword = encoder.encode(result);// password
			String clientId = token;
			MemoryPersistence persistence = new MemoryPersistence();
			mqtt = new MqttClient(broker, clientId, persistence);
			mqtt.setCallback(this);
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			connOpts.setUserName(mqttuser);
			connOpts.setKeepAliveInterval(15);
			connOpts.setMqttVersion(3);
			connOpts.setPassword(mqttpassword.toCharArray());
			mqtt.connect(connOpts);
			SubscribeThread subscribePresent = new SubscribeThread("/" + appid + "/" + "&present");
			Thread subscribePresentThread = new Thread(subscribePresent);
			subscribePresentThread.start();
			SubscribeThread subscribeAbsent = new SubscribeThread("/" + appid + "/" + "&absent");
			Thread subscribeAbsentThread = new Thread(subscribeAbsent);
			subscribeAbsentThread.start();
			Resubscribe();
		} catch (InvalidKeyException e) {
		} catch (MqttException e) {
			if (e.getReasonCode() == 4) {
				setStatus("0");
				EventListener.onError(e.getMessage());
				Build_microgear();
				Reconnect();
			} else if (e.getReasonCode() == 5) {
				EventListener.onError("Error: Thing disable.");
				Thread.sleep(2000);
				Reconnect();
			} else {
				EventListener.onError("Error: Please check your internet.");
				Thread.sleep(2000);
				Reconnect();
			}
		}
	}

	private void Resubscribe() {
		status_vac = false;
		try {
			new request().OAuth(key, secret, authorize_callback);
			EventListener.onConnect();
		} catch (Exception e1) {
		}
		if (vec_setalias != null) {
			Setalias(vec_setalias);
		}
		for (int i = 0; i < vec_subscribe.size(); i++) {
			Subscribe(vec_subscribe.get(i));
		}
		for (int i = 0; i < republish_topic.size(); i++) {
			MqttMessage message = new MqttMessage();
			message.setQos(qos);
			message.setPayload(republish_message.get(i).getBytes());
			if (Checktopic(republish_topic.get(i)) != null) {
				PublishThread publisher = new PublishThread(republish_topic.get(i), message);
				Thread PublishThread = new Thread(publisher);
				PublishThread.start();
			}
		}
		republish_topic.removeAll(republish_topic);
		republish_message.removeAll(republish_message);
		setStatus("0");
		status_vac = true;
	}

	private void Build_microgear() {
		Create(appid, key, secret);
	}

	private void Reconnect() {
		try {
			connect(appid, key, secret);
		} catch (Exception e) {
		}
	}

	private void Writefile(String secret, String token, String revoke) {
		try {
			add_microgear1.put("key", key);
			add_microgear1.put("requesttoken", "null");
			add_microgear2.put("token", token);
			add_microgear2.put("secret", secret);
			add_microgear2.put("endpoint", endpoint);
			add_microgear2.put("revokecode", revoke);
			add_microgear1.put("accesstoken", add_microgear2);
			microgear.put("_", add_microgear1);
			//edit
			FileWriter write = new FileWriter(dir, false);
		//	FileWriter write = new FileWriter(getClass().getResource(dir).getFile(), false);
			write.write(microgear.toString());
			write.flush();
			write.close();
			//edit
			File checkfile = new File(dir);
		//	File checkfile = new File(getClass().getResource(dir).getFile());
			checkfile.setWritable(false);
		} catch (JSONException e) {
		} catch (IOException e) {
		}
	}

	private void Resettoken() {
		microgear_revokecode = microgear_revokecode.replaceAll("/", "_");
		final String Request_url = resettoken_url + token + "/" + microgear_revokecode;
		try {
			URLConnection connection = new URL(Request_url).openConnection();
			InputStream re = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(re));
			StringBuilder response = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null) {
				response.append(line);
			}
			rd.close();
		} catch (Exception e) {
		}
	}

	public void Disconnect() {
		
		DisconnectThread Disconnect = new DisconnectThread();
		Thread DisconnectThread = new Thread(Disconnect);
		DisconnectThread.start();
	}

	private boolean Checkname(String Topic) {
		Pattern p = Pattern.compile("[^A-Za-z0-9_]");
		if (!p.matcher(Topic).find() && !Topic.isEmpty()) {
			return true;
		} else {
			System.err.println("Error: name must be A-Z,a-z,0-9,_,& and must not spaces. ");
			System.exit(0);
			return false;
		}
	}

	private String Checktopic(String Topic) {
		Pattern p = Pattern.compile("[^A-Za-z0-9/_]");
		if (!p.matcher(Topic).find() && !Topic.isEmpty()) {
			Pattern p1 = Pattern.compile("[\\._/]");
			if (p1.matcher(Topic).find()) {
				return Topic.substring(1);
			} else {
				return Topic;
			}
		} else {
			System.err.println("Error: name must be A-Z,a-z,0-9,_,& and must not spaces.");
			System.exit(0);
			return null;
		}
	}

	public void Publish(String Topic, String Message, boolean Retainde) {
		try {
			MqttMessage message = new MqttMessage();
			message.setQos(qos);
			message.setPayload(Message.getBytes());
			if (Checktopic(Topic) != null) {
				PublishThread publisher = new PublishThread("/" + appid + "/" + Checktopic(Topic), message, Retainde);
				Thread PublishThread = new Thread(publisher);
				PublishThread.start();
			}
		} catch (NullPointerException e) {
			if (status_vac) {
				republish_topic.add("/" + appid + "/" + Checktopic(Topic));
				republish_message.add(Message);
			}
		}
	}

	public void Publish(String Topic, String Message) {
		try {
			MqttMessage message = new MqttMessage();
			message.setQos(qos);
			message.setPayload(Message.getBytes());
			if (Checktopic(Topic) != null) {
				PublishThread publisher = new PublishThread("/" + appid + "/" + Checktopic(Topic), message);
				Thread PublishThread = new Thread(publisher);
				PublishThread.start();
			}
		} catch (NullPointerException e) {
			if (status_vac) {
				republish_topic.add("/" + appid + "/" + Checktopic(Topic));
				republish_message.add(Message);
			}
		}
	}

	public void Subscribe(String Topic) {
		try {
			if (Checktopic(Topic) != null) {
				SubscribeThread Subscribe = new SubscribeThread("/" + appid + "/" + Checktopic(Topic));
				Thread SubscribeThread = new Thread(Subscribe);
				SubscribeThread.start();
				if (status_vac) {
					vec_subscribe.add(Checktopic(Topic));
				}
			}
		} catch (NullPointerException e) {
			if (status_vac) {
				vec_subscribe.add(Topic);
			}
		}
	}

	public void Unsubscribe(String Topic) {
		try {
			if (Checktopic(Topic) != null) {
				UnsubscribeThread Unsubscribe = new UnsubscribeThread("/" + appid + "/" + Checktopic(Topic));
				Thread UnsubscribeThread = new Thread(Unsubscribe);
				UnsubscribeThread.start();
				vec_subscribe.remove(Checktopic(Topic));
			}
		} catch (NullPointerException e) {
			vec_subscribe.remove(Topic);
		}
	}

	public void Setalias(String Newalias) {
		try {
			MqttMessage message = new MqttMessage();
			message.setQos(qos);
			message.setPayload("".getBytes());
			if (Checkname(Newalias)) {
				PublishThread publish = new PublishThread("/" + appid + "/@setalias/" + Newalias, message);
				Thread PublishThread = new Thread(publish);
				PublishThread.start();
				if (status_vac) {
					vec_setalias = Newalias;
				}
			}
		} catch (NullPointerException e) {
			if (status_vac) {
				vec_setalias = Newalias;
			}
		}
	}

	public void Chat(String Name, String Message) {
		try {
			MqttMessage message = new MqttMessage();
			message.setQos(qos);
			message.setPayload(Message.getBytes());
			if (Checkname(Name)) {
				PublishThread publisher = new PublishThread("/" + appid + "/gearname/" + Name, message);
				Thread PublishThread = new Thread(publisher);
				PublishThread.start();
			}
		} catch (NullPointerException e) {
			if (status_vac) {
				if (Checkname(Name)) {
					republish_topic.add("/" + appid + "/gearname/");
					republish_message.add(Message);
				}
			}
		}
	}

	@Override
	public void connectionLost(Throwable arg0) {
		Reconnect();
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
	}

	@Override
	public void messageArrived(String Topic, MqttMessage Message) {
		if (Topic.indexOf("&present") != -1) {
			EventListener.onPresent(Message.toString());
		} else if (Topic.indexOf("&absent") != -1) {
			EventListener.onAbsent(Message.toString());
		} else {
			EventListener.onMessage(Topic, Message.toString());

		}
	}

	/**
	 * @return the status
	 */
	static String getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 * @return
	 */
	static String setStatus(String status) {
		return Microgear.status = status;
	}

}
