package com.binarythought.jdbcjson;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager; 
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Types;
import java.text.DateFormat;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.google.gson.stream.JsonWriter;


public class JDBCJSON
{
	private Properties properties;


	public JDBCJSON()
	{
		properties = new Properties();
	}


	public void load(String path) throws Exception
	{
		FileInputStream stream = new FileInputStream(path);
		properties.load(stream);
		stream.close();
	}


	public String[] list()
	{
		HashSet<String> jobs = new HashSet<String>();
		for(String name : properties.stringPropertyNames()){
			int dot = name.lastIndexOf('.');
			if(dot > 0){ jobs.add(name.substring(0, dot)); }
		}

		return jobs.toArray(new String[0]);
	}

	
	public void run(String job, Set<String> warnings) throws Exception
	{
		DateFormat dateformat = DateFormat.getDateInstance(DateFormat.SHORT);
		DateFormat timeformat = DateFormat.getTimeInstance(DateFormat.SHORT);
		DateFormat datetimeformat = DateFormat.getDateTimeInstance(
			DateFormat.SHORT, DateFormat.SHORT
		);

		String url = properties.getProperty(job + ".url");

		String sql = properties.getProperty(job + ".sql");
		if(sql == null){ sql = "SELECT * FROM " + job; }

		String out = properties.getProperty(job + ".out");
		if(out == null){ out = job + ".json"; }

		String driver = properties.getProperty(job + ".driver");
		if(driver != null){ Class.forName(driver); }

		Connection conn = null;
		JsonWriter writer = null;
		try {
			conn = DriverManager.getConnection(url);

			Statement st = conn.createStatement(
				ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY
			);

			ResultSet rs = st.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			int columns = rsmd.getColumnCount();

			writer = new JsonWriter(new FileWriter(out, false));
			writer.beginArray();
			while(rs.next()){
				writer.beginObject();
				for(int i = 1; i <= columns; i++){
					switch(rsmd.getColumnType(i)){
						case Types.CHAR:
						case Types.LONGVARCHAR:
						case Types.LONGNVARCHAR:
						case Types.NCHAR:
						case Types.NVARCHAR:
						case Types.VARCHAR:
							writer.name(rsmd.getColumnName(i));
							writer.value(rs.getString(i));
						break;

						case Types.NULL:
							writer.name(rsmd.getColumnName(i));
							writer.nullValue();
						break;

						case Types.BIT:
						case Types.BOOLEAN:
							writer.name(rsmd.getColumnName(i));
							writer.value(rs.getBoolean(i));
						break;

						case Types.BIGINT:
						case Types.INTEGER:
						case Types.SMALLINT:
						case Types.TINYINT:
							writer.name(rsmd.getColumnName(i));
							writer.value(rs.getLong(i));
						break;

						case Types.REAL:
							writer.name(rsmd.getColumnName(i));
							writer.value(rs.getFloat(i));
						break;

						case Types.DOUBLE:
						case Types.FLOAT:
							writer.name(rsmd.getColumnName(i));
							writer.value(rs.getDouble(i));
						break;

						case Types.DECIMAL:
						case Types.NUMERIC:
							writer.name(rsmd.getColumnName(i));
							writer.value(rs.getBigDecimal(i));
						break;

						case Types.DATE:
							writer.name(rsmd.getColumnName(i));
							if(rs.getDate(i) == null){
								writer.nullValue();
							} else {
								writer.value(dateformat.format(rs.getDate(i)));
							}
						break;

						case Types.TIME:
							writer.name(rsmd.getColumnName(i));
							if(rs.getTime(i) == null){
								writer.nullValue();
							} else {
								writer.value(timeformat.format(rs.getTime(i)));
							}
						break;

						case Types.TIMESTAMP:
							writer.name(rsmd.getColumnName(i));
							if(rs.getTimestamp(i) == null){
								writer.nullValue();
							} else {
								writer.value(datetimeformat.format(rs.getTimestamp(i)));
							}
						break;

						default:
							if(warnings != null){
								warnings.add(
									"Unsupported column, " + rsmd.getColumnName(i) + 
									", type " + rsmd.getColumnTypeName(i)
								);
							}
						break;
					}
				}
				writer.endObject();
			}
			writer.endArray();

		} finally {
			try { writer.close(); }
			catch(Exception ex){ }

			try { conn.close(); }
			catch(Exception ex){ }
		}
	}


	public static void main(String[] args)
	{
		boolean debug = false;
		String properties_path = null;

		for(int i = 0; i < args.length; i++){
			if("-d".equals(args[i])){
				debug = true;
			} else if(i == (args.length - 1)){
				properties_path = args[i];
			} else {
				System.out.println("Unrecognized option: " + args[i]);
				break;
			}
		}

		if(properties_path == null){
			System.out.println("Usage:");
			System.out.println("\tjava -jar jdbcjson.jar [-d] myfile.properties");
			System.out.println("Options:");
			System.out.println("\t-d\tDebug mode. Prints warnings and more error detail.");
			System.exit(1);
		}

		JDBCJSON jdbcjson = new JDBCJSON();

		try {
			jdbcjson.load(properties_path);
		} catch(Exception ex){
			System.out.print("Error while loading " + args[0] + ": ");
			System.out.println(ex.getMessage());
			if(debug){ ex.printStackTrace(); }
			System.exit(1);
		}

		String[] jobs = jdbcjson.list();
		for(String job : jobs){
			try {
				if(debug){
					HashSet<String> warnings = new HashSet<String>();
					jdbcjson.run(job, warnings);

					for(String warning : warnings){
						System.out.println("Warning: " + warning);
					}
				} else {
					jdbcjson.run(job, null);
				}
			} catch(Exception ex){
				System.out.print("Error while running job " + job + ": ");
				System.out.println(ex.getMessage());
				if(debug){ ex.printStackTrace(); }
				System.exit(1);
			}
		}

		System.exit(0);
	}
}
