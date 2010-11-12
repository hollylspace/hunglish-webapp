package hu.mokk.hunglish.util;

import java.net.URISyntaxException;
import java.net.URL;

public class Utils {

	private static final String URI_PREFIX = "file:/";
	
	public static String convertPath(Class clazz, String path){		
		String result = null;
		try {
			URL url = clazz.getClassLoader().getResource(path);
			if (url == null){
				throw new IllegalStateException("resource not found:"+path);
			}
			result = url.toURI().toString();
			if (result.startsWith(URI_PREFIX)){
				result = result.substring(URI_PREFIX.length());
			}
		} catch (URISyntaxException e) {
			throw new RuntimeException("cannot convert path:"+path);
		}
		return result;
	}
	
}
