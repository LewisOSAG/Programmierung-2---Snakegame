package javagc.snake;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

/**
 * Einstellungen mit zugehörigen Widgets und Daten
 */
public class Settings
{

	BorderPane root;
	Integer[] playerCounts = {1, 2, 3, 4};
	Color[] playerColors = {Color.RED, Color.LIME, Color.BLUE, Color.FUCHSIA};
	String[] playerNames = {"Player 1", "Player 2", "Player 3", "Player 4"};
	String[] playerNamesTemp = {"Player 1", "Player 2", "Player 3", "Player 4"};
	String[][] playerControls = {{"W", "A", "S", "D"}, {"", "", "", ""}, {"", "", "", ""}, {"", "", "", ""}};
	String[][] playerControlsTemp = {{"W", "A", "S", "D"}, {"", "", "", ""}, {"", "", "", ""}, {"", "", "", "" }};
	List<VBox> colorSelectionVBoxes = new ArrayList<VBox>();
	Slider[][] sliderList = new Slider[playerCounts.length][3];
	Label status;
	HBox redundantPlayerPopupHbox = new HBox();

	DbContext dbContext;
	int playerCount = 1;
	int playerCountTemp = 1;
	
	public Settings()
	{
	}
	
	public void createSettingsWidgets(BorderPane bPane, DbContext dbContext)
	{	
		/**
		 * Erstellt Widgets für das Settings-Fenster
		 */
		root = bPane;
		this.dbContext = dbContext;
		VBox SettingsBox = new VBox();
		SettingsBox.setSpacing(8);
		SettingsBox.setPadding(new Insets(8));
		Label eL = new Label("Einstellungen");
		eL.setFont(new Font(30));
		
		//Player count selection Widget
		playerCountTemp = playerCount;
		for(int i=0; i<playerNamesTemp.length; i++)
		{
			playerNamesTemp[i] = playerNames[i];
		}
		
		resetPlayerControls();
		
		HBox playerCountHBox = new HBox();

		Label pcL = new Label("Spieleranzahl: ");
		pcL.setFont(new Font(20));
		ChoiceBox<Integer> playerCountCB = new ChoiceBox<>();
		playerCountCB.getItems().addAll(playerCounts);
		playerCountCB.setValue(playerCountTemp);
		playerCountHBox.getChildren().add(pcL);
		playerCountHBox.getChildren().add(playerCountCB);
		//add EventHandler later
		
		//Hbox for color selection
		HBox colorSelectionsHBox = new HBox();
		colorSelectionsHBox.setSpacing(8);
		
		//add EventHandler for playerCountCB, needs colorSelectionsHBox
		EventHandler<ActionEvent>playerCountCBEH = (event) ->
		{
			playerCountTemp = playerCountCB.getValue();

			updateColorSelection(colorSelectionsHBox);
		};
		playerCountCB.setOnAction(playerCountCBEH);
		
		//create / update color selection boxes
		updateColorSelection(colorSelectionsHBox);
		
		Button applyColorSelectionButton = new Button("Anwenden");
		applyColorSelectionButton.setOnAction(this::onPressed);
		status = new Label("Alles OK!");

		if (redundantPlayerPopupHbox.getChildren().isEmpty()) {
			Label redundantPlayerLabel = new Label("Spieler existiert bereits");
			Button redundantPlayerButton = new Button("Informationen übernehmen");
			redundantPlayerButton.setOnAction(this::onPressedRedundantPlayerButton);
			redundantPlayerPopupHbox .getChildren().add(redundantPlayerLabel);
			redundantPlayerPopupHbox.getChildren().add(redundantPlayerButton);
		}
		redundantPlayerPopupHbox.setVisible(false);

		//Display all widgets
		SettingsBox.getChildren().add(eL);
		SettingsBox.getChildren().add(playerCountHBox);
		SettingsBox.getChildren().add(colorSelectionsHBox);
		SettingsBox.getChildren().add(applyColorSelectionButton);
		SettingsBox.getChildren().add(status);
		SettingsBox.getChildren().add(redundantPlayerPopupHbox);
		root.setCenter(SettingsBox);
	}
	
