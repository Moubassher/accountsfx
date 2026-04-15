package com.moubassher.accountsfx;

import com.context.our_utilities.DbConfig;

import java.sql.Connection;
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
	

	DateTimeFormatter localTimeformat =
			DateTimeFormatter.ofPattern("hh:mm a").localizedBy(Locale.forLanguageTag("AR"));

	private Class<F> featureEnum;

	private DbConfig dbConfig;
	public AccountDao(Class<F> featureEnum, String appName) {
		this.featureEnum = featureEnum;
		dbConfig = new DbConfig(appName, "Users.db", "jdbc:sqlite:", this.getClass(), false);
	}


	public AccountType<F> getAccountType(int accountTypeId) {
		return readAccountTypes().stream()
				.filter(accountType -> accountType.getAccountTypeId() == accountTypeId)
				.findFirst().orElse(null);

	}

	public Account<F> getAccount(int accountId) {
		return readAccounts().stream()
				.filter(account -> account.getUserId() == accountId)
				.findFirst().get();
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

		try (Connection conn = dbConfig.openConnection();
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
				"UPDATE user_sessions SET logoutAt = ? WHERE userId = ? AND logoutAt IS NULL"; //$NON-NLS-1$

		try (Connection conn = dbConfig.openConnection();
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


	public List<Session> readSessions() {
		String query = "SELECT userId, loginAt, logoutAt FROM user_sessions"; //$NON-NLS-1$
		List<Session> sessions = new ArrayList<>();

		var dateTimeFormatter = DateTimeFormatter.ofPattern("E yyyy/M/dd HH:mm a")
				.localizedBy(Locale.forLanguageTag("ar"));

		try (Connection conn = dbConfig.openConnection();
			 PreparedStatement stmt = conn.prepareStatement(query);
			 ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				int userId = rs.getInt("userId");

				String loginStr = rs.getString("loginAt");
				String logoutStr = rs.getString("logoutAt");

				LocalDateTime login = LocalDateTime.parse(loginStr, dateTimeFormatter);
				LocalDateTime logout = logoutStr != null ? LocalDateTime.parse(logoutStr, dateTimeFormatter) : null;

				sessions.add(new Session(userId, login, logout));
			}

		} catch (SQLException e) {
			Logger.getLogger("pharmacy-log").severe(e.getMessage() + " culprit is: " + e.getCause() + "\nlocation: " + this.getClass().getEnclosingMethod());
		}

		return sessions;
	}


	public List<Account<F>> readPendingAccounts() {
		return readAccounts("users_pending");
	}

	public List<Account<F>> readAccounts() {
		return readAccounts("users");
	}


	private List<Account<F>> readAccounts(String table) {

		List<Account<F>> allAccounts = new ArrayList<>();

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
						"FROM " + table + ";";

		try (Connection conn = dbConfig.openConnection();
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

		String sql = """
						SELECT a.accountTypeId,
						       a.accountTypeName,
						       a.accountClass,
						       GROUP_CONCAT(p.feature) AS privileges
						FROM accountTypes a
						LEFT JOIN privileges p
						       ON p.accountTypeId = a.accountTypeId
						GROUP BY a.accountTypeId,
						         a.accountTypeName,
						         a.accountClass
						""";

		try (Connection conn = dbConfig.openConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				int accountTypeId = rs.getInt("accountTypeId");
				String type = rs.getString("accountTypeName");

				String privilegeString = rs.getString("privileges");
				List<F> privileges;
				if (privilegeString == null || privilegeString.isEmpty()) {
					privileges = new ArrayList<>();
				} else {
					privileges = Arrays.stream(privilegeString.split(","))
							.map(s -> Enum.valueOf(featureEnum, s)) // convert String to enum
							.toList();
				}

				AccountType.Class accountClass = AccountType.Class.valueOf(rs.getString("accountClass"));
				AccountType<F> accountType = new AccountType(accountTypeId, type, privileges, accountClass);
				accountTypes.add(accountType);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return accountTypes;
	}

	public boolean hasNonMaintainerAccountType() {
		boolean hasAccountTypes=  ! readAccountTypes().stream().allMatch(accountType -> accountType.getAccountClass().equals(AccountType.Class.MAINTAINER));
		return hasAccountTypes;
	}

	public  List<AccountType<F>> readAccountTypes() {
		List<AccountType<F>> accountTypes = new ArrayList<>();

		String sql =
				"""
						SELECT a.accountTypeId,
						       a.accountTypeName,
						       a.accountClass,
						       GROUP_CONCAT(p.feature) AS privileges
						FROM accountTypes a
						LEFT JOIN privileges p
						       ON p.accountTypeId = a.accountTypeId
						GROUP BY a.accountTypeId,
						         a.accountTypeName,
						         a.accountClass
						""";

		try (Connection conn = dbConfig.openConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				int accountTypeId = rs.getInt("accountTypeId");
				String type = rs.getString("accountTypeName");

				String privilegeString = rs.getString("privileges");
				List<F> privileges;
				if (privilegeString == null || privilegeString.isEmpty()) {
					privileges = new ArrayList<>();
				} else {
					privileges = Arrays.stream(privilegeString.split(","))
							.map(s -> Enum.valueOf(featureEnum, s)) // convert String to enum
							.toList();
				}

				AccountType.Class accountClass = AccountType.Class.valueOf(rs.getString("accountClass"));
				AccountType<F> accountType = new AccountType(accountTypeId, type, privileges, accountClass);
				accountTypes.add(accountType);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return accountTypes;
	}


	public void createMaintainerAccount() {
		List<F> privileges = List.of(featureEnum.getEnumConstants());
		AccountType<F> maintainerAccount = new AccountType<>("Maintainer", privileges, AccountType.Class.MAINTAINER);

		boolean maintainerTypeCreated = addAccountType(maintainerAccount);
		if(maintainerTypeCreated) {
			String password = "perseverence123$";
			String hashedPassword = new PasswordValidator().hashPassword(password);
			Account<F> maintainer = new Account<>("Moubassher", hashedPassword, maintainerAccount);
			approveNewAccount(maintainer);
		}

	}

	public void deleteAllAccounts() {
		String sql = "DELETE FROM users";
		try (Connection conn = dbConfig.openConnection()
			 ;PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean resetPassword(Account<F> account, String newPassword) {
		boolean reset = false;
		String command = "UPDATE users SET password = ? WHERE userId = ?";
		try (Connection conn = dbConfig.openConnection()) {
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
				.noneMatch(account -> account.isAdmin());
	}

	public boolean hasPendingAccounts() {
		return !readPendingAccounts().isEmpty();
	}

	private boolean verifyLoginCredentials(int userId, String hashedPassword) {

		String command = "SELECT userId, password from users WHERE userId = ? AND password = ?;"; //$NON-NLS-1$
		boolean validCreds = false;

		try(Connection conn = dbConfig.openConnection()) {
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

		try (Connection conn = dbConfig.openConnection();
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


		String sql = "INSERT OR IGNORE INTO accountTypes (accountTypeName, accountClass) VALUES (?, ?)";

		try (Connection conn = dbConfig.openConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			stmt.setString(1, accountType.getTypeName());
			stmt.setString(2, accountType.getAccountClass().toString());

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
		String newType = accountType.getTypeName().toLowerCase();
		return readAccountTypes().stream()
				.map(type -> type.getTypeName().toLowerCase())
				.noneMatch(name -> name.equals(newType));
	}


	public boolean updateAccountType(int accountTypeId, AccountType<F> accountType) {
		String sqlUpdateType = "UPDATE accountTypes SET accountTypeName = ?, accountClass = ? WHERE accountTypeId = ?";

		try (Connection connection = dbConfig.openConnection();
			 PreparedStatement stmt = connection.prepareStatement(sqlUpdateType)) {

			stmt.setString(1, accountType.getTypeName());
			stmt.setString(2, accountType.getAccountClass().toString());
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

		try (Connection conn = dbConfig.openConnection()) {
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
			try { dbConfig.openConnection().rollback(); } catch (Exception ignored) {}

			return false;
		}
	}

	public boolean deleteAccountTypes() {
		String sql = "DELETE FROM accountTypes";
		try(Connection conn = dbConfig.openConnection();
		PreparedStatement stmt = conn.prepareStatement(sql)) {
			int rowsAffected = stmt.executeUpdate();
			return rowsAffected > 0;
		} catch (SQLException e) {
            e.printStackTrace();
			return false;
        }
    }

	public void addPrivileges(AccountType<F> accountType) throws SQLException {
		String sql = "INSERT OR IGNORE INTO privileges (accountTypeId, feature) VALUES (?, ?)";

		try (Connection connection = dbConfig.openConnection();
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


	 public boolean deleteAllPrivileges() {

		String sql = "DELETE FROM privileges;";
		try (Connection conn = dbConfig.openConnection();
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

	public boolean approveNewAccount(Account<F> account) {
		int pendingId = account.getUserId();
		boolean added = addAccountDetails(account, "users");
		if (added) {
			return deletePendingUser(pendingId);
		}
		return false;
	}

	public boolean requestAccountApproval(Account<F> account) {
		return addAccountDetails(account, "users_pending");
	}

	public boolean deletePendingUser(int userId)  {
		String sql = "DELETE FROM users_pending WHERE userId = ?";

		try (Connection connection = dbConfig.openConnection();
			 PreparedStatement ps = connection.prepareStatement(sql)) {

			ps.setInt(1, userId);
			int affected = ps.executeUpdate();
			return affected > 0;
		} catch (SQLException e) {
            e.printStackTrace();
			return false;
        }
    }

	private boolean addAccountDetails(Account<F> account, String table) {
		boolean added = false;

		if (account.getName() == null || account.getName().isBlank()) {
			throw new IllegalArgumentException("Cannot add a nameless account");
		}

		AccountType<F> accountType = account.getAccountType();
		if (accountType == null || !isAccountTypeValid(accountType)) {
			throw new IllegalArgumentException("Account type is invalid when inserting account");
		}

		if (isAdminRequired() && !account.isAdmin() && !(accountType.getAccountClass() == AccountType.Class.MAINTAINER)) {
			throw new UnsupportedOperationException("Cannot create a non-admin account without an admin account present first");
		}

		String command = "INSERT INTO "+ table +"  (name, password, accountTypeId, salary, workStart, workFinish) "
				+ "VALUES (?,?,?,?,?,?)";

		try (Connection conn = dbConfig.openConnection();
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
			e.printStackTrace();
		}

		return added;

	}


	boolean deleteAccount(Account<F> account) {
		if (account == null) {
			return false;
		}

		String command = "DELETE FROM users WHERE userId = ?;";

		try (Connection conn = dbConfig.openConnection();
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