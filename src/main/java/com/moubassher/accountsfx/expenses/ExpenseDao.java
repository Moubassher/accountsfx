package com.moubassher.accountsfx.expenses;

import com.context.our_utilities.DbConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
public class ExpenseDao {

	private DbConfig dbConfig;
	public ExpenseDao(String appName) {
		dbConfig = new DbConfig(appName, "Users.db", "jdbc:sqlite:", this.getClass(), true);
	}

	public List<Expense> readExpenses() {
		String sql = "SELECT expenseId, expenseTitle, cost, date, madeByUserId, note FROM expenses";
		List<Expense> expenses = new ArrayList<>();

		try (Connection connection = dbConfig.openConnection();
			 Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				int expenseId = rs.getInt("expenseId");
				String expenseTitle = rs.getString("expenseTitle");
				int cost = rs.getInt("cost");
				LocalDateTime date = LocalDateTime.parse(rs.getString("date"));
				int madeByUserId = rs.getInt("madeByUserId");
				String note = rs.getString("note");
				Expense expense = new Expense(expenseId, expenseTitle, cost, date, madeByUserId, note);
				expenses.add(expense);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return expenses;
	}


	public boolean addExpense(Expense expense) {
		String sql = "INSERT INTO expenses (expenseTitle, note, cost, date, madeByUserId) VALUES (?, ?, ?, ?, ?)";

		try (Connection connection = dbConfig.openConnection();
			 PreparedStatement ps = connection.prepareStatement(sql)) {
			int index = 1;
			ps.setString(index++, expense.getExpense());
			ps.setString(index++, expense.getNote());
			ps.setInt(index++, expense.getCost());
			ps.setString(index++, expense.getDate().toString());
			ps.setInt(index++, expense.getMadeByUserId());

			return ps.executeUpdate() > 0;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}


	public boolean deleteExpense(Expense expense) {

		String sql = "DELETE FROM expenses WHERE expenseId = ?";

		try (Connection connection = dbConfig.openConnection();
			 PreparedStatement ps = connection.prepareStatement(sql)) {

			ps.setInt(1, expense.getExpenseId());
			return ps.executeUpdate() > 0;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}


}