	void updateColorSelection(HBox colorSelectionsHBox)
	{
		/**
		 * Aktualisiert den Farbauswahl-Bereich bei Änderung der Spieleranzahl
		 */
		colorSelectionsHBox.getChildren().clear();
		for(int i=1;  i<=playerCountTemp; i++)
		{
			VBox colorSelectionVBox = new VBox();
			colorSelectionVBox.setSpacing(8);	
			createColorSelection(colorSelectionVBox, i);
			colorSelectionsHBox.getChildren().add(colorSelectionVBox);
			colorSelectionVBoxes.add(colorSelectionVBox);
		}
	}
	
	private void createColorSelection(VBox colorSelectionVBox, int player)
	{
		/**
		 * Erstellt alle Widgets des Farbauswahlbereichs und der Steuerungsoptionen
		 */
		//Player name Label and Text Field
		Label playerNameL = new Label(playerNamesTemp[player - 1]);
		TextField nameTextField = new TextField();
		nameTextField.setText(playerNamesTemp[player - 1]);
		nameTextField.textProperty().addListener((widget, oldV, newV) -> {playerNamesTemp[player - 1] = newV; playerNameL.setText(nameTextField.getText());});
		nameTextField.textProperty().addListener(new ChangeListener<String>()
		{
	        @Override
	        public void changed(final ObservableValue<? extends String> ov, final String oldValue, final String newValue)
	        {
	            if (nameTextField.getText().length() > 10) {
	            	status.setText("Maximale Länge für Namen: 10 Zeichen!");
	                String s = nameTextField.getText().substring(0, 10);
	                nameTextField.setText(s);
	            }
	        }
	    });
		colorSelectionVBox.getChildren().add(playerNameL);
		colorSelectionVBox.getChildren().add(nameTextField);
		Label ctrlInfo = new Label("Steuerung:\n(hoch, links, runter, rechts)");
		TextField controlUp = new TextField()
		{
			@Override
			public void replaceText(int start, int end, String text)
			{
				if(((text.matches("[A-z]|[1-9]") && (ctrlInPlayersCtrl(text.toUpperCase()) == 0)) || text.equals("")))
				{
		        	status.setText("Erlaubte Steuerungsoptionen: Ein Buchstabe oder eine Zahl (ohne Numpad), keine Doppelbelegungen!");
					super.replaceText(start,  end,  text.toUpperCase());
				}
			}
			@Override
			public void replaceSelection(String text)
			{
				if(((text.matches("[A-z]|[1-9]") && (ctrlInPlayersCtrl(text.toUpperCase()) == 0)) || text.equals("")))
				{
		        	status.setText("Erlaubte Steuerungsoptionen: Ein Buchstabe oder eine Zahl (ohne Numpad), keine Doppelbelegungen!");
					super.replaceSelection(text.toUpperCase());
				}
			}
		};
		controlUp.setPromptText("hoch");
		if (!playerControlsTemp[player - 1][0].equals(""))
		{
			controlUp.setText(playerControlsTemp[player - 1][0]);
		}
		controlUp.textProperty().addListener((widget, oldv, newv) -> {playerControlsTemp[player - 1][0] = newv.toUpperCase();});
		//limit to 1 char -> stackoverflow...
		controlUp.textProperty().addListener(new ChangeListener<String>()
		{
	        @Override
	        public void changed(final ObservableValue<? extends String> ov, final String oldValue, final String newValue)
	        {
	            if (controlUp.getText().length() > 1) {
	                String s = controlUp.getText().substring(0, 1);
	                controlUp.setText(s);
	            }
	        }
	    });
		TextField controlLeft = new TextField()
		{
			@Override
			public void replaceText(int start, int end, String text)
			{
				if(((text.matches("[A-z]|[1-9]") && (ctrlInPlayersCtrl(text.toUpperCase()) == 0)) || text.equals("")))
				{
		        	status.setText("Erlaubte Steuerungsoptionen: Ein Buchstabe oder eine Zahl (ohne Numpad), keine Doppelbelegungen!");
					super.replaceText(start,  end,  text.toUpperCase());
				}
			}
			@Override
			public void replaceSelection(String text)
			{
				if(((text.matches("[A-z]|[1-9]") && (ctrlInPlayersCtrl(text.toUpperCase()) == 0)) || text.equals("")))
				{
		        	status.setText("Erlaubte Steuerungsoptionen: Ein Buchstabe oder eine Zahl (ohne Numpad), keine Doppelbelegungen!");
					super.replaceSelection(text.toUpperCase());
				}
			}
		};
		controlLeft.setPromptText("links");
		if (!playerControlsTemp[player - 1][1].equals(""))
		{
			controlLeft.setText(playerControlsTemp[player - 1][1]);
		}
		controlLeft.textProperty().addListener((widget, oldv, newv) -> {playerControlsTemp[player - 1][1] = newv.toUpperCase().toUpperCase();});
		controlLeft.textProperty().addListener(new ChangeListener<String>()
		{
	        @Override
	        public void changed(final ObservableValue<? extends String> ov, final String oldValue, final String newValue)
	        {
	            if (controlLeft.getText().length() > 1) {
	                String s = controlLeft.getText().substring(0, 1);
	                controlLeft.setText(s);
	            }
	        }
	    });
		TextField controlDown = new TextField()
		{
			@Override
			public void replaceText(int start, int end, String text)
			{
				if(((text.matches("[A-z]|[1-9]") && (ctrlInPlayersCtrl(text.toUpperCase()) == 0)) || text.equals("")))
				{
		        	status.setText("Erlaubte Steuerungsoptionen: Ein Buchstabe oder eine Zahl (ohne Numpad), keine Doppelbelegungen!");
					super.replaceText(start,  end,  text.toUpperCase());
				}
			}
			@Override
			public void replaceSelection(String text)
			{
				if(((text.matches("[A-z]|[1-9]") && (ctrlInPlayersCtrl(text.toUpperCase()) == 0)) || text.equals("")))
				{
		        	status.setText("Erlaubte Steuerungsoptionen: Ein Buchstabe oder eine Zahl (ohne Numpad), keine Doppelbelegungen!");
					super.replaceSelection(text.toUpperCase());
				}
			}
		};
		controlDown.setPromptText("runter");
		if (!playerControlsTemp[player - 1][2].equals(""))
		{
			controlDown.setText(playerControlsTemp[player - 1][2]);
		}
		controlDown.textProperty().addListener((widget, oldv, newv) -> {playerControlsTemp[player - 1][2] = newv.toUpperCase();});
		controlDown.textProperty().addListener(new ChangeListener<String>()
		{
	        @Override
	        public void changed(final ObservableValue<? extends String> ov, final String oldValue, final String newValue)
	        {
	            if (controlDown.getText().length() > 1)
	            {
	                String s = controlDown.getText().substring(0, 1);
	                controlDown.setText(s);
	            }
	        }
	    });
		TextField controlRight = new TextField()
		{
			@Override
			public void replaceText(int start, int end, String text)
			{
	        	status.setText("Erlaubte Steuerungsoptionen: Ein Buchstabe oder eine Zahl (ohne Numpad), keine Doppelbelegungen!");
				if(((text.matches("[A-z]|[1-9]") && (ctrlInPlayersCtrl(text.toUpperCase()) == 0)) || text.equals("")))
				{
					super.replaceText(start,  end,  text.toUpperCase());
				}
			}
			@Override
			public void replaceSelection(String text)
			{
	        	status.setText("Erlaubte Steuerungsoptionen: Ein Buchstabe oder eine Zahl (ohne Numpad), keine Doppelbelegungen!");
				if(((text.matches("[A-z]|[1-9]") && (ctrlInPlayersCtrl(text.toUpperCase()) == 0)) || text.equals("")))
				{
					super.replaceSelection(text.toUpperCase());
				}
			}
		};
		controlRight.setPromptText("rechts");
		if (!playerControlsTemp[player - 1][3].equals(""))
		{
			controlRight.setText(playerControlsTemp[player - 1][3]);
		}
		controlRight.textProperty().addListener((widget, oldv, newv) -> {playerControlsTemp[player - 1][3] = newv.toUpperCase();});
		controlRight.textProperty().addListener(new ChangeListener<String>()
		{
	        @Override
	        public void changed(final ObservableValue<? extends String> ov, final String oldValue, final String newValue)
	        {
	            if (controlRight.getText().length() > 1)
	            {
	                String s = controlRight.getText().substring(0, 1);
	                controlRight.setText(s);
	            }
	        }
	    });
		
		//Player color with preview
		Label colorInfo = new Label("Farbe (RGB):");
		Rectangle colorPreviewRect = new Rectangle(32, 32);
		
		Slider colorSliderR = new Slider(0, 255, 0);
		colorSliderR.setShowTickLabels(true);
		colorSliderR.setMajorTickUnit(64);
		
		Slider colorSliderG = new Slider(0, 255, 0);
		colorSliderG.setShowTickLabels(true);
		colorSliderG.setMajorTickUnit(64);
		
		Slider colorSliderB = new Slider(0, 255, 0);
		colorSliderB.setShowTickLabels(true);
		colorSliderB.setMajorTickUnit(64);
		
		sliderList[player - 1][0] = colorSliderR;
		sliderList[player - 1][1] = colorSliderG;
		sliderList[player - 1][2] = colorSliderB;
		
		//change color of preview
		colorSliderR.valueProperty().addListener((obs, oldv, newv) -> {int r = (int) Math.round((Double) newv); colorPreviewRect.setFill(Color.rgb(r, (int) Math.round((Double) colorSliderG.getValue()), (int) Math.round((Double)colorSliderB.getValue())));});
		colorSliderG.valueProperty().addListener((obs, oldv, newv) -> {int g = (int) Math.round((Double) newv); colorPreviewRect.setFill(Color.rgb((int) Math.round((Double) colorSliderR.getValue()), g, (int) Math.round((Double)colorSliderB.getValue())));});
		colorSliderB.valueProperty().addListener((obs, oldv, newv) -> {int b = (int) Math.round((Double) newv); colorPreviewRect.setFill(Color.rgb((int) Math.round((Double) colorSliderR.getValue()), (int) Math.round((Double)colorSliderG.getValue()), b));});

		//set slider to saved values
		colorSliderR.setValue((playerColors[player - 1].getRed() * 255));
		colorSliderG.setValue((playerColors[player - 1].getGreen() * 255));
		colorSliderB.setValue((playerColors[player - 1].getBlue() * 255));
		
		colorSelectionVBox.getChildren().add(colorPreviewRect);
		colorSelectionVBox.getChildren().add(colorInfo);
		colorSelectionVBox.getChildren().add(colorSliderR);
		colorSelectionVBox.getChildren().add(colorSliderG);
		colorSelectionVBox.getChildren().add(colorSliderB);
		colorSelectionVBox.getChildren().add(ctrlInfo);
		colorSelectionVBox.getChildren().add(controlUp);
		colorSelectionVBox.getChildren().add(controlLeft);
		colorSelectionVBox.getChildren().add(controlDown);
		colorSelectionVBox.getChildren().add(controlRight);
	}

