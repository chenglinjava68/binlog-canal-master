package com.tinyv.binlog.utils;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class PropertiesUtils {

	public static PropertiesConfiguration config = null;

	static {
		try {
			AbstractConfiguration.setDefaultListDelimiter('-');  
			config = new PropertiesConfiguration("config.properties");
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

}
