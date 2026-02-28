package com.moubassher.accountsfx;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ResourceBundle;

public class UserSessionsOverviewCtrl<F extends Enum<F> & Feature<F>> implements Initializable {

	@FXML
	private TableView<Session> sessionTable;
	@FXML
	private TableColumn<Session, String> nameCol, loginCol, logoutCol,
			logoutTimeCol;

	private Class<F> features;

	private String appName;

	public UserSessionsOverviewCtrl(Class<F> features, String appName) {
		this.features = features;
		this.appName = appName;
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {

		AccountDao accountDao = new AccountDao(features, appName);
		sessionTable.setItems(FXCollections.observableArrayList(accountDao.readSessions()));
	}


}