package com.myyj.sdk.tools.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class DatabaseDAO{
	private SQLiteDatabase db;
	
	public DatabaseDAO(SQLiteDatabase db){
		this.db = db;
	}
	
	/**获取指定区号的省份和地区名.*/
	public Map<String,String> queryAeraCode(String number){
		return queryNumber("0", number);
	}

	/**获取指定号码的省份和地区名。
	 * <code>select city_id from number_0 limit arg1,arg2.</code>
	 * arg1表示从第几行（行数从零开始）开始，arg2表示查询几行数据.*/
	public Map<String,String> queryNumber(String prefix, String center){
		if (center.isEmpty() || !isTableExists("number_" + prefix))
			return null;
		Log.d("caojingqi","prefix= " + prefix + "    center= " + center);
		
		int num = Integer.parseInt(center) - 1;
		String sql1 = "select city_id from number_" + prefix + " limit " + num + ",1";//空格不能少
		String sql2 = "select province_id from city where _id = (" + sql1 + ")";
		String sql = "select province,city from province,city where _id=("+sql1+")and id=("+sql2+")";

		return getCursorResult(sql);
	}
	
	/**返回查询结果集*/
	private Map<String, String> getCursorResult(String sql) {
		Cursor cursor = getCursor(sql);
		int col_len = cursor.getColumnCount();
		Log.d("caojingqi","col_len= " + col_len );
		Map<String, String> map = new HashMap<String, String>();
		
		while (cursor.moveToNext()){
			for (int i = 0; i < col_len; i++){
				String columnName = cursor.getColumnName(i);
				String columnValue = cursor.getString(cursor.getColumnIndex(columnName));
				if (columnValue == null)
					columnValue = "";
				Log.d("caojingqi","columnName= " + columnName + "    columnValue= " + columnValue);
				map.put(columnName, columnValue);
			}
		}
		return map;
	}

	private Cursor getCursor(String sql) {
		return  db.rawQuery(sql, null);
	}

	/**判断指定的表是否存在。*/
	public boolean isTableExists(String tableName){
		boolean result = false;
		if (tableName == null)
			return false;
		Cursor cursor = null;
		try{
			String sql = "select count(*) as c from sqlite_master where type='table' and " +
					"name = '" + tableName.trim() +"' ";
			cursor = db.rawQuery(sql, null);
			if (cursor.moveToNext()){
				int count = cursor.getInt(0);
				if (count > 0)
					result = true;
			}
		}catch(Exception e){
			
		}
		return result;
	}
	
	/**关闭数据库。*/
	public void closeDB(){
		if(db != null){
			db = null;
			db.close();
		}
	}
}