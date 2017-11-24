package mvcModel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;


public class MySQLqueries 
{
	private Connection connectDB = null;
	private Statement statement = null;
	final private String host="127.0.0.1:3307";
	final private String user="root";
	final private String passwd="pass";
	private String dbName = "prediction";

	String table_attributes = "CREATE TABLE IF NOT EXISTS attributes (" 
			+ "ID_ATT INT NOT NULL AUTO_INCREMENT,"  
			+ "URI_ATT VARCHAR(200)," 
			+ "IS_ABOUT VARCHAR(200)," 
			+ "MIN_VALUE FLOAT,"
			+ "MAX_VALUE FLOAT,"
			+ "PRIMARY KEY(ID_ATT))";
	String table_tc = "CREATE TABLE IF NOT EXISTS target_class (" 
			+ "ID_TC INT NOT NULL AUTO_INCREMENT,"  
			+ "URI_TC VARCHAR(200)," 
			+ "PRIMARY KEY(ID_TC))";
	String table_gc = "CREATE TABLE IF NOT EXISTS global_context (" 
			+ "ID_GC INT NOT NULL,"  
			+ "URI_GC VARCHAR(200)," 
			+ "NB_IDENTICAL_PAIRS_INFERENCE INT," 
			+ "NB_IDENTICAL_PAIRS_MS INT," 
			+ "PRIMARY KEY(ID_GC))";
	String table_gc_relations = "CREATE TABLE IF NOT EXISTS global_context_relations (" 
			+ "ID_GC INT,"  
			+ "MORE_SPECIFIC_THAN INT" 
			+ ")";
	String table_tc_observations = "CREATE TABLE IF NOT EXISTS tc_observations (" 
			+ "ID_TC INT,"  
			+ "ID_ATT INT," 
			+ "VALUE FLOAT," 
			+ "MEASURE_UNIT VARCHAR(200),"
			+ "OBSERVATION_TYPE INT, "
			+ "SCALE VARCHAR(200))";
	String table_GC_observations = "CREATE TABLE IF NOT EXISTS global_context_observations (" 			
			+ "ID_GC_ATT INT,"
			+ "ID_GC INT,"
			+ "ID_ATT INT,"  
			+ "TOTAL_VALUE FLOAT,"
			+ "TOTAL_NB INT,"
			+ "PRIMARY KEY(ID_GC_ATT))";
	/*String table_pair_observations = "CREATE TABLE IF NOT EXISTS pair_observations (" 			
			+ "ID_TC_1 INT,"
			+ "ID_TC_2 INT,"
			+ "ID_GC INT,"  
			+ "ID_ATT INT," 
			+ "ERROR_RATE FLOAT)";*/
	String table_rules = "CREATE TABLE IF NOT EXISTS rules (" 
			+ "ID_RULE INT NOT NULL AUTO_INCREMENT," 
			+ "ID_GC INT,"  
			+ "ID_ATT INT," 
			+ "AVG_ERROR_RATE FLOAT,"
			+ "SUPPORT FLOAT, "
			+ "TOTAL_NB INT,"
			+ "PRIMARY KEY(ID_RULE))";

	public MySQLqueries(Boolean delete) throws ClassNotFoundException, SQLException
	{
		Class.forName("com.mysql.jdbc.Driver");
		/*if(delete == true)
		{
			dropSchema(this.dbName);
		}*/
		//createSchema(this.dbName);
		connectDB = DriverManager
				.getConnection("jdbc:mysql://" + host + "/"+dbName+"?"
						+ "user=" + user + "&password=" + passwd );
		/*if(delete == true)
		{
			executeUpdate(table_attributes);
			executeUpdate(table_tc);
			executeUpdate(table_gc);
			executeUpdate(table_gc_relations);
			executeUpdate(table_tc_observations);
			executeUpdate(table_GC_observations);
			executeUpdate(table_rules);
		}	*/
	}

	public void executeUpdate(String query)
	{
		try {
			statement = connectDB.createStatement();		
			statement.executeUpdate(query);
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Error Function: executeUpdate" );
		}
	}

	public int executeUpdateAndReturnID(String query)
	{
		long thisID = 0;
		try {
			statement = connectDB.createStatement();		
			statement.executeUpdate(query);
			ResultSet lastID = statement.getGeneratedKeys();		
			if (lastID.next()) {
				thisID = lastID.getLong(1);
			}
			statement.close();			
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Error Function: executeUpdate" );
		}
		return (int) thisID;
	}

