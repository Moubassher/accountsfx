package com.moubassher.accountsfx.accessControl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class TaskDatabase {

	private String driverPrefix = "jdbc:sqlite:"; //$NON-NLS-1$

	private String databaseName = "MgmtTasks.db"; //$NON-NLS-1$

	private String databaseLocation = "file:C:pharmaNetResources\\databases\\"; //$NON-NLS-1$
	private String database = driverPrefix + databaseLocation + databaseName;


	public int countTasksDue() {
		int tasksDue = 0;
		String command = "SELECT COUNT(*) from Todo;";
		try (Connection conn = TaskDatabase.connectToDatabase(database)) {
			Statement stmt = conn.createStatement();
			stmt.execute(command);

			ResultSet results = stmt.getResultSet();

			while(results.next()) {
				tasksDue = results.getInt("COUNT(*)");	
			}

		} catch (SQLException e) {

			Logger.getLogger("pharmacy-log").severe(e.getMessage() + "culprit is: " + e.getCause() + "\n" + "location: " + this.getClass().getEnclosingMethod());

		}

		return tasksDue;
	}


	public boolean addTask(String task, String description) {
		boolean done = false;
		String command = "INSERT INTO Todo (task, description) VALUES (?, ?);";
		try (Connection conn = TaskDatabase.connectToDatabase(database)) {
			PreparedStatement stmt = conn.prepareStatement(command);
			stmt.setString(1, task);
			stmt.setString(2, description);
			stmt.execute();

			done = true;

		} catch (SQLException e) {
			Logger.getLogger("pharmacy-log").severe(e.getMessage() + "culprit is: " + e.getCause() + "\n" + "location: " + this.getClass().getEnclosingMethod());
		}

		return done;
	}

	private static Connection connectToDatabase(String databaseLocation) {
		String MethodName = "connectToDatabase() of DictionaryDatabase"; //$NON-NLS-1$
		Connection connection = null;

		try {
			connection = DriverManager.getConnection(databaseLocation); 

		} catch(SQLException e) {
			Logger.getLogger("pharmacy-log").severe(e.getMessage() + "culprit is: " + e.getCause() + "\n" + "location: " + UserDao.class.getEnclosingMethod());
		}
		return connection;

	}	//end of connectToDatabase()



	private TaskDatabase() {
	}

	private static TaskDatabase instance = null;

	public static TaskDatabase getInstance() {
		if (instance == null) {
			instance = new TaskDatabase();
		}
		return instance;
	}

}
