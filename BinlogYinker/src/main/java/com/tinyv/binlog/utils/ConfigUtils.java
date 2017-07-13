package com.tinyv.binlog.utils;

public class ConfigUtils {

	
	/* mysql相关配置文件 */
	public static final String MYSQL_USERNAME=PropertiesUtils.config.getString("mysql.username"); 
	public static final String MYSQL_PASSWORD=PropertiesUtils.config.getString("mysql.password");
	
	/* canal相关配置文件*/
	public static final String CANAL_HOST=PropertiesUtils.config.getString("canal.host");
	public static final int CANAL_PORT=Integer.parseInt(PropertiesUtils.config.getString("canal.port"));
	public static final String CANAL_DATABASE_NAME=PropertiesUtils.config.getString("canal.database_name");
	public static final String CANAL_SUBSCRIBE=PropertiesUtils.config.getString("canal.subscribe");
	
	/*kafka相关*/
	public static final String KAFKA_TOPIC=PropertiesUtils.config.getString("kafka.topic");
	public static final String ZOOKEEPER_CONNECT=PropertiesUtils.config.getString("zookeeper.connect");
	public static final String METADATA_BROKER_LIST=PropertiesUtils.config.getString("metadata.broker.list");
	
	/*
	 * 渠道
	 */
	public static final String CHANNEL_TYPE=PropertiesUtils.config.getString("channel.type");
	
	
	public static void main(String[] args) {
		System.out.println(ConfigUtils.CANAL_SUBSCRIBE);
		System.out.println(ConfigUtils.ZOOKEEPER_CONNECT);
	}
	
}
