package com.moubassher.accountsfx.accessControl;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class UserDao {

	private String driverPrefix = "jdbc:sqlite:"; //$NON-NLS-1$

	private String databaseName = "Users.db"; //$NON-NLS-1$

	private String databaseLocation = "file:/C:/pharmaNetResources/databases/"; //$NON-NLS-1$
	private String database = driverPrefix + databaseLocation + databaseName;

	private String name = "(موظف غير مسجل)";

	private String accountType = "مبيعات";

	DateTimeFormatter localTimeformat =
			DateTimeFormatter.ofPattern("hh:mm a").localizedBy(Locale.forLanguageTag("AR"));

	//UP allow resetting of users' password from PharmacyManagerBot 
	public String getAccountType() {
		return accountType;
	}

	public Map<String, List<User>> getuserType_EmployeeMap() {
		Map<String, List<User>> obMap = getAllEmployees().stream().collect(
				Collectors.groupingBy(employee -> employee.getUserType()));
		return obMap;
	}


	public boolean loginEmployee(String name, String password) {
		boolean validLogin = verifyLoginCredentials("employees", name, password); //$NON-NLS-1$
		if(validLogin) {
			LocalDateTime loginTime = LocalDateTime.now();
			registerLoginTime(name, loginTime);
		}
		return validLogin;
	}
	public boolean loginManagement(String name, String password) {
		return verifyLoginCredentials("management", name, password);
	}

	public List<Login> readLogins() {
		List<Login> logins = new ArrayList<>();
		String command =  "SELECT * FROM logins"; //$NON-NLS-1$
		try(Connection conn = DriverManager.getConnection(database)) {
			Statement stmt = conn.createStatement();
			stmt.execute(command);

			ResultSet results = stmt.getResultSet();
			while(results.next()) {
				String employee = results.getString("User"); //$NON-NLS-1$
				String loginDate = results.getString("loginDate"); //$NON-NLS-1$
				String loginTime = results.getString("loginTime"); //$NON-NLS-1$
				String logoutDate = results.getString("logoutDate"); //$NON-NLS-1$
				String logoutTime = results.getString("logoutTime"); //$NON-NLS-1$
				Login login = new Login(employee, loginDate, loginTime, logoutDate, logoutTime);

				logins.add(login);
			}

		} catch(SQLException e) {
			Logger.getLogger("pharmacy-log").severe(e.getMessage() + "culprit is: " + e.getCause() + "\n" + "location: " + this.getClass().getEnclosingMethod());
		}

		return logins;
	}


	public boolean logoutUser() {
		LocalDateTime logoutTime = LocalDateTime.now();
		registerLogoutTime(name, logoutTime);
		name = "";
		return true;
	}


	public void encryptDatabase() {
		String read = "SELECT * from employees;";
		String encrypt = "UPDATE management SET name = ?, password = ? WHERE name = ?;";
		ObservableList<String> names = FXCollections.observableArrayList();
		ObservableList<String> passwords = FXCollections.observableArrayList();
		try (Connection conn = DriverManager.getConnection(database)) {
			Statement stmt = conn.createStatement();
			stmt.execute(read);
			ResultSet results = stmt.getResultSet();
			while(results.next()) {
				String name = results.getString("name");
				String password = results.getString("password");
				names.add(name);
				passwords.add(password);
			}
			PreparedStatement encryptStmt = conn.prepareStatement(encrypt);

			String encryptedName = "";
			String encryptedPass = "";
			ObservableList<String> encryptedNameList = FXCollections.observableArrayList();
			ObservableList<String> encryptedPassList = FXCollections.observableArrayList();
			try {


				//Creating a Signature object
				Signature sign = Signature.getInstance("SHA256withRSA");

				//Creating KeyPair generator object
				KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");

				//Initializing the key pair generator
				keyPairGen.initialize(2048);

				//Generate the pair of keys
				KeyPair pair = keyPairGen.generateKeyPair();

				//Getting the public key from the key pair
				PublicKey publicKey = pair.getPublic();

				//Creating a Cipher object
				Cipher nameCipher, passCipher;
				nameCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
				passCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
				//Initializing a Cipher object
				nameCipher.init(Cipher.ENCRYPT_MODE, publicKey);
				passCipher.init(Cipher.ENCRYPT_MODE, publicKey);
				//Add data to the cipher
				byte[] nameInput = name.getBytes();
				//    byte [] passInput = pass.getBytes();
				nameCipher.update(nameInput);
				//  passCipher.update(passInput);

				//encrypting the data
				byte[] nameCipherText = nameCipher.doFinal();
				byte [] passCipherText = passCipher.doFinal();

				encryptedName = new String (nameCipherText, "UTF8");
				encryptedPass = new String(passCipherText, "UTF8");

				encryptedNameList.add(encryptedName);
				encryptedPassList.add(encryptedPass);

				//Initializing the same cipher for decryption
				nameCipher.init(Cipher.DECRYPT_MODE, pair.getPrivate());

				//Decrypting the text
				byte[] decipheredText = nameCipher.doFinal(nameCipherText);


			} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
				Logger.getLogger("pharmacy-log").severe(e.getMessage() + "culprit is: " + e.getCause() + "\n" + "location: " + this.getClass().getEnclosingMethod());
			} catch (InvalidKeyException e) {
				Logger.getLogger("pharmacy-log").severe(e.getMessage() + "culprit is: " + e.getCause() + "\n" + "location: " + this.getClass().getEnclosingMethod());
			} catch (IllegalBlockSizeException e) {
				Logger.getLogger("pharmacy-log").severe(e.getMessage() + "culprit is: " + e.getCause() + "\n" + "location: " + this.getClass().getEnclosingMethod());
			} catch (BadPaddingException e) {
				Logger.getLogger("pharmacy-log").severe(e.getMessage() + "culprit is: " + e.getCause() + "\n" + "location: " + this.getClass().getEnclosingMethod());
			} catch (UnsupportedEncodingException e) {
				Logger.getLogger("pharmacy-log").severe(e.getMessage() + "culprit is: " + e.getCause() + "\n" + "location: " + this.getClass().getEnclosingMethod());
			}

			for(int i = 0; i < encryptedNameList.size(); i++) {
				encryptStmt.setString(1, encryptedName);
				encryptStmt.setString(2, encryptedPass);
				encryptStmt.setString(3, name);
				encryptStmt.addBatch();
			}

			encryptStmt.executeUpdate();


		} catch (SQLException e) {
			Logger.getLogger("pharmacy-log").severe(e.getMessage() + "culprit is: " + e.getCause() + "\n" + "location: " + this.getClass().getEnclosingMethod());
		}
	}

	public ObservableList<User> getAllEmployees() {

		ObservableList<User> allUsers = FXCollections.observableArrayList();

		String command = "SELECT userId, name, password, userType, salary, workStart, workFinish FROM users;"; //$NON-NLS-1$
		try(Connection conn = DriverManager.getConnection(database)) {
			Statement stmt = conn.createStatement();
			stmt.execute(command);
			ResultSet results = stmt.getResultSet();
			while(results.next()) {
				int userId = results.getInt("userId");
				String name = results.getString("name"); //$NON-NLS-1$
				String password = results.getString("password");
				String userType = results.getString("userType"); //$NON-NLS-1$
				int salary = results.getInt("salary");
				String workStartRaw = results.getString("workStart");
				LocalTime workStart = null;
				if(workStartRaw != null) {
					workStart = LocalTime.parse(workStartRaw, localTimeformat);
				}
				
				String workFinishRaw = results.getString("workFinish");
				LocalTime workFinish = null;
				if(workFinishRaw != null) {
					workFinish = LocalTime.parse(workStartRaw, localTimeformat);
				}
				User user = new User(userId, name, password, userType, salary, workStart, workFinish);

				allUsers.add(user);
			}

		} catch (SQLException e) {
			Logger.getLogger("pharmacy-log").severe(e.getMessage() + "culprit is: " + e.getCause() + "\n" + "location: " + this.getClass().getEnclosingMethod());
		}
		return allUsers;
	}

	private void registerLogoutTime(String name, LocalDateTime logoutTime) {
		String command = "UPDATE logins SET logoutTime = ?, logoutDate = ? WHERE User = ? AND loginDate = ?"; //$NON-NLS-1$

		try(Connection conn = DriverManager.getConnection(database)) {
			PreparedStatement stmt = conn.prepareStatement(command);

			var dateFormatter =
					DateTimeFormatter.ofPattern("E yyy/M/dd").localizedBy(Locale.forLanguageTag("AR"));
			var timeFormatter =
					DateTimeFormatter.ofPattern("HH:mm a").localizedBy(Locale.forLanguageTag("AR"));

			LocalDate today = LocalDate.now();
			String formattedLogoutTime = timeFormatter.format(logoutTime);
			String formattedLoginDate = dateFormatter.format(today);

			stmt.setString(1, formattedLogoutTime);
			stmt.setString(2, formattedLoginDate);
			stmt.setString(3, name);
			stmt.setString(4, formattedLoginDate);

			stmt.executeUpdate();
		} catch (SQLException e) {
			Logger.getLogger("pharmacy-log").severe(e.getMessage() + "culprit is: " + e.getCause() + "\n" + "location: " + this.getClass().getEnclosingMethod());
		}
	}

	private void registerLoginTime(String name, LocalDateTime loginTime) {
		String command = "INSERT into logins (User, loginDate, loginTime) values (?, ?, ?)"; //$NON-NLS-1$
		try(Connection conn = DriverManager.getConnection(database)) {
			PreparedStatement stmt = conn.prepareStatement(command);
			stmt.setString(1, name);

			var dateFormatter =
					DateTimeFormatter.ofPattern("E yyy/M/dd").localizedBy(Locale.forLanguageTag("AR"));
			var timeFormatter =
					DateTimeFormatter.ofPattern("HH:mm a").localizedBy(Locale.forLanguageTag("AR"));

			String formattedDate = dateFormatter.format(loginTime);
			String formattedTime = timeFormatter.format(loginTime);

			stmt.setString(2, formattedDate);
			stmt.setString(3, formattedTime);
			stmt.executeUpdate();


		} catch (SQLException e) {
			Logger.getLogger("pharmacy-log").severe(e.getMessage() + "culprit is: " + e.getCause() + "\n" + "location: " + this.getClass().getEnclosingMethod());
		}
	}

	public ObservableList<String> readAllUserNames() {
		ObservableList<String> allUsers = FXCollections.observableArrayList();
		String command = "SELECT name FROM employees;"; //$NON-NLS-1$
		try(Connection conn = DriverManager.getConnection(database)) {
			Statement stmt = conn.createStatement();
			stmt.execute(command);
			ResultSet results = stmt.getResultSet();
			while(results.next()) {
				String oneUser = results.getString("name"); //$NON-NLS-1$
				allUsers.add(oneUser);
			}
		} catch (SQLException e) {
			Logger.getLogger("pharmacy-log").severe(e.getMessage() + "culprit is: " + e.getCause() + "\n" + "location: " + this.getClass().getEnclosingMethod());
		}
		return allUsers;
	}

	public boolean resetPassword(String employeeName, String newPassword) {
		boolean reset = false;
		String command = "UPDATE employees SET password = ? WHERE name = ?";
		try (Connection conn = DriverManager.getConnection(database)) {
			PreparedStatement stmt = conn.prepareStatement(command);
			stmt.setString(1, newPassword);
			stmt.setString(2, employeeName);
			stmt.executeUpdate();
			reset = true;

		} catch (SQLException e) {
			Logger.getLogger("pharmacy-log").severe(e.getMessage() + "culprit is: " + e.getCause() + "\n" + "location: " + this.getClass().getEnclosingMethod());
			System.exit(0);
		}
		return reset;
	}

	public boolean isAdminRequired() {
		return getAllEmployees().stream()
				.noneMatch(employee -> employee.isManager());
	}

	public List<String> readuserTypes() {
		List<String> userTypes = new ArrayList<>();
		String sql = "SELECT employee_type FROM user_type;";
		try(Connection conn = DriverManager.getConnection(database)) {
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.execute();
			ResultSet results = stmt.getResultSet();
			while(results.next()) {
				String userType = results.getString("employee_type");
				userTypes.add(userType);
			}
		} catch (SQLException e) {
            e.printStackTrace();
        }
		return userTypes;
    }

	private boolean verifyLoginCredentials(String tableName, String name, String password) {

		String command = "SELECT name, password from "+ tableName +" WHERE name = ? AND password = ?;"; //$NON-NLS-1$
		boolean validCreds = false;

		try(Connection conn = DriverManager.getConnection(database)) {
			PreparedStatement stmt = conn.prepareStatement(command);
			stmt.setString(1, name);
			stmt.setString(2, password);

			ResultSet results = stmt.executeQuery();
			while(results.next()) {
				name = results.getString("name");
				password = results.getString("password");
				this.name = name;

				validCreds = true;
			}
		} catch (SQLException e) {

			Logger.getLogger("pharmacy-log").severe(e.getMessage() + "culprit is: " + e.getCause() + "\n" + "location: " + this.getClass().getEnclosingMethod());
		}
		return validCreds;
	}


	public boolean updateEmployee(String attr, String attrValue, String name) {
		boolean done = false;
		String command = "UPDATE employees SET ? = ? WHERE name = ?;";
		try (Connection conn = DriverManager.getConnection(database)) {
			PreparedStatement stmt = conn.prepareStatement(command);
			stmt.setString(1, attr);
			stmt.setString(2, attrValue);
			stmt.setString(3, name);
			stmt.executeUpdate();

			done = true;
		} catch (SQLException e) {
			Logger.getLogger("pharmacy-log").severe(e.getMessage() + "culprit is: " + e.getCause() + "\n" + "location: " + this.getClass().getEnclosingMethod());
		}
		return done;
	}

	public boolean deleteEmployee(String name) {
		boolean done = false;
		String command = "delete from employees WHERE name = ?;";
		try (Connection conn = DriverManager.getConnection(database)) {
			PreparedStatement stmt = conn.prepareStatement(command);

			stmt.setString(1, name);
			stmt.executeUpdate();

			done = true;
		} catch (SQLException e) {
			Logger.getLogger("pharmacy-log").severe(e.getMessage() + "culprit is: " + e.getCause() + "\n" + "location: " + this.getClass().getEnclosingMethod());
		}
		return done;
	}


	public String getName() {
		return name;
	}

	public boolean addNewEmployee(User user) {
		boolean added = false;

		if(user.getName() == null || user.getName().isBlank()) {
			throw new IllegalArgumentException("Cannot add a nameless user");
		}

		if(isAdminRequired()) {
			throw new UnsupportedOperationException("Cannot create a non-admin account without an admin account present first");
		}

		String command = "INSERT into users (name, password, userType, salary, workStart, workFinish) " //$NON-NLS-1$
				+ "VALUES (?,?,?,?,?,?)"; //$NON-NLS-1$
		try(Connection conn = DriverManager.getConnection(database)) {
			PreparedStatement stmt = conn.prepareStatement(command);

			stmt.setString(1, user.getName());
			stmt.setString(2, user.getHashedPassword());
			stmt.setString(3, user.getUserType());
			stmt.setInt(4, user.getSalary());

			String shiftStart = user.getShiftStart().format(localTimeformat);
			String shiftFinish = user.getShiftEnd().format(localTimeformat);
			stmt.setString(4, shiftStart);
			stmt.setString(5, shiftFinish);

			int affectedRows = stmt.executeUpdate();
			added = affectedRows > 0;

		} catch (SQLException e) {
			Logger.getLogger("pharmacy-log").severe(e.getMessage() + "culprit is: " + e.getCause() + "\n" + "location: " + this.getClass().getEnclosingMethod());
		}
		return added;
	}

	public boolean createManager(Manager manager) {
		boolean created = false;
		String sql = "INSERT INTO employees(name, password, userType) values(?,?, ?);";
		try(Connection connection = DriverManager.getConnection(database)) {
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setString(1, manager.getName());
			stmt.setString(2, manager.getHashedPassword());
			stmt.setString(3, manager.getUserType());
			int affectedRows = stmt.executeUpdate();
			created = affectedRows > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return created;
	}

	private UserDao() {

		//encryptDatabase();
	}

	private static UserDao instance = null;

	public static UserDao getInstance() {
		if (instance == null) {
			instance = new UserDao();
		}
		return instance;
	}


}