	public ResultSet executeQuery(String query)
	{
		try {
			statement = connectDB.createStatement();		
			return statement.executeQuery(query);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Error Function: executeQuery" );
			return null;
		}
	}
	
	public int getSum(String query)
	{
		try
		{
			ResultSet rs = executeQuery(query);
			while(rs.next())
			{
				return rs.getInt(1);
			}
		} catch(Exception ex)
		{
			ex.getMessage();
			System.out.println("Error Function: getSum");
			return 0;
		}
		return 0;
	}
	
	public void updateInferenceNumber(int GC_ID, int number)
	{
		try
		{
			String query="UPDATE global_context "
					+ "SET NB_IDENTICAL_PAIRS_INFERENCE= " + number
					+ " WHERE ID_GC ='" + GC_ID +"'"; 
			executeUpdate(query);
		} catch(Exception ex)
		{
			System.out.println("Error Adding MIN and MAX");
		}
	}

	public void dropSchema(String db)
	{
		try {
			Connection connect = DriverManager
					.getConnection("jdbc:mysql://" + host + "/?"
							+ "user=" + user + "&password=" + passwd );
			statement = connect.createStatement();		
			statement.executeUpdate("DROP DATABASE IF EXISTS "+db);  
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void createSchema(String db)
	{
		try {
			Connection connect = DriverManager
					.getConnection("jdbc:mysql://" + host + "/?"
							+ "user=" + user + "&password=" + passwd );
			statement = connect.createStatement();		
			statement.executeUpdate("CREATE DATABASE IF NOT EXISTS "+db); 
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Error Function: createSchema" );
		}
	}

	public void addAllGlobalContextsToDB(ArrayList<String> globalContexts)
	{
		for(String gc : globalContexts)
		{
			String query = "INSERT INTO Contexts"
					+ "(uri) "
					+ "VALUES ('" + gc +"')";
			executeUpdate(query);
		}
	}


	public void addAllAttributesToDB(ArrayList<String> attributes)
	{
		for(String att : attributes)
		{
			String query = "INSERT INTO attributes"
					+ " (URI_ATT, IS_ABOUT, MIN_VALUE, MAX_VALUE) "
					+ "VALUES('" + att +"', '', '0', '0')";
			executeUpdate(query);
		}		
	}


	public void addAllAttributesToDB(HashMap<String, ArrayList<String>> attributes)
	{
		for(String att : attributes.keySet())
		{
			if(attributes.get(att).isEmpty())
			{
				String query = "INSERT INTO attributes"
						+ " (URI_ATT, IS_ABOUT, MIN_VALUE, MAX_VALUE) "
						+ "VALUES('" + att +"', '', '0', '0')";
				executeUpdate(query);
			}
			else
			{
				for(String isAbout : attributes.get(att))
				{
					String query = "INSERT INTO attributes"
							+ "(URI_ATT, IS_ABOUT, MIN_VALUE, MAX_VALUE) "
							+ "VALUES('" + att +"', '"+ isAbout +"', '0', '0')";
					executeUpdate(query);
				}
			}		
		}		
	}
	
	public int getTCFromDB(String tcInstance) throws SQLException
	{
		String query = "SELECT ID_TC FROM target_class "
				+ "WHERE URI_TC='" + tcInstance + "'"; 
		ResultSet rs = executeQuery(query);
		while(rs.next())
		{
			return rs.getInt(1);
		}
		rs.close();
		return 0;
	}

	public int getAttributeFromDB(String attribute, String isAbout) throws SQLException
	{
		String query = "SELECT ID_ATT FROM attributes "
				+ "WHERE URI_ATT='" + attribute + "' "
				+ "AND IS_ABOUT='" + isAbout + "'"; 
		ResultSet rs = executeQuery(query);
		while(rs.next())
		{
			return rs.getInt(1);
		}
		rs.close();
		return 0;
	}


	public int countNumberOfAttributes() throws SQLException
	{
		String query = "SELECT COUNT(ID_ATT) FROM attributes "; 
		ResultSet rs = executeQuery(query);
		while(rs.next())
		{
			return rs.getInt(1);
		}
		rs.close();
		return 0;
	}

	public float[] getMinMaxOfAttribute(int id) throws SQLException
	{
		float[] min_max = new float[2];
		String query = "SELECT MIN(VALUE), MAX(VALUE) FROM tc_observations"
				+ " where ID_ATT =" +id ;
		ResultSet rs = executeQuery(query);
		while(rs.next())
		{
			min_max[0] = rs.getFloat(1);
			min_max[1] = rs.getFloat(2);
			return min_max;
		}
		rs.close();
		return new float[2];
	}

	public void updateAttribute(int att_id, float min, float max)
	{
		try
		{
			String query="UPDATE attributes "
					+ "SET MIN_VALUE='" + min + "', MAX_VALUE='" + max + "' "
					+ "WHERE ID_ATT ='" + att_id +"'"; 
			executeUpdate(query);
		} catch(Exception ex)
		{
			System.out.println("Error Adding MIN and MAX");
		}
	}

	public float[] getMinMaxOfAttributeFromAtt(int id) throws SQLException
	{
		float[] min_max = new float[2];
		String query = "SELECT MIN_VALUE, MAX_VALUE FROM attributes"
				+ " where ID_ATT =" +id ;
		ResultSet rs = executeQuery(query);
		while(rs.next())
		{
			min_max[0] = rs.getFloat(1);
			min_max[1] = rs.getFloat(2);
			return min_max;
		}
		rs.close();
		return new float[2];
	}
	
	
	public HashMap<Integer, Float> getCommonAttributes(String res1, String res2)
	{
		HashMap<Integer, Float> result = new HashMap<>();
		String query = "SELECT t1.ID_ATT, ABS(t1.VALUE - t2.VALUE) As Abs"
				+ " FROM tc_observations as t1, tc_observations as t2" 
				+ " WHERE t1.ID_ATT = t2.ID_ATT"
				+ " AND t1.ID_TC = (SELECT ID_TC FROM target_class WHERE URI_TC ='" + res1 +"')"
				+ " AND t2.ID_TC = (SELECT ID_TC FROM target_class WHERE URI_TC ='" + res2 +"')"
				+ " Order by ID_ATT";
		ResultSet rs = executeQuery(query);
		try {
			while(rs.next())
			{
				result.put(rs.getInt(1), rs.getFloat(2));
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Error Function: getCommonAttributes" );
		}
		return result;
	}
	
	
	public ResultSet getRules(int ID_GC)
	{
		String query = "SELECT t1.ID_ATT, SUM(TOTAL_VALUE), SUM(TOTAL_NB), t2.MIN_VALUE, t2.MAX_VALUE "
				+ " FROM global_context_observations as t1" 
				+ " INNER JOIN attributes as t2 ON t1.ID_ATT = t2.ID_ATT"
				+ " WHERE t1.ID_GC =" + ID_GC
				+ " OR ID_GC IN (SELECT ID_GC FROM global_context_relations WHERE MORE_SPECIFIC_THAN=" + ID_GC + ")"
				+ " GROUP BY ID_ATT ORDER BY ID_ATT";	
		
		return executeQuery(query);
	}


/*	public ArrayList<Integer> getCommonAttributes(String res1, String res2)
	{
		ArrayList<Integer> result = new ArrayList<Integer>();
		String query = "SELECT DISTINCT ID_ATT FROM tc_observations"
				+ " WHERE ID_TC =" 
				+ "(SELECT ID_TC "
				+ " FROM target_class "
				+ " WHERE URI_TC ='" + res1 + "') " 
				+" AND ID_ATT in "
				+ "(SELECT ID_ATT FROM tc_observations "
				+ "WHERE ID_TC = " 
				+ "(SELECT ID_TC "
				+ " FROM target_class "
				+ " WHERE URI_TC ='" + res2 + "')"
				+")" ;
		ResultSet rs = executeQuery(query);
		try {
			while(rs.next())
			{
				result.add(rs.getInt(1));
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Error Function: getCommonAttributes" );
		}
		return result;
	}*/

	public Float getValueOfAttribute(String res, Integer ID_ATT)
	{
		String query = "SELECT avg(VALUE) FROM tc_observations"
				+ " WHERE ID_TC = " 
				+ "(SELECT ID_TC "
				+ " FROM target_class "
				+ " WHERE URI_TC ='" + res + "')" 
				+" AND ID_ATT='" +ID_ATT +"'";
		ResultSet rs = executeQuery(query);
		try {
			rs.next();
			return rs.getFloat(1);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Error Function: getValueOfAttribute" );
			return (float) 0;
		}
	}
	
	public Float getAbsValueOfAttribute(String res1, String res2, Integer ID_ATT)
	{
		String query = "SELECT avg(VALUE) FROM tc_observations"
				+ " WHERE ID_TC = " 
				+ "(SELECT ID_TC "
				+ " FROM target_class "
				+ " WHERE URI_TC ='" + res1 + "')" 
				+" AND ID_ATT='" +ID_ATT +"'";
		ResultSet rs = executeQuery(query);
		try {
			rs.next();
			return rs.getFloat(1);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Error Function: getValueOfAttribute" );
			return (float) 0;
		}
	}

	public void insertErrorRateToGC(int gc_id, int att_id, float errorRate, String res1, String res2)
	{
		try
		{
			String query = "INSERT INTO pair_observations"
					+ " (ID_TC_1, ID_TC_2, ID_GC, ID_ATT, ERROR_RATE)"
					+ " VALUES ("
					+ "(SELECT ID_TC "
					+ " FROM target_class "
					+ " WHERE URI_TC ='" + res1 + "'), "
					+ "(SELECT ID_TC "
					+ " FROM target_class "
					+ " WHERE URI_TC ='" + res2 + "'), '"
					+ gc_id + "', '"
					+ att_id + "', '"
					+ errorRate + "'"
					+ ")";
			executeUpdate(query);
		} catch(Exception ex)
		{
			System.out.println("Error Function: insertErrorRateToGC");
		}
	}


	public ResultSet getAverageErrorRate(int GC_ID)
	{
		try
		{
			String query = "SELECT ID_ATT, AVG(ERROR_RATE) "
					+ " FROM pair_observations "
					+ "WHERE ID_GC=" + GC_ID 
					+ " GROUP BY ID_ATT";
			ResultSet rs = executeQuery(query);
			return rs;
		} catch(Exception ex)
		{
			System.out.println("Error Function: getAverageErrorRate");
			return null;
		}
	}
	
	
	public ResultSet getAttributesWithInference(int GC_ID)
	{
		try
		{
			String query = "SELECT ID_ATT, AVG(ERROR_RATE) "
					+ " FROM pair_observations "
					+ "WHERE ID_GC=" + GC_ID 
					+ " GROUP BY ID_ATT";
			ResultSet rs = executeQuery(query);
			return rs;
		} catch(Exception ex)
		{
			System.out.println("Error Function: getAverageErrorRate");
			return null;
		}
	}

	public int getNumberOfPairsInGCWithID(int GC_ID, int ATT_ID)
	{
		try
		{
			String query = "SELECT COUNT(*) "
					+ " FROM pair_observations "
					+ "WHERE ID_GC=" + GC_ID 
					+ " AND ID_ATT=" + ATT_ID;
			ResultSet rs = executeQuery(query);
			while(rs.next())
			{
				return rs.getInt(1);
			}
		} catch(Exception ex)
		{
			ex.getMessage();
			System.out.println("Error Function: getNumberOfPairsInGCWithID");
			return 0;
		}
		return 0;
	}


	public int getNumberOfIdenticalPairsInference(int GC_ID)
	{
		try
		{
			String query = "SELECT NB_IDENTICAL_PAIRS_INFERENCE "
					+ " FROM global_context "
					+ "WHERE ID_GC=" + GC_ID;
			ResultSet rs = executeQuery(query);
			while(rs.next())
			{
				return rs.getInt(1);
			}
		} catch(Exception ex)
		{
			ex.getMessage();
			System.out.println("Error Function: getNumberOfIdenticalPairsInference");
			return 0;
		}
		return 0;
	}

	public void addNewRule(int ID_GC, int ID_ATT, float AVG_ERROR_RATE, double support, String desc_support)
	{
		String query = "INSERT INTO rules"
				+ " (ID_GC, ID_ATT, AVG_ERROR_RATE, SUPPORT, DESC_SUPPORT) "
				+ "VALUES('" + ID_GC + "', '" + ID_ATT + "', '" + AVG_ERROR_RATE +"', '" + support +"','"+desc_support+"')";
		executeUpdate(query);
	}
	



}
