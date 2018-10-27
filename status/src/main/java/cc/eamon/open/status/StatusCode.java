package cc.eamon.open.status;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Properties;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class StatusCode {
	
	/**
	 * 请求成功
	 */
	public static int SUCCESS=200;
	/**
	 * 简单请求成功消息
	 */
	public static String SUCCESS_MSG="请求成功";
	
	/**
	 * 请求失败
	 */
	public static int FAILED=701;
	/**
	 * 简单请求失败消息
	 */
	public static String FAILED_MSG="请求失败";
	
	/**
	 * 错误未识别
	 */
	public static int NOT_RECOGNIZED=1000;
	/**
	 * 错误未识别消息
	 */
	public static String NOT_RECOGNIZED_MSG="错误未识别";
	
	private int code; 
	
	private String msg;
	
	//拒绝实例化，保护结构	
	private StatusCode(){}
	
	private static HashMap<String, StatusCode> nameToCodeMap = new HashMap<>();

	
	static{
		Properties prop = new Properties();
		Resource resource=new ClassPathResource("status.properties");
		try{
			prop.load(new InputStreamReader(resource.getInputStream(), "UTF-8"));
			prop.stringPropertyNames().stream().forEachOrdered((e)->{
				String cm[] = prop.getProperty(e).split(",");
				
				if(cm.length<2)
					try {
						throw new Exception();
					} catch (Exception e1) {
						System.err.println("status.properties IO exception");
					}
				
				StatusCode code = new StatusCode();
				code.code = Integer.parseInt(cm[0]);
				code.msg = cm[1];
				
				
				switch(e){
				case "SUCCESS":
					SUCCESS = code.code;
					SUCCESS_MSG = code.msg;
					break;
				case "FAILD":
					FAILED = code.code;
					FAILED_MSG = code.msg;
					break;
				case "NOT_RECOGNIZED":
					NOT_RECOGNIZED = code.code;
					NOT_RECOGNIZED_MSG = code.msg;
					break;
				}
				nameToCodeMap.put(e, code);
			});
			
			
		}catch(Exception e){
			System.err.println("status.properties IO exception");
		}
	}
	
	public static Integer getCode(String name){
		StatusCode statusCode = nameToCodeMap.get(name);
		if(statusCode ==null){
			return NOT_RECOGNIZED;
		}
		else{
			return statusCode.code;
		}
	}
	
	public static String getMsg(String name){
		StatusCode statusCode = nameToCodeMap.get(name);
		if(statusCode ==null){
			return NOT_RECOGNIZED_MSG;
		}
		else{
			return statusCode.msg;
		}
	}
	
}