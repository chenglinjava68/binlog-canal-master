package com.tinyv.binlog;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.Message;
import com.tinyv.binlog.utils.ConfigUtils;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.alibaba.otter.canal.protocol.CanalEntry.RowChange;
import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.client.*;

public class CanalClientMain {

	private static Logger logger = Logger.getLogger(CanalClientMain.class);

	public static void main(String[] args) {
		CanalConnector connector = CanalConnectors.newSingleConnector(
				new InetSocketAddress(ConfigUtils.CANAL_HOST, ConfigUtils.CANAL_PORT), ConfigUtils.CANAL_DATABASE_NAME,
				ConfigUtils.MYSQL_USERNAME, ConfigUtils.MYSQL_PASSWORD);
		int batchSize = 1000;
		int emptyCount = 0;  
		try {  
            connector.connect();  
            connector.subscribe(ConfigUtils.CANAL_SUBSCRIBE);
            connector.rollback();  
            int totalEmtryCount = 1200;  
            while (emptyCount < totalEmtryCount) {  
                Message message = connector.getWithoutAck(batchSize); // 获取指定数量的数据  
                long batchId = message.getId();  
                int size = message.getEntries().size();  
                if (batchId == -1 || size == 0) {  
                    emptyCount++;  
                    logger.info("current emptyCount is "+emptyCount);
                    try {  
                        Thread.sleep(1000);  
                    } catch (InterruptedException e) {  
                        logger.error("线程休眠一样"+e.toString());
                    }  
                } else {  
                    emptyCount = 0;  
                    // System.out.printf("message[batchId=%s,size=%s] \n", batchId, size);  
                    try {
						printEntry(message.getEntries());
					} catch (Exception e) {
						 connector.rollback(batchId); // 处理失败, 回滚数据  
					}  
                }  
                connector.ack(batchId); // 提交确认  
                
            }  
  
            System.out.println("empty too many times, exit");  
        } finally {  
            connector.disconnect();  
        }  
	}

	private static void printEntry(List<Entry> entrys) throws Exception{
		for (CanalEntry.Entry entry : entrys) {
			if ((entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN)
					|| (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND)) {
				continue;
			}
			RowChange rowChage = null;
			try {
				rowChage = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
			} catch (Exception e) {
				logger.error("ERROR ## parser of eromanga-event has an error , data:" + entry.toString());
				throw new RuntimeException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(),e);
			}

			EventType eventType = rowChage.getEventType();
			Map<String, String> mapShare = InitMap(entry, eventType);
			
			for (CanalEntry.RowData rowData : rowChage.getRowDatasList())
				if (eventType == CanalEntry.EventType.DELETE) { // 删除数据
					Map<String, String> printColumn = printColumn(rowData.getBeforeColumnsList());
					printColumn.putAll(mapShare);
					logger.info(JSON.toJSONString(printColumn));
					KafkaProducer.sendMsg(ConfigUtils.KAFKA_TOPIC, JSON.toJSONString(printColumn));
				} else if (eventType == CanalEntry.EventType.INSERT) { // 插入数据
					Map<String, String> printColumn = printColumn(rowData.getAfterColumnsList());
					printColumn.putAll(mapShare);
					logger.info(JSON.toJSONString(printColumn));
					KafkaProducer.sendMsg(ConfigUtils.KAFKA_TOPIC, JSON.toJSONString(printColumn));
				} else {
					/* 更新数据前 */
					Map<String, String> printColumnBefore = printColumn(rowData.getBeforeColumnsList());
					printColumnBefore.putAll(mapShare);
					printColumnBefore.put("modify", "before");
					logger.info(JSON.toJSONString(printColumnBefore));
					KafkaProducer.sendMsg(ConfigUtils.KAFKA_TOPIC, JSON.toJSONString(printColumnBefore));

					/* 更新数据后 */
					Map<String, String> printColumnAfter = printColumn(rowData.getAfterColumnsList());
					printColumnAfter.putAll(mapShare);
					printColumnAfter.put("modify", "after");
					logger.info(JSON.toJSONString(printColumnAfter));
					KafkaProducer.sendMsg(ConfigUtils.KAFKA_TOPIC, JSON.toJSONString(printColumnAfter));
				}
		}
	}

	/**
	 * 初始化Map
	 * 
	 * @param entry
	 * @param eventType
	 * @return
	 */
	private static Map<String, String> InitMap(CanalEntry.Entry entry, EventType eventType) {
		Map<String, String> mapShare = new HashMap<String, String>();
		mapShare.put("logfilename_ext", entry.getHeader().getLogfileName().toLowerCase());
		mapShare.put("logfileoffset_ext", String.valueOf(entry.getHeader().getLogfileOffset()).toLowerCase());
		mapShare.put("databasename_ext", entry.getHeader().getSchemaName().toLowerCase());
		mapShare.put("tablename_ext", entry.getHeader().getTableName().toLowerCase());
		mapShare.put("eventtype_ext", eventType.toString().toLowerCase());
		mapShare.put("topicname_ext", ConfigUtils.KAFKA_TOPIC.toLowerCase());
		mapShare.put("channeltype_ext", ConfigUtils.CHANNEL_TYPE.toLowerCase());
		return mapShare;
	}

	/**
	 * 真实数据封装Map
	 * 
	 * @param columns
	 * @return
	 */
	private static Map<String, String> printColumn(List<CanalEntry.Column> columns) {
		Map<String, String> map = new HashMap<String, String>();
		for (CanalEntry.Column column : columns) {
			if(StringUtils.isNoneBlank(column.getValue())){
				map.put(column.getName().toLowerCase(), column.getValue().toLowerCase());
			}else{
				map.put(column.getName().toLowerCase(),null);
			}
		}
		return map;
	}
}