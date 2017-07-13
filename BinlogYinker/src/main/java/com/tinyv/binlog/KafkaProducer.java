package com.tinyv.binlog;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.util.Properties;

import com.tinyv.binlog.utils.ConfigUtils;

/**
 * kafka工具类
 * @author yangluan
 * 2017年6月8日下午4:02:55
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class KafkaProducer{
	String topic;
	public KafkaProducer(String topics){
		super();
		this.topic=topics;
	}
	static Producer producer = createProducer();
	public static void sendMsg(String topic,String data){
		producer.send(new KeyedMessage<String, String>(topic,data));
		//producer.close();    //屏蔽
	}

	public static Producer<Integer,String> createProducer(){
		Properties properties = new Properties();
		properties.put("zookeeper.connect",ConfigUtils.ZOOKEEPER_CONNECT);
		properties.put("serializer.class","kafka.serializer.StringEncoder");
        properties.put("metadata.broker.list",ConfigUtils.METADATA_BROKER_LIST);// 声明kafka broker
        return new Producer<Integer,String>(new ProducerConfig(properties));
	}
}
