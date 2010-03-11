package hu.mokk.hunglish.domain;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Basic {

	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		// TODO Auto-generated method stub
		Class.forName("com.mysql.jdbc.Driver").newInstance(); 
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost", "hunglish", "sw6x2the");  
		PreparedStatement st = conn.prepareStatement("select hu_sentence from hunglishwebapp.bisen where id = 400001");
		ResultSet rs = st.executeQuery();
		rs.first();
		System.out.println(rs.getString(1));
	}

}
