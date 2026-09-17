package com.moubassher.accountsfx.expenses;

import com.context.my_javafx_utilities.DbConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
public class ExpenseDao {

	private DbConfig userDbConfig;
	public ExpenseDao(DbConfig userDbConfig) {
		this.userDbConfig = userDbConfig;
	}

	public List<Expense> readUnapprovedExpenses() {
		return readExpensesByApprovalStatus(false);
	}

	public List<Expense> readApprovedExpenses() {
		return readExpensesByApprovalStatus(true);
	}

	private List<Expense> readExpensesByApprovalStatus(boolean approvedStatus) {
		String sql = "SELECT expenseId, expenseTitle, cost, date, madeByUserId, note, approved FROM expenses WHERE approved = ?";
		List<Expense> expenses = new ArrayList<>();

		try (Connection connection = userDbConfig.openConnection();
			 PreparedStatement ps = connection.prepareStatement(sql)) {

			ps.setBoolean(1, approvedStatus);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					int expenseId = rs.getInt("expenseId");
					String expenseTitle = rs.getString("expenseTitle");
					int cost = rs.getInt("cost");
					LocalDateTime date = LocalDateTime.parse(rs.getString("date"));
					int madeByUserId = rs.getInt("madeByUserId");
					String note = rs.getString("note");
					boolean approved = rs.getBoolean("approved");
					expenses.add(new Expense(expenseId, expenseTitle, cost, date, madeByUserId, note, approved));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return expenses;
	}

	public List<Expense> readAllExpenses() {
		String sql = "SELECT expenseId, expenseTitle, cost, date, madeByUserId, note, approved FROM expenses";
		List<Expense> expenses = new ArrayList<>();

		try (Connection connection = userDbConfig.openConnection();
			 Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				int expenseId = rs.getInt("expenseId");
				String expenseTitle = rs.getString("expenseTitle");
				int cost = rs.getInt("cost");
				LocalDateTime date = LocalDateTime.parse(rs.getString("date"));
				int madeByUserId = rs.getInt("madeByUserId");
				String note = rs.getString("note");
				boolean approved = rs.getBoolean("approved");
				expenses.add(new Expense(expenseId, expenseTitle, cost, date, madeByUserId, note, approved));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return expenses;
	}


	public boolean addExpense(Expense expense) {
		String sql = "INSERT INTO expenses (expenseTitle, note, cost, date, madeByUserId," +
				"approved) VALUES (?, ?, ?, ?, ?,?)";

		try (Connection connection = userDbConfig.openConnection();
			 PreparedStatement ps = connection.prepareStatement(sql)) {
			int index = 1;
			ps.setString(index++, expense.getExpense());
			ps.setString(index++, expense.getNote());
			ps.setInt(index++, expense.getCost());
			ps.setString(index++, expense.getTimestamp().toString());
			ps.setInt(index++, expense.getMadeByUserId());
			ps.setBoolean(index++, false);
			return ps.executeUpdate() > 0;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean approveExpense(Expense expense) {
		String sql = "UPDATE expenses SET approved = ? WHERE expenseId = ?";

		try (Connection connection = userDbConfig.openConnection();
			 PreparedStatement ps = connection.prepareStatement(sql)) {

			ps.setBoolean(1, true);
			ps.setInt(2, expense.getExpenseId());

			return ps.executeUpdate() > 0;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean unapproveExpense(Expense expense) {
		String sql = "UPDATE expenses SET approved = ? WHERE expenseId = ?";

		try (Connection connection = userDbConfig.openConnection();
			 PreparedStatement ps = connection.prepareStatement(sql)) {

			ps.setBoolean(1, false);
			ps.setInt(2, expense.getExpenseId());

			return ps.executeUpdate() > 0;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}


	public boolean deleteExpense(Expense expense) {

		String sql = "DELETE FROM expenses WHERE expenseId = ?";

		try (Connection connection = userDbConfig.openConnection();
			 PreparedStatement ps = connection.prepareStatement(sql)) {

			ps.setInt(1, expense.getExpenseId());
			return ps.executeUpdate() > 0;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}




}
