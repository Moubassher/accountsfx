package com.moubassher.accountsfx.accessControl;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class AttendanceCtrl implements Initializable {

	@FXML
	private TableView<Login> attendanceTable;
	@FXML
	private TableColumn<Login, String> nameCol, loginDateCol, loginTimeCol, logoutDateCol,
			logoutTimeCol;
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		nameCol.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().employeeName()));
		loginDateCol.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().loginDate()));
		loginTimeCol.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().loginTime()));
		logoutDateCol.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().logoutDate()));
		logoutTimeCol.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().logoutTime()));

		AccountDao accountDao = AccountDao.getInstance();
		attendanceTable.setItems(FXCollections.observableArrayList(accountDao.readLogins()));
	}
	public AttendanceCtrl() {

		/*
		nameCol.setCellFactory(callback -> {
			TableCell<Login, String> cell = createCell();
			return cell;
		});
		loginDateCol.setCellFactory(callback -> {
			TableCell<Login, String> cell = createCell();
			return cell;
		});
		
		loginDateCol.setCellFactory(callback -> {
			TableCell<Login, String> cell = createCell();
			return cell;
		});
		
		logoutDateCol.setCellFactory(callback -> {
			TableCell<Login, String> cell = createCell();
			return cell;
		});
		
		logoutTimeCol.setCellFactory(callback -> {
			TableCell<Login, String> cell = createCell();
			return cell;
		});
		*/
	}

	private static TableCell<Login, String> createCell() {

		TableCell<Login, String> cell = new TableCell<>();

		Text text = new Text();
		cell.setGraphic(text);
		cell.setEditable(true);

		text.textProperty().bind(cell.itemProperty());

		return cell;
	}

}