	private void onPressedRedundantPlayerButton (ActionEvent e) {
		for(int i=0; i<playerNames.length; i++)
		{
			String currentPlayer = playerNamesTemp[i];
			loadPlayerInformation(currentPlayer, i);
		}
	}

	private void onPressed(ActionEvent e)
	{
		/**
		 * Event Handler des Anwenden-Knopfs
		 */
		playerCount = playerCountTemp;
		
		for(int i=0; i<playerCount; i++)
		{
			int r = (int) Math.round(sliderList[i][0].getValue());
			int g = (int) Math.round(sliderList[i][1].getValue());
			int b = (int) Math.round(sliderList[i][2].getValue());
			if(!checkColor(r, g, b, i))
			{
				sliderList[i][0].setValue(playerColors[i].getRed() * 255);
				sliderList[i][1].setValue(playerColors[i].getGreen() * 255);
				sliderList[i][2].setValue(playerColors[i].getBlue() * 255);
				continue; //Farbe zu dunkel oder zu hell
			}
			playerColors[i] = IntToColor(RGBtoInt(r, g, b));
			saveSettingsToDb(i);

		}
		
		for(int i=0; i<playerNames.length; i++)
		{
			playerNames[i] = playerNamesTemp[i];
		}
		
		for(int i=0; i<playerControls.length; i++)
		{
			for(int j=0; j<playerControls.length; j++)
			{
					playerControls[i][j] = playerControlsTemp[i][j];	
			}
		}
	}

