package com.moubassher.accountsfx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class  AccountDao <F extends Enum<F> & Feature<F>>{

	private final String driverPrefix = "jdbc:sqlite:"; //$NON-NLS-1$

	private final String databaseName = "Users.db"; //$NON-NLS-1$

	private final String databaseLocation = "file:/C:/pharmaNetResources/databases/"; //$NON-NLS-1$
	private final String database = driverPrefix + databaseLocation + databaseName;

	DateTimeFormatter localTimeformat =
			DateTimeFormatter.ofPattern("hh:mm a").localizedBy(Locale.forLanguageTag("AR"));


	private Class<F> featureEnum;

	public AccountDao(Class<F> featureEnum) {
		this.featureEnum = featureEnum;
	}

	public AccountDao() {

	}


	public AccountType<F> getAccountType(int accountTypeId) {
		return readAccountTypes().stream()
				.filter(accountType -> accountType.getAccountTypeId() == accountTypeId)
				.findFirst().orElse(null);

	}


	public boolean loginAccount(Account<F> account) {
		int userId = account.getUserId();
		String password = account.getHashedPassword();
		boolean validCredentials = verifyLoginCredentials(userId, password); //$NON-NLS-1$
		if(validCredentials) {
			return registerLogin(userId);
		}
		return false;
	}

	private boolean registerLogin(int userId) {
		String command = "INSERT INTO user_sessions (userId, loginAt) VALUES (?, ?)"; //$NON-NLS-1$

		try (Connection conn = DriverManager.getConnection(database);
			 PreparedStatement stmt = conn.prepareStatement(command)) {

			var dateTimeFormatter =
					DateTimeFormatter.ofPattern("E yyyy/M/dd HH:mm a")
							.localizedBy(Locale.forLanguageTag("ar"));

			LocalDateTime login = LocalDateTime.now();
			stmt.setInt(1, userId);
			stmt.setString(2, login.format(dateTimeFormatter));

			stmt.executeUpdate();
			return true;

		} catch (SQLException e) {
			Logger.getLogger("pharmacy-log").severe(e.getMessage()+ " culprit is: "+ e.getCause()+ "\nlocation: "
							+ this.getClass().getEnclosingMethod());
			return false;
		}
	}

	public boolean logoutAccount(Account<F> account) {
		int userId = account.getUserId();
		String password = account.getHashedPassword();
		boolean validCredentials = verifyLoginCredentials(userId, password); //$NON-NLS-1$
		if(validCredentials) {
			return registerLogout(userId);
		}
		return false;
	}

	private boolean registerLogout(int userId) {
		String command =
				"UPDATE user_sessions " +
						"SET logoutAt = ? " +
						"WHERE userId = ? AND logoutAt IS NULL"; //$NON-NLS-1$

		try (Connection conn = DriverManager.getConnection(database);
			 PreparedStatement stmt = conn.prepareStatement(command)) {

			var dateTimeFormatter =
					DateTimeFormatter.ofPattern("E yyyy/M/dd HH:mm a")
							.localizedBy(Locale.forLanguageTag("ar"));

			LocalDateTime logoutTime = LocalDateTime.now();
			stmt.setString(1, logoutTime.format(dateTimeFormatter));
			stmt.setInt(2, userId);

			return stmt.executeUpdate() > 0;

		} catch (SQLException e) {
			Logger.getLogger("pharmacy-log").severe(e.getMessage()+ " culprit is: "+ e.getCause()+ "\nlocation: "+ this.getClass().getEnclosingMethod());
			return false;
		}
	}


	public List<Login> readLogins() {
		List<Login> logins = new ArrayList<>();
		String command =  "SELECT * FROM logins"; //$NON-NLS-1$
		try(Connection conn = DriverManager.getConnection(database)) {
			Statement stmt = conn.createStatement();
			stmt.execute(command);

			ResultSet results = stmt.getResultSet();
			while(results.next()) {
				String employee = results.getString("userId"); //$NON-NLS-1$
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
				byte[] nameInput = "name.getBytes()".getBytes();
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
				encryptStmt.addBatch();
			}

			encryptStmt.executeUpdate();


		} catch (SQLException e) {
			Logger.getLogger("pharmacy-log").severe(e.getMessage() + "culprit is: " + e.getCause() + "\n" + "location: " + this.getClass().getEnclosingMethod());
		}
	}

	public ObservableList<Account<F>> readAccounts() {

		ObservableList<Account<F>> allAccounts = FXCollections.observableArrayList();

		// Load all account types once
		Map<Integer, AccountType<F>> accountTypeMap = readAccountTypes()
				.stream()
				.collect(Collectors.toMap(
						AccountType::getAccountTypeId,
						at -> at,
						(a, b) -> a   // keep the first; or choose which one wins
				));
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
				AccountType<F> accountType = accountTypeMap.get(accountTypeId);

				Account<F> account = new Account(
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


	public  List<AccountType<F>> readArchivedAccountTypes() {
		List<AccountType<F>> accountTypes = new ArrayList<>();

		String sql =
				"SELECT a.accountTypeId, a.accountType, a.isAdmin, " +
						"       GROUP_CONCAT(p.feature) AS privileges " +
						"FROM accountTypes_archive a " +
						"LEFT JOIN privileges_archive p ON p.accountTypeId = a.accountTypeId " +
						"GROUP BY a.accountTypeId";

		try (Connection conn = DriverManager.getConnection(database);
			 PreparedStatement stmt = conn.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				int accountTypeId = rs.getInt("accountTypeId");
				String type = rs.getString("accountType");

				String privilegeString = rs.getString("privileges");
				List<F> privileges;
				if (privilegeString == null || privilegeString.isEmpty()) {
					privileges = new ArrayList<>();
				} else {
					privileges = Arrays.stream(privilegeString.split(","))
							.map(s -> Enum.valueOf(featureEnum, s)) // convert String to enum
							.toList();
				}

				boolean isAdmin = rs.getBoolean("isAdmin");
				AccountType<F> accountType = new AccountType(accountTypeId, type, privileges, isAdmin);
				accountTypes.add(accountType);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return accountTypes;
	}



	public  List<AccountType<F>> readAccountTypes() {
		List<AccountType<F>> accountTypes = new ArrayList<>();

		String sql =
				"SELECT a.accountTypeId, a.accountType, a.isAdmin, " +
						" GROUP_CONCAT(p.feature) AS privileges " +
						"FROM accountTypes a " +
						"LEFT JOIN privileges p ON p.accountTypeId = a.accountTypeId " +
						"GROUP BY a.accountTypeId";

		try (Connection conn = DriverManager.getConnection(database);
			 PreparedStatement stmt = conn.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				int accountTypeId = rs.getInt("accountTypeId");
				String type = rs.getString("accountType");

				String privilegeString = rs.getString("privileges");
				List<F> privileges;
				if (privilegeString == null || privilegeString.isEmpty()) {
					privileges = new ArrayList<>();
				} else {
					privileges = Arrays.stream(privilegeString.split(","))
							.map(s -> Enum.valueOf(featureEnum, s)) // convert String to enum
							.toList();
				}

				boolean isAdmin = rs.getBoolean("isAdmin");
				AccountType<F> accountType = new AccountType(accountTypeId, type, privileges, isAdmin);
				accountTypes.add(accountType);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return accountTypes;
	}


	public void createMaintainerAccount(Class<F> featureEnum) {
		List<F> privileges = List.of(featureEnum.getEnumConstants());
		AccountType<F> maintainerAccount = new AccountType<>("Maintainer", privileges, true);

		boolean maintainerTypeCreated = addAccountType(maintainerAccount);
		if(maintainerTypeCreated) {
			String password = "perseverence123$";
			String hashedPassword = new PasswordValidator().hashPassword(password);
			Account maintainer = new Account("Moubassher", hashedPassword, maintainerAccount);
			addNewAccount(maintainer);
		}

	}

	void deleteAllAccounts() {
		String sql = "DELETE FROM users";
		try (Connection conn = DriverManager.getConnection(database)
			 ;PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean resetPassword(Account<F> account, String newPassword) {
		boolean reset = false;
		String command = "UPDATE users SET password = ? WHERE userId = ?";
		try (Connection conn = DriverManager.getConnection(database)) {
			PreparedStatement stmt = conn.prepareStatement(command);
			stmt.setString(1, newPassword);
			stmt.setInt(2, account.getUserId());
			stmt.executeUpdate();
			reset = true;

		} catch (SQLException e) {
			Logger.getLogger("pharmacy-log").severe(e.getMessage() + "culprit is: " + e.getCause() + "\n" + "location: " + this.getClass().getEnclosingMethod());
			System.exit(0);
		}
		return reset;
	}

	public  boolean isAdminRequired() {
		return readAccounts().stream()
				.noneMatch(employee -> employee.isAdmin());
	}

	private boolean verifyLoginCredentials(int userId, String hashedPassword) {

		String command = "SELECT userId, password from users WHERE userId = ? AND password = ?;"; //$NON-NLS-1$
		boolean validCreds = false;

		try(Connection conn = DriverManager.getConnection(database)) {
			PreparedStatement stmt = conn.prepareStatement(command);
			stmt.setInt(1, userId);
			stmt.setString(2, hashedPassword);

			ResultSet results = stmt.executeQuery();
			while(results.next()) {
				String readHashedPassword = results.getString("password");
				if(hashedPassword.equals(readHashedPassword)) {
					return true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return validCreds;
	}


	boolean deleteAccounts(List<Account<F>> accounts) {
		if (accounts == null || accounts.isEmpty()) {
			return true;
		}

		String command = "DELETE FROM users WHERE userId = ?;";

		try (Connection conn = DriverManager.getConnection(database);
			 PreparedStatement stmt = conn.prepareStatement(command)) {

			conn.setAutoCommit(false);

			for (Account<F> account : accounts) {
				stmt.setInt(1, account.getUserId());
				stmt.addBatch();
			}

			stmt.executeBatch();
			conn.commit();
			return true;

		} catch (SQLException e) {
			Logger.getLogger("pharmacy-log").severe(
					e.getMessage() + " culprit: " + e.getCause() +
							"\nlocation: " + this.getClass().getEnclosingMethod()
			);
			return false;
		}
	}


	public boolean addAccountType(AccountType<F> accountType) {

		accountType.validate();

		boolean isAccountTypeNew = isAccountTypeNew(accountType);
		if(!isAccountTypeNew) {
			return false;
		}


		String sql = "INSERT INTO accountTypes (accountType, isAdmin) VALUES (?, ?)";

		try (Connection conn = DriverManager.getConnection(database);
			 PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			stmt.setString(1, accountType.getType());
			stmt.setBoolean(2, accountType.isAdmin());

			int affected = stmt.executeUpdate();

			if (affected > 0) {

				try (ResultSet rs = stmt.getGeneratedKeys()) {
					if (rs.next()) {
						int id = rs.getInt(1);
						accountType.setId(id); // store generated id back in object
					}
				}
				addPrivileges(accountType);

			}

			return affected > 0;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	private boolean isAccountTypeNew(AccountType<F> accountType) {
		String newType = accountType.getType().toLowerCase();
		return readAccountTypes().stream()
				.map(type -> type.getType().toLowerCase())
				.noneMatch(name -> name.equals(newType));
	}


	public boolean updateAccountType(int accountTypeId, AccountType<F> accountType) {
		String sqlUpdateType = "UPDATE accountTypes SET accountType = ?, isAdmin = ? WHERE accountTypeId = ?";

		try (Connection connection = DriverManager.getConnection(database);
			 PreparedStatement stmt = connection.prepareStatement(sqlUpdateType)) {

			stmt.setString(1, accountType.getType());
			stmt.setBoolean(2, accountType.isAdmin());
			stmt.setInt(3, accountTypeId);
			stmt.executeUpdate();

			String sqlDeletePrivileges = "DELETE FROM privileges WHERE accountTypeId = ?";
			try (PreparedStatement delStmt = connection.prepareStatement(sqlDeletePrivileges)) {
				delStmt.setInt(1, accountType.getAccountTypeId());
				delStmt.executeUpdate();
			}

			addPrivileges(accountType);

			return true; // success
		} catch (Exception e) {
			e.printStackTrace();
			return false; // failure
		}
	}

	boolean deleteAccountType(int accountTypeId) {
		String deletePrivilegesSql = "DELETE FROM privileges WHERE accountTypeId = ?";
		String deleteAccountTypeSql = "DELETE FROM accountTypes WHERE accountTypeId = ?";

		try (Connection conn = DriverManager.getConnection(database)) {
			conn.setAutoCommit(false);

			// Delete related privileges
			try (PreparedStatement ps = conn.prepareStatement(deletePrivilegesSql)) {
				ps.setInt(1, accountTypeId);
				ps.executeUpdate();
			}

			// Delete the account type
			try (PreparedStatement ps = conn.prepareStatement(deleteAccountTypeSql)) {
				ps.setInt(1, accountTypeId);
				int affectedRows = ps.executeUpdate();
				if (affectedRows == 0) {
					conn.rollback();
					return false;
				}
			}

			conn.commit();
			return true;

		} catch (Exception e) {
			try { DriverManager.getConnection(database).rollback(); } catch (Exception ignored) {}

			return false;
		}
	}



	public void addPrivileges(AccountType<F> accountType) throws SQLException {
		String sql = "INSERT OR IGNORE INTO privileges (accountTypeId, feature) VALUES (?, ?)";

		try (Connection connection = DriverManager.getConnection(database);
			 PreparedStatement stmt = connection.prepareStatement(sql)) {

			int accountTypeId = accountType.getAccountTypeId();

			for (F privilege : accountType.getPrivileges()) {
				stmt.setInt(1, accountTypeId);
				stmt.setString(2, privilege.name());
				stmt.addBatch();
			}

			stmt.executeBatch();
		}
	}


	 boolean deleteAllPrivileges() {

		String sql = "DELETE FROM privileges;";
		try (Connection conn = DriverManager.getConnection(database);
			 PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.execute();
			return true;
		} catch(SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	boolean deleteAllAccountTypes() {
		String sql = "DELETE FROM accountTypes;";
		try (Connection conn = DriverManager.getConnection(database);
			 PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.execute();
			return true;
		} catch(SQLException e) {
			e.printStackTrace();
			return false;
		}
	}


	private  boolean isAccountTypeValid(AccountType<F> accountType) {
		return readAccountTypes().contains(accountType);
	}

	public boolean addNewAccount(Account<F> account) {
		boolean added = false;

		if (account.getName() == null || account.getName().isBlank()) {
			throw new IllegalArgumentException("Cannot add a nameless account");
		}

		AccountType<F> accountType = account.getAccountType();
		if (accountType == null || !isAccountTypeValid(accountType)) {
			throw new IllegalArgumentException("Account type is invalid when inserting account");
		}

		if (isAdminRequired() && !account.isAdmin()) {
			throw new UnsupportedOperationException("Cannot create a non-admin account without an admin account present first");
		}

		String command = "INSERT INTO users (name, password, accountTypeId, salary, workStart, workFinish) "
				+ "VALUES (?,?,?,?,?,?)";

		try (Connection conn = DriverManager.getConnection(database);
			 PreparedStatement stmt = conn.prepareStatement(command, Statement.RETURN_GENERATED_KEYS)) {

			stmt.setString(1, account.getName());
			stmt.setString(2, account.getHashedPassword());
			stmt.setInt(3, account.getAccountType().getAccountTypeId());
			stmt.setInt(4, account.getSalary());

			stmt.setObject(5,
					account.getShiftStart() != null ? account.getShiftStart().format(localTimeformat) : null);

			stmt.setObject(6,
					account.getShiftEnd() != null ? account.getShiftEnd().format(localTimeformat) : null);

			int affectedRows = stmt.executeUpdate();
			added = affectedRows > 0;

			// Retrieve generated ID
			if (added) {
				try (ResultSet rs = stmt.getGeneratedKeys()) {
					if (rs.next()) {
						int id = rs.getInt(1);
						account.setUserId(id); // <-- assign new ID back to object
					}
				}
			}

		} catch (SQLException e) {
			Logger.getLogger("pharmacy-log").severe(
					e.getMessage() + " culprit: " + e.getCause() +
							"\nlocation: " + this.getClass().getEnclosingMethod()
			);
		}

		return added;
	}


	boolean deleteAccount(Account<F> account) {
		if (account == null) {
			return false;
		}

		String command = "DELETE FROM users WHERE userId = ?;";

		try (Connection conn = DriverManager.getConnection(database);
			 PreparedStatement stmt = conn.prepareStatement(command)) {

			stmt.setInt(1, account.getUserId());

			int affectedRows = stmt.executeUpdate();
			return affectedRows > 0;

		} catch (SQLException e) {
			Logger.getLogger("pharmacy-log").severe(
					e.getMessage() + " culprit: " + e.getCause() +
							"\nlocation: " + this.getClass().getEnclosingMethod()
			);
			return false;
		}
	}

}