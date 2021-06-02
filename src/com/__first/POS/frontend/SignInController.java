package com.__first.POS.frontend;

import java.io.IOException;
import java.sql.SQLException;

import com.__first.POS.backend.DatabaseInterface;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class SignInController {

	private static final String PATH_TO_CHEF="/com/__first/POS/frontend/Chef.fxml";
	private static final String PATH_TO_SERVER="/com/__first/POS/frontend/Server.fxml";
	private static final String PATH_TO_MANAGER="/com/__first/POS/frontend/Manager.fxml";

	@FXML
	private TextField passwordField;

	@FXML
	private TextField usernameField;

	@FXML
	private Button signInButton;

	@FXML
	private Text roleText;

	public void initialize() {

	}

	@FXML
	void signIn(ActionEvent event) throws IOException {
		String user=usernameField.getText();
		String pass=passwordField.getText();

		int role;
		try {
			DatabaseInterface dbi=DatabaseInterface.getInstance();
			role=dbi.signIn(user, pass);
		}catch(SQLException e) {
			popup("Database Error", "Error: "+e.getMessage());
			return;
		}

		changeScene(role);

	}

	/**
	 * Changes the scene to the specified fxml path
	 * 
	 * @param pathToScene
	 * @throws IOException
	 */

	private void changeScene(int role) throws IOException {

		// CHEFF 
		if(role==0) {
			changeSceneHelper(PATH_TO_CHEF);

		}

		// SERVER
		else if(role==1) {
			changeSceneHelper(PATH_TO_SERVER);
		}

		// MANAGER
		else if(role==2) {
			changeSceneHelper(PATH_TO_MANAGER);
		}

		else {
			popup("Login Error", "Invalid Username or Password");
		}

	}
	
	private void changeSceneHelper(String path) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource(path));
		Scene s = new Scene(root);
		
		s.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		
		Stage current = (Stage) signInButton.getScene().getWindow();
		Stage window = new Stage();
		window.setScene(s);
		window.centerOnScreen();
		window.show();
		current.close();
	} 

	/**
	 * Popup dialog
	 * 
	 * @param title   the title of the popup dialog
	 * @param message the message of the popup dialog
	 */
	public void popup(String title, String message) {
		Alert alert=new Alert(AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);

		alert.showAndWait();
	}
	
	@FXML
	void textFieldKeyPressed(KeyEvent event) throws IOException {
		if(event.getCode()==KeyCode.ENTER) {
			signIn(null);
		}
	}

}