	private void saveSettingsToDb (int currentPlayerIndex) {
		String currentColor = playerColors[currentPlayerIndex].toString();
		String currentPlayer = playerNamesTemp[currentPlayerIndex];
		String currentControlUp = playerControlsTemp[currentPlayerIndex][0];
		String currentControlLeft = playerControlsTemp[currentPlayerIndex][1];
		String currentControlDown = playerControlsTemp[currentPlayerIndex][2];
		String currentControlRight = playerControlsTemp[currentPlayerIndex][3];
		Optional.ofNullable(dbContext).map(DbContext::getConnection).ifPresent(connection -> {
			try {
				if (checkifPlayerExists(connection, currentPlayer).isEmpty()){
					Statement statement = connection.createStatement();
					statement.executeUpdate(createSqlSaveQuery(currentPlayer, currentControlUp, currentControlLeft, currentControlRight, currentControlDown, currentColor));
					redundantPlayerPopupHbox.setVisible(false);
				} else {
					redundantPlayerPopupHbox.setVisible(true);
					System.out.println(redundantPlayerPopupHbox.getChildren());

				}
			} catch (SQLException e) {
				System.out.println("ERROR: Failed to save user information");
				e.printStackTrace();
			}
		});
	}
	/**return the playername if is already exists*/
	private Optional<String> checkifPlayerExists (Connection connection, String username) {
		try {
			Statement statement = connection.createStatement();
			String sqlQuery = "SELECT * FROM usertable WHERE username = '" + username + "'";
			ResultSet resultSet = statement.executeQuery(sqlQuery);
			while (resultSet.next()) {
				String currentUser= resultSet.getString("username");
				System.out.println("User: >" + currentUser +  "< existiert bereits");
				return Optional.of(username);
			}
		} catch (SQLException e) {
			System.out.println("ERROR: Failed to save user information");
			e.printStackTrace();
		}
		return Optional.empty();
	}

