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

public class AccountDao {

	private String driverPrefix = "jdbc:sqlite:"; //$NON-NLS-1$

	private String databaseName = "Users.db"; //$NON-NLS-1$

	private String databaseLocation = "file:/C:/pharmaNetResources/databases/"; //$NON-NLS-1$
	private String database = driverPrefix + databaseLocation + databaseName;

	private String name = "(حساب غير مسجل)";

	private String accountType = "مبيعات";

	DateTimeFormatter localTimeformat =
			DateTimeFormatter.ofPattern("hh:mm a").localizedBy(Locale.forLanguageTag("AR"));

	//UP allow resetting of users' password from a Maintainer account.
	public String getAccountType() {
		return accountType;
	}




	public boolean loginAccount(String name, String password) {
		boolean validLogin = verifyLoginCredentials("users", name, password); //$NON-NLS-1$
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
				String employee = results.getString("Account"); //$NON-NLS-1$
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

	public ObservableList<Account> readAccounts() {

		ObservableList<Account> allAccounts = FXCollections.observableArrayList();

		// Load all account types once
		Map<Integer, AccountType> accountTypeMap = readAccountTypes()
				.stream()
				.collect(Collectors.toMap(AccountType::getAccountTypeId, at -> at));

		String sql =
				"SELECT userId, name, password, accountTypeId, salary, workStart, workFinish " +
						"FROM users;";

		try (Connection conn = DriverManager.getConnection(database);
			 PreparedStatement stmt = conn.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {

				int userId = rs.getInt("userId");
				String name = rs.getString("name");
				String password = rs.getString("password");
				int accountTypeId = rs.getInt("accountTypeId");
				int salary = rs.getInt("salary");

				// Parse times
				LocalTime workStart = Optional.ofNullable(rs.getString("workStart"))
						.map(t -> LocalTime.parse(t, localTimeformat))
						.orElse(null);

				LocalTime workFinish = Optional.ofNullable(rs.getString("workFinish"))
						.map(t -> LocalTime.parse(t, localTimeformat))
						.orElse(null);

				// Fetch AccountType directly from the map
				AccountType accountType = accountTypeMap.get(accountTypeId);

				Account account = new Account(
						userId,
						name,
						password,
						accountType,
						salary,
						workStart,
						workFinish
				);

				allAccounts.add(account);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return allAccounts;
	}



	public List<AccountType> readAccountTypes() {
		List<AccountType> accountTypes = new ArrayList<>();

		String sql =
				"SELECT a.accountTypeId, a.accountType, a.isAdmin, " +
						"       GROUP_CONCAT(p.feature) AS privileges " +
						"FROM accountTypes a " +
						"LEFT JOIN privileges p ON p.accountTypeId = a.accountTypeId " +
						"GROUP BY a.accountTypeId";

		try (Connection conn = DriverManager.getConnection(database);
			 PreparedStatement stmt = conn.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {

				int id = rs.getInt("accountTypeId");
				String type = rs.getString("accountType");

				String privilegeString = rs.getString("privileges");
				List<String> privileges = (privilegeString == null)
						? new ArrayList<>()
						: Arrays.asList(privilegeString.split(","));

				boolean isAdmin = rs.getBoolean("isAdmin");

				accountTypes.add(new AccountType(id, type, privileges, isAdmin));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return accountTypes;
	}


	private void registerLogoutTime(String name, LocalDateTime logoutTime) {
		String command = "UPDATE logins SET logoutTime = ?, logoutDate = ? WHERE Account = ? AND loginDate = ?"; //$NON-NLS-1$

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
		String command = "INSERT into logins (Account, loginDate, loginTime) values (?, ?, ?)"; //$NON-NLS-1$
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
		return readAccounts().stream()
				.noneMatch(employee -> employee.isAdmin());
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

	public boolean deleteAccount(Account account) {
		boolean done = false;
		int accountId = account.getUserId();
		String command = "delete from users WHERE userId = ?;";
		try (Connection conn = DriverManager.getConnection(database)) {
			PreparedStatement stmt = conn.prepareStatement(command);

			stmt.setInt(1, accountId);
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

	public void addNewFeatures(List<String> featureNames) {

		String sql = "INSERT OR IGNORE INTO features (featureName) VALUES (?)";

		try (Connection conn = DriverManager.getConnection(database);
			 PreparedStatement stmt = conn.prepareStatement(sql)) {

			conn.setAutoCommit(false);  // batch insert for speed & safety

			for (String name : featureNames) {
				stmt.setString(1, name);
				stmt.addBatch();
			}

			stmt.executeBatch();
			conn.commit();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void deleteFeatures(List<String> featureNames) {

		String sql = "DELETE FROM features WHERE featureName = ?";

		try (Connection conn = DriverManager.getConnection(database);
			 PreparedStatement stmt = conn.prepareStatement(sql)) {

			conn.setAutoCommit(false);  // ensure atomic batch deletion

			for (String name : featureNames) {
				stmt.setString(1, name);
				stmt.addBatch();
			}

			stmt.executeBatch();
			conn.commit();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	public boolean addAccountTypes(List<AccountType> types) {

		String sql = "INSERT INTO accountTypes (accountType, isAdmin) VALUES (?, ?)";

		try (Connection conn = DriverManager.getConnection(database);
			 PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			conn.setAutoCommit(false);

			for (AccountType accountType : types) {
				stmt.setString(1, accountType.getType());
				stmt.setBoolean(2, accountType.isAdmin());

				int affected = stmt.executeUpdate();

				// If row was actually inserted (not ignored), retrieve generated ID
				if (affected > 0) {
					try (ResultSet rs = stmt.getGeneratedKeys()) {
						if (rs.next()) {
							int id = rs.getInt(1);
							accountType.setId(id); // store the generated id in the object
						}
					}
				}
			}

			conn.commit();
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}


	private boolean isAccountTypeValid(AccountType accountType) {
		return readAccountTypes().contains(accountType);
	}

	public boolean addNewAccount(Account account) {
		boolean added = false;

		if(account.getName() == null || account.getName().isBlank()) {
			throw new IllegalArgumentException("Cannot add a nameless account");
		}

		AccountType accountType = account.getAccountType();
		if(accountType == null || !isAccountTypeValid(accountType)) {
			throw new IllegalArgumentException("Account type when inserting account is invalid");
		}

		if(isAdminRequired() && !account.isAdmin()) {
			throw new UnsupportedOperationException("Cannot create a non-admin account without an admin account present first");
		}

		String command = "INSERT into users (name, password, accountTypeId, salary, workStart, workFinish) " //$NON-NLS-1$
				+ "VALUES (?,?,?,?,?,?)";

		try(Connection conn = DriverManager.getConnection(database)) {
			PreparedStatement stmt = conn.prepareStatement(command);

			stmt.setString(1, account.getName());
			stmt.setString(2, account.getHashedPassword());
			stmt.setInt(3, account.getAccountType().getAccountTypeId());
			stmt.setInt(4, account.getSalary());

			stmt.setObject(4,
					account.getShiftStart() != null ? account.getShiftStart().format(localTimeformat) : null
			);

			stmt.setObject(5,
					account.getShiftEnd() != null ? account.getShiftEnd().format(localTimeformat) : null
			);


			int affectedRows = stmt.executeUpdate();
			added = affectedRows > 0;

		} catch (SQLException e) {
			Logger.getLogger("pharmacy-log").severe(e.getMessage() + "culprit is: " + e.getCause() + "\n" + "location: " + this.getClass().getEnclosingMethod());
		}
		return added;
	}

	private AccountDao() {
	}

	private static AccountDao instance = null;

	public static AccountDao getInstance() {
		if (instance == null) {
			instance = new AccountDao();
		}
		return instance;
	}


}