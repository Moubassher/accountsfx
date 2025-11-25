package com.moubassher.accountsfx.accessControl;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.time.LocalTime;

public class AccountTable extends TableView<Account>{

	TableColumn<Account, String> nameColumn = new TableColumn<>("إسم الحساب");
	
	TableColumn<Account, AccountType> accountTypeCol = new TableColumn<>("نوع الحساب");
	
	TableColumn<Account, LocalTime> workStartCol = new TableColumn<>("زمن الحضور");
	
	TableColumn<Account, LocalTime> workFinishCol = new TableColumn<>("زمن الإنصراف");
	TableColumn<Account, Integer> salaryColumn = new TableColumn<>("المرتب");
	TableColumn<Account, String> optionsColumn = new TableColumn<>("الخيارات");

	AccountDao empDatabase = AccountDao.getInstance();

	private static AccountTable instance = null;

	public static AccountTable getInstance() {
		if (instance == null) {
			instance = new AccountTable();
		}
		return instance;
	}
	
	private AccountTable() {
		
		this.setEditable(true);
		
		this.setId("employee-table"); //$NON-NLS-1$
	
		nameColumn.setCellValueFactory(x -> new ReadOnlyObjectWrapper<>(x.getValue().getName()));
		accountTypeCol.setCellValueFactory(x -> new ReadOnlyObjectWrapper<>(x.getValue().getAccountType()));
		workStartCol.setCellValueFactory(x -> new ReadOnlyObjectWrapper<>(x.getValue().getShiftStart()));
		workFinishCol.setCellValueFactory(x -> new ReadOnlyObjectWrapper<>(x.getValue().getShiftEnd()));
		//salaryColumn.setCellValueFactory(x -> new ReadOnlyObjectWrapper<Integer>(x.getValue().salary()));
		
		this.getColumns().add(nameColumn);
		this.getColumns().add(accountTypeCol);
		this.getColumns().add(workStartCol);
		this.getColumns().add(workFinishCol);
		this.getColumns().add(salaryColumn);
		this.getColumns().add(optionsColumn);
		
		this.getColumns().forEach(col -> {
			col.setEditable(true);
		});

		this.getItems().setAll(empDatabase.readAccounts());
	}

	public void reloadTable() {
		this.getItems().clear();
		this.setItems(empDatabase.readAccounts());
	}
	
	private TableCell<Account, String> createCell() {
		TableCell<Account, String> cell = new TableCell<>();
	
		Text text = new Text();
		cell.setGraphic(text);
		cell.setEditable(true);

		cell.editingProperty().addListener((a, b, c) -> {
			if(a.getValue() == true) {
				
				initiateEditingCell(cell, text);

			} else {	//else: cell is not editing:
				cell.cancelEdit();
				cell.setGraphic(text);
			}
				
		});
		
		return cell;
	}


	private void initiateEditingCell(TableCell<Account, String> cell, Text text) {
		//values to update in the database are obtained this way:
		int index = cell.getIndex();
		String name = nameColumn.getCellData(index);

		TextArea textArea = new TextArea();
		textArea.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

		textArea.setMaxWidth(cell.getWidth());
		textArea.setMaxHeight(cell.getHeight());
		textArea.setWrapText(true);

		textArea.setText(text.getText());

		Button OK = new Button("تعديل");
		OK.setOnAction(x -> {
			TableColumn<Account, String> col = cell.getTableColumn();
			String attr = "";
			
			String attrValue = textArea.getText();
			boolean success = editOkAction(attr, attrValue, name);
			
			if(success) {
				cell.commitEdit(textArea.getText());
				cell.setGraphic(text);
			}
			
		});
		
		Button Cancel = new Button("إلغاء التعديل");
		
		HBox editingButtonsPane= new HBox();

		editingButtonsPane.getChildren().addAll(OK, Cancel);
		VBox box = new VBox(5);
		box.widthProperty().add(cell.getWidth());
		box.getChildren().add(textArea);

		//show buttons for committing and cancelling editing of the cell if I hover with the mouse 
		//over it. 
		box.hoverProperty().addListener((x, y, z) -> {
			if(x.getValue() == true) {
				box.getChildren().add(editingButtonsPane);
			} else {
				box.getChildren().clear();
				box.getChildren().add(textArea);
			}
		});
		cell.setGraphic(box);
	}	
	
	public boolean editOkAction(String attr, String attrValue, String name) {
		boolean done = empDatabase.updateEmployee(attr, attrValue, name);
		return done;
	}
	
}