	/**load all the user infrmation*/
	private void loadPlayerInformation (String username, int currPlayerIndex) {
		try {
			Statement statement = dbContext.getConnection().createStatement();
			String sqlQuery = "SELECT * FROM usertable WHERE username = '" + username + "'";
			ResultSet resultSet = statement.executeQuery(sqlQuery);
			while (resultSet.next()) {
				String user = resultSet.getString("username");
				String score= resultSet.getString("score");
				String color= resultSet.getString("color");
				String ctrlUp= resultSet.getString("ctrlUp");
				String ctrlDown= resultSet.getString("ctrlDown");
				String ctrlLeft= resultSet.getString("ctrlLeft");
				String ctrlRight= resultSet.getString("ctrlRight");
				int count = 0;
				for (Node node : colorSelectionVBoxes.get(currPlayerIndex).getChildren()) {
					if (node instanceof TextField) {
						switch (count) {
							case 0 : ((TextField) node).setText(user); break;
							case 1 : ((TextField) node).setText(ctrlUp); break;
							case 2 : ((TextField) node).setText(ctrlLeft); break;
							case 3 : ((TextField) node).setText(ctrlDown); break;
							case 4 : ((TextField) node).setText(ctrlRight); break;
						}
						count++;
					} else if (node instanceof Rectangle) {
						int hex = Integer.valueOf("00ff0000", 16);
						int r = (hex & 0xFF0000) >> 16;
						int g = (hex & 0xFF00) >> 8;
						int b = (hex & 0xFF);
						((Rectangle)node).setFill(Color.rgb(r, g, b));
					}

				}

			}
		} catch (SQLException e) {
			System.out.println("ERROR: Failed to load user information");
			e.printStackTrace();
		}
	}
	private String createSqlSaveQuery (String username, String ctrlUp, String ctrlLeft, String ctrlRight, String ctrlDown, String color) {
		return  "INSERT INTO usertable (username, color, ctrlUp, ctrlDown, ctrlLeft, ctrlRight) values ("
				+ "'" + username + "',"
				+ "'" +  color + "',"
				+ "'" +  ctrlUp + "',"
				+ "'" +  ctrlDown + "',"
				+ "'" +  ctrlLeft + "',"
				+ "'" +  ctrlRight + "'"
				+");";
	}
	private boolean checkColor(int r, int g, int b, int playerNumber)
	{
		/**
		 * Überprüft die Farbe eines Spielers, ob sie zu hell, zu dunkel oder zu ähnlich
		 * zur Farbe eines anderen Spielers ist
		 */
		//zu dunkel
		if(r < 80 && g < 80 && b < 80)
		{
			status.setText(playerNames[playerNumber] + ": Farbe zu dunkel!");
			return false;
		}
		//zu hell
		if(r > 200 && g > 200 && b > 200)
		{
			status.setText(playerNames[playerNumber] + ": Farbe zu hell!");
			return false;
		}
		//zu ähnlich
		if(similarColor(r, g, b, playerNumber))
		{
			status.setText(playerNames[playerNumber] + ": Farbe zu ähnlich zur Farbe eines anderen Spielers!");
			return false;
		}
		//ok
		status.setText("Alles OK!");
			return true;
	}
	
