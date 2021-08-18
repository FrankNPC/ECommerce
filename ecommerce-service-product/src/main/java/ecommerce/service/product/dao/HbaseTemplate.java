package ecommerce.service.product.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptor;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptorBuilder;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.TableDescriptor;
import org.apache.hadoop.hbase.client.TableDescriptorBuilder;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HbaseTemplate {
	
    private static final Logger logger = LoggerFactory.getLogger(HbaseTemplate.class);

    private Admin hbaseAdmin;
    private Connection connection;
    public HbaseTemplate(Map<String, String> config) {
    	try {
	    	Configuration hbaseConfiguration = HBaseConfiguration.create();
	        for(Map.Entry<String, String> map : config.entrySet()){
	        	hbaseConfiguration.set(map.getKey(), map.getValue());
	        }
	        connection = ConnectionFactory.createConnection(hbaseConfiguration);
	        hbaseAdmin = connection.getAdmin();
    	}catch(Exception e) {
    		logger.error(e.getMessage());
    	}
    }
    
	public void createTableWithFamilies(String tableName, String... families) throws IOException {
	    if (!hbaseAdmin.tableExists(TableName.valueOf(tableName))) {
            List<ColumnFamilyDescriptor> familyDescriptors = Arrays.stream(families).map(family->ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes(family)).build()).collect(Collectors.toList());
            TableDescriptor tableDescriptor = TableDescriptorBuilder.newBuilder(TableName.valueOf(tableName))
                    .setColumnFamilies(familyDescriptors)
                    .build();
	    	hbaseAdmin.createTable(tableDescriptor);
	    }
	}
	public void deleteTable(String tableName) throws IOException{
		hbaseAdmin.disableTable(TableName.valueOf(tableName));
		hbaseAdmin.deleteTable(TableName.valueOf(tableName));
	}
	public void saveOrUpdateByRowKey(String tableName, String rowKey, String family, String qualifier, String value) throws IOException{
		Table table = connection.getTable(TableName.valueOf(tableName));
		Put put = new Put(Bytes.toBytes(rowKey));
		put.addColumn(Bytes.toBytes(family),Bytes.toBytes(qualifier), Bytes.toBytes(value));
		table.put(put);
	}
	public void saveOrUpdateByRowKeys(String tableName, String rowKey, String family, String qualifiers[], String[][] values) throws IOException{
		Table table = connection.getTable(TableName.valueOf(tableName));
		Put put = new Put(Bytes.toBytes(rowKey));
        for(String[] value : values) {
            for(int i=0;i<qualifiers.length;i++){
                put.addColumn(Bytes.toBytes(family), Bytes.toBytes(qualifiers[i]), Bytes.toBytes(value[i]));
            }
        }
		table.put(put);
	}
	public void deleteByRowKey(String tableName, String rowKey, String family, String qualifier) throws IOException{
		Table table = connection.getTable(TableName.valueOf(tableName));
		Delete delete = new Delete(Bytes.toBytes(rowKey));
		if (qualifier!=null&&qualifier.isEmpty()) {
			delete.addColumn(Bytes.toBytes(family),Bytes.toBytes(qualifier));
		}
		table.delete(delete);
	}
	public String getByRowKey(String tableName, String rowKey, String family, String qualifier) throws IOException{
		Table table = connection.getTable(TableName.valueOf(tableName));
		Get get = new Get(Bytes.toBytes(rowKey));
		Result getResult = table.get(get);
		return Bytes.toString(getResult.getValue(Bytes.toBytes(family), Bytes.toBytes(qualifier)));
	}
	public List<String> queryByRowKey(String tableName, String rowKey, String family, String qualifier) throws IOException{
		Table table = connection.getTable(TableName.valueOf(tableName));
		Scan scan = new Scan();
		if (rowKey!=null&&!rowKey.isEmpty()) {
			Get get = new Get(Bytes.toBytes(rowKey));
			scan = new Scan(get);
		}
		ResultScanner scanner = table.getScanner(scan);
		List<String> returnList = new ArrayList<String>();
		for(Result result : scanner){
			returnList.add(Bytes.toString(result.getValue(Bytes.toBytes(family), Bytes.toBytes(qualifier))));
		}
		return returnList;
	}
}
