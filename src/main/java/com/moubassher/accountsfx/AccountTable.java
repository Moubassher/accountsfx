package com.moubassher.accountsfx;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
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

public class AccountTable<F extends Enum<F> & Feature<F>> extends TableView<Account<F>>{

	TableColumn<Account<F>, String> nameColumn = new TableColumn<>("إسم الحساب");
	TableColumn<Account<F>, AccountType<F>> accountTypeCol = new TableColumn<>("نوع الحساب");
	
	TableColumn<Account<F>, LocalTime> workStartCol = new TableColumn<>("زمن الحضور");
	
	TableColumn<Account<F>, LocalTime> workFinishCol = new TableColumn<>("زمن الإنصراف");
	TableColumn<Account<F>, Integer> salaryColumn = new TableColumn<>("المرتب");
	TableColumn<Account<F>, String> optionsColumn = new TableColumn<>("الخيارات");

	private AccountDao<F> accountDao;

	 AccountTable(AccountDao<F> accountDao) {
		this.accountDao = accountDao;

		this.setEditable(true);

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

		this.getItems().setAll(accountDao.readAccounts());
	}

	public void reloadTable() {
		this.getItems().clear();
		this.setItems(FXCollections.observableArrayList(accountDao.readAccounts()));
	}
	
	private TableCell<Account<F>, String> createCell() {
		TableCell<Account<F>, String> cell = new TableCell<>();
	
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


	private void initiateEditingCell(TableCell<Account<F>, String> cell, Text text) {
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
			TableColumn<Account<F>, String> col = cell.getTableColumn();
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
		boolean done = false; /*accountDao.updateEmployee(attr, attrValue, name);*/
		return done;
	}
	
}