	private boolean similarColor(int r, int g, int b, int playerNumber)
	{
		/**
		 * Überprüft, ob eine Farbe ähnlich (+-48) zu einer der anderen Farben ist
		 */
		for(int i=0; i<playerCount; i++)
		{
			if(i == playerNumber)
				continue;	
			if(((Math.abs((playerColors[i].getRed() * 255) - r)) < 48) && ((Math.abs((playerColors[i].getGreen() * 255) - g)) < 48) && ((Math.abs((playerColors[i].getBlue() * 255) - b)) < 48))
				return true;
		}
		return false;
	}
	
	private Color IntToColor(int col)
	{
		/**
		 * Gibt JavaFX-Color aus Farbe in hexadezimalform zurück
		 */
		double r = ((col>>16)&0xFF)/255.0;
		double g = ((col>>8)&0xFF)/255.0;
		double b = ((col>>0)&0xFF)/255.0;
		Color c = new Color(r, g, b, 1.0);
		return c;
	}
	
	private int RGBtoInt(int r, int g, int b)
	{
		/**
		 * Gibt Farbe in Hexadezimalform aus einzelnen Rot-, Grün und Blauwerten zurück
		 */
		int c = 0;
		c = c + ((r&0xFF) << 16);
		c = c + ((g&0xFF) << 8);
		c = c + ((b&0xFF) << 0);
		return(c);
	}
	
	private int ctrlInPlayersCtrl(String c)
	{
		/**
		 * Testet, ob dine Taste bereits belegt ist
		 */
		int cnt = 0;
		for(int i=0; i<playerCountTemp; i++)
		{
			for(int j=0; j<playerControls.length; j++)
			{
				if(playerControlsTemp[i][j].equals(c))
				{
					cnt++;
				}
			}
		}
		return cnt;
	}
	
	private void resetPlayerControls()
	{
		/**
		 * Setzt die temporären Steuerungsoptionen zurück
		 */
		for(int i=0; i<playerControlsTemp.length; i++)
		{
			for(int j=0; j<playerControlsTemp.length; j++)
			{
				playerControlsTemp[i][j] = playerControls[i][j];
			}
		}
	}

	public Color[] getPlayerColors()
	{
		/**
		 * Gibt die Farben aller Spieler zurück
		 */
		return playerColors;
	}
	
	public String getPlayerNControlUp(int n)
	{
		/**
		 * Gibt die Taste für "hoch" des Spielers N zurück
		 */
		return playerControls[n][0];
	}
	
	public String getPlayerNControlLeft(int n)
	{
		/**
		 * Gibt die Taste für "links" des Spielers N zurück
		 */
		return playerControls[n][1];
	}
	
	public String getPlayerNControlDown(int n)
	{
		/**
		 * Gibt die Taste für "runter" des Spielers N zurück
		 */
		return playerControls[n][2];
	}
	
	public String getPlayerNControlRight(int n)
	{
		/**
		 * Gibt die Taste für "rechts" des Spielers N zurück
		 */
		return playerControls[n][3];
	}
}
