package zx.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigUtil {
	private Properties props = new Properties();
	private String propFileName;

	private ConfigUtil() {
	}

	public ConfigUtil(String propFileName) {
		this.propFileName = propFileName;
		InputStream ips = ConfigUtil.class.getClassLoader()
				.getResourceAsStream(this.propFileName);
		try {
			props.load(ips);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	public String getValue(String key) {
		return props.getProperty(key);
	}

//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		System.out.println(new ConfigUtil("fruit.properties"). getValue("qq"));
//	}

}
