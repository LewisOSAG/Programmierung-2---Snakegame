package javagc.snake;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javagc.snake.Point.ORIENTATION;
import javagc.snake.SnakeLogic.KeyBindings;

public class Main extends Application {

	// Spielvariablen
	private List<Snake> players = new ArrayList<>();
	private int playerNum;
	private ScheduledThreadPoolExecutor timer;
	private List<ScheduledFuture<?>> futureTasks;
	private Connection connection;
	DbContext dbContext;
	private Stage stage;
	private HBox menu;
	private BorderPane defScreen;
	private BorderPane setScreen;
	private BorderPane gameScreen;
	private AtomicBoolean gameRunning;
	private Map<String, Integer> scoreBoardData;

	private final double objectScale = 12.5;

	private Settings settingsWindow = new Settings();
	private SnakeMenu sMenu = new SnakeMenu();
	private Assets assets;

	private SnakeCollisionHandler colHandler;
	private ScoreChangedListener sCL;

	@Override
	public void start(Stage stage) {
		this.stage = stage;
		connectToDb(); // Verbindung zur Datenbank aufbauen
		gameRunning = new AtomicBoolean(false);
		menu = createMenu(); // Menü erstellen
		defScreen = createWidgets(createContent()); // Standardbildschirm erstellen
		setScreen = createSettings(); // Einstellungsbildschirm erstellen

		timer = new ScheduledThreadPoolExecutor(5);
		futureTasks = new ArrayList<>();

		var root = defScreen;
		Scene scene = new Scene(root);

		stage.setTitle("JFX Snake");	
		stage.setScene(scene);
		stage.setOnCloseRequest((e) -> {
			onQuit(null); // Anwendung beenden beim Schließen des Fensters
		});

		stage.setResizable(false);
		stage.show();
	}
 	// Methode zur Herstellung der Datenbankverbindung
	private void connectToDb (){
		try {
			 // Lokale Datenbankverbindung erstellen in Anwenders home directory
			String url = "jdbc:h2:~/test";
			String username = "sa";
			String pw = "123";
			connection = DriverManager.getConnection(url, username, pw);
			System.out.println("Connected to db");
			createSqlTable(connection); // Tabelle in der Datenbank erstellen
			String sql = "SELECT * FROM usertable";
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				String currentUser= resultSet.getString("username");
				System.out.println("User:" + currentUser);
			}
			dbContext = new DbContext(connection);
			scoreBoardData = loadScoreboardData(); // Bestenliste laden 
			System.out.println(scoreBoardData);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Terminate application...");
			System.exit(0);
		}
	}
	// Methode zur Erstellung der Datenbanktabelle
	private void createSqlTable (Connection connection) throws SQLException {
		String createUserTableQuery = "CREATE TABLE IF NOT EXISTS usertable "
				 + "(username VARCHAR(32), "
				 + "score INT, "
                 + "color VARCHAR(12), "
				 + "ctrlUp VARCHAR(1), "
				 + "ctrlDown VARCHAR(1), "
				 + "ctrlLeft VARCHAR(1), "
				 + "ctrlRight VARCHAR(1) "
				 + ");";
		Statement statement = connection.createStatement();
		statement.executeUpdate(createUserTableQuery);
	}
	// Erstellung des Inhalts für das Spielfeld
	private Pane createContent() {
		var canvas = new Pane();

		canvas.setMinSize(900, 750);
		canvas.setBackground(new Background(new BackgroundFill(Paint.valueOf("BLACK"), null, null)));

		return canvas;
	}
	// Erstellung der Widgets für den Bildschirm
	private BorderPane createWidgets(Pane contentScreen) {
		var root = new BorderPane();

		root.setTop(menu);
		root.setCenter(contentScreen);
		root.centerProperty();

		return root;
	}
	// Erstellung des Einstellungsbildschirms
	private BorderPane createSettings() {
		var root = new BorderPane();
 		// Hintergrund des Einstellungsbildschirms erstellen
		root.setBackground(new Background(
				new BackgroundFill(
						new LinearGradient(0, 0, 0, 1, true,
								CycleMethod.NO_CYCLE,
								new Stop(0, Color.web("#FEFEFE")),
								new Stop(1, Color.web("#FFCFCF"))
								), null, null)));
		// Button zum Zurückkehren zum Spiel erstellen und konfigurieren						
		var ret = new Button("Back to Game");
		ret.setAlignment(Pos.BOTTOM_RIGHT);
		ret.setOnAction(this::onBackToGame);
		var ctrlBox = new HBox(ret);
		ctrlBox.setMaxWidth(Double.MAX_VALUE);
		ctrlBox.setPadding(new Insets(5));
		ctrlBox.setSpacing(5);
		ctrlBox.setAlignment(Pos.CENTER_RIGHT);
		root.setBottom(ctrlBox);

		// Einstellungen erstellen, falls dbContext nicht null ist
		Optional.ofNullable(dbContext).ifPresent(ctx -> settingsWindow.createSettingsWidgets(root, dbContext));

		return root;
	}

	private void updateSettings() {
		// Aktualisierung der Einstellungen
		Optional.ofNullable(dbContext).ifPresent(ctx -> settingsWindow.createSettingsWidgets(setScreen, dbContext));
	}
	// Menü erstellen
	private HBox createMenu() {
		HBox menuBox = new HBox();
		var menuBar = new MenuBar();

		Menu game = new Menu("Game");
		MenuItem settings = new MenuItem("Settings");
		settings.setOnAction(this::onOpenSettings);
		MenuItem startGame = new MenuItem("Start New Game");
		startGame.setOnAction(this::onStartNewGame);
		startGame.setAccelerator(new KeyCodeCombination(KeyCode.F2));

		game.getItems().addAll(settings, startGame);
		if(gameScreen != null) {
			MenuItem gameManip;
			// Überprüfen, ob das Spiel läuft, um die entsprechende Option im Menü anzuzeigen
			if(!gameRunning.get()) {
				gameManip = new MenuItem("Continue Game");
				gameManip.setOnAction(this::onGameToActiveState);
			}
			else {
				gameManip = new MenuItem("Pause Game");
				gameManip.setOnAction(this::onGameToPausedState);
				gameManip.setAccelerator(new KeyCodeCombination(KeyCode.ESCAPE));
			}
			game.getItems().add(gameManip);
		}
		menuBar.getMenus().add(game);
		menuBox.getChildren().add(menuBar);

		// Snake-Menu erstellen und im Menü anzeigen
		sMenu.createMenuWidgets(menuBox, settingsWindow.playerCount, settingsWindow.playerNames, settingsWindow.playerColors);

		return menuBox;
	}
	 // Aktualisiert das Menü auf der Oberseite des gegebenen BorderPane
	private void updateMenu(BorderPane root) {
		menu = createMenu();
		root.setTop(menu);
	}
	// Erstellt die Game Over-Bildschirmansicht mit den entsprechenden Score- und Spielerdaten
	private BorderPane createGOScreen() {
		var root = new BorderPane();
		VBox box = new VBox();
		HBox scores = new HBox();
		HBox scoreboard = new HBox();
		scoreboard.setSpacing(5.0);
		scoreboard.setAlignment(Pos.CENTER);
		// Hintergrundfarbe setzen und 'Zurück zum Spiel'-Button erstellen
		root.setBackground(new Background(new BackgroundFill(Paint.valueOf("WHITE"), null, null)));
		var ret = new Button("Back to Game");
		ret.setOnAction(this::onBackToGame);
		Label go = new Label("Game Over!");
		go.setFont(new Font(100));

		// Scores und Scoreboard-Label einrichten
		if(playerNum == 1) {
			Label scr = new Label("Score: " + sMenu.getScores()[0]);
			scr.setFont(new Font(40));
			savePlayerScore(settingsWindow.playerNames[0],sMenu.getScores()[0]);
			scores.getChildren().add(scr);
		} else {
			for(int i = 0; i < playerNum; i++) {
				Label scr = new Label("Player " + (i+1) + ": " + sMenu.getScores()[i] + "    ");
				scr.setFont(new Font(20));
				savePlayerScore(settingsWindow.playerNames[i],sMenu.getScores()[i]);
				scores.getChildren().add(scr);
			}
		}
		int count = 1;
		Label scoreboardLabel = new Label("Top 3 Player Scores:");
		for (Map.Entry<String, Integer> entry :  scoreBoardData.entrySet()) {
			Label place = new Label(count + ".");
			Label name = new Label("Name: " + entry.getKey());
			Label score = new Label("Total score: " + entry.getValue());
			VBox scoreboardVbox = new VBox();
			scoreboardVbox.getChildren().addAll(place, name, score);
			scoreboard.getChildren().add(scoreboardVbox);
			count++;
		}
		// Hinzufügen der erstellten Elemente zum Layout
		scores.setAlignment(Pos.CENTER);
		box.setAlignment(Pos.CENTER);
		box.setSpacing(30);
		box.getChildren().addAll(go, scores, ret, scoreboardLabel, scoreboard);
		root.setCenter(box);

		return root;
	}
	// Speichert den Punktestand des Spielers in der Datenbank
	private void savePlayerScore (String playerName, int score) {
		System.out.println(players);
		String sql = "UPDATE usertable " +
				"SET score = "+ score+" WHERE username='"+playerName+"'";
		try {
			connection.createStatement().executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	// Lädt die Ranglisten-Daten aus der Datenbank und gibt sie als Map zurück
	private Map<String, Integer> loadScoreboardData () {
		String sql = "SELECT * FROM  usertable " +
				"ORDER BY score DESC";
		Map<String, Integer> userscoreMap = new HashMap<String, Integer>();
		try {
			Statement statement = connection.createStatement();
			statement.setMaxRows(3);
			ResultSet resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				String username = resultSet.getString("username");
				int score = resultSet.getInt("score");
				userscoreMap.put(username, score);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return userscoreMap;
	}
	// Erstellt das Spielumfeld basierend auf der Spieleranzahl
	private Pane createGameEnv(Integer playerNum) {
		// Aktualisiert die Spiel-Scores
		sMenu.setScores();
		// Erstellt das Spiel-Feld
		var gameField = createContent();
		// Initialisiert Assets für das Spiel
		assets = new Assets(gameField, objectScale);

		if(playerNum > 0) {
			this.playerNum = playerNum;
			// Löscht vorhandene Spieler
			players.clear();
			for(Integer i = Integer.valueOf(0); i < playerNum; i++) {
				double xOffset, yOffset;
 				// Setzt die Startpositionen für Spieler basierend auf Index
				switch(i) {
				case 0:
					xOffset = objectScale*6;
					yOffset = 0;
					break;
				case 1:
					xOffset = 0;
					yOffset = -objectScale*6;
					break;
				case 2: 
					xOffset = -objectScale*6;
					yOffset = 0;
					break;
				case 3:
					xOffset = 0;
					yOffset = objectScale*6;
					break;
				default:
					xOffset = 0;
					yOffset = 0;
					break;
				}
				 // Erstellt Spieler mit den erforderlichen Eigenschaften
				KeyBindings keyBindings = new KeyBindings(settingsWindow.getPlayerNControlUp(i), settingsWindow.getPlayerNControlLeft(i), settingsWindow.getPlayerNControlDown(i), settingsWindow.getPlayerNControlRight(i));
				Snake player = new Snake(gameField.getMinWidth()/2+xOffset, gameField.getMinHeight()/2+yOffset, objectScale, ORIENTATION.values()[i], settingsWindow.getPlayerColors()[i], keyBindings, i, gameField);
				// Fügt Spieler dem Spiel-Feld hinzu
				players.add(player);
				gameField.getChildren().addAll(player.getSnake());
				stage.addEventHandler(KeyEvent.KEY_PRESSED, player.getPlayer().getHandler());
				// Reagiert auf Änderungen der Spielerpunktzahl
				sCL = new ScoreChangedListener() {
					@Override
					public void scoreChanged(int newVal, int playerNum) {
						Platform.runLater(() -> {
							sMenu.updateScore(newVal, playerNum);
						});
					}
				};
				player.addScoreChangedListener(sCL);
				// Erstellt und plant die Spieler-Update-Aufgaben
				Runnable r = () -> {
					try {
						player.getTask().run();
					}
					catch(Exception e) {
						e.printStackTrace();
					}
				};
				// Fügt Spieler-Update-Aufgaben für eine gleichmäßige Aktualisierung hinzu
				futureTasks.add(timer.scheduleAtFixedRate(r, 0, 1000/32, TimeUnit.MILLISECONDS)); // 32 FPS
			}
			// Initialisiert die Kollisionsbehandlung und Assets
			colHandler = new SnakeCollisionHandler(players, assets, gameField);
			assets.createAssetTask();
			// Plant Aufgaben für das Aktualisieren der Assets
			Runnable rf = () -> {
				try {
					if(gameRunning.get())
						assets.getTask().run();
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			};
			futureTasks.add(timer.scheduleAtFixedRate(rf, 10, 2500, TimeUnit.MILLISECONDS)); // One Fruit every 2.5 seconds
			// Plant Kollisions-Check-Aufgaben für das Spiel
			Runnable rc = () -> {
				try {
					if(gameRunning.get()) {
						colHandler.collide();
						Platform.runLater(() -> {
							checkForGameOver();
						});
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			};
			futureTasks.add(timer.scheduleAtFixedRate(rc, 0, 1000/32, TimeUnit.MILLISECONDS)); // 32 FPS
		}

		return gameField; // Gibt das Spiel-Feld zurück
	}
	// Überprüft, ob das Spiel zu Ende ist
	private void checkForGameOver() {
		if(players.size() == 0) {
			onGameOver(null);
		}
	}
	// Öffnet die Einstellungen
	private void onOpenSettings(ActionEvent ae) {
		onExitGame(null);
		updateSettings();
		stage.getScene().setRoot(setScreen);
	}
	// Verlässt das Spiel
	private void onExitGame(ActionEvent ae) {
		if(gameScreen != null) {
			onGameToPausedState(null);
			defScreen = gameScreen;
		}
	}
	// Kehrt zum Spiel zurück
	private void onBackToGame(ActionEvent ae) {
		updateMenu(defScreen);
		stage.getScene().setRoot(defScreen);
	}
	// Startet ein neues Spiel
	private void onStartNewGame(ActionEvent ae) {
		killActiveTasks();
		gameScreen = createWidgets(createGameEnv(Integer.valueOf(settingsWindow.playerCount)));
		onGameToActiveState(null);
		stage.getScene().setRoot(gameScreen);
	}
	// Setzt das Spiel in den pausierten Zustand
	private void onGameToPausedState(ActionEvent ae) {
		if(gameScreen != null) {
			setGameRunning(false);
			updateMenu(gameScreen);
		}
	}
	// Setzt das Spiel in den aktiven Zustand
	private void onGameToActiveState(ActionEvent ae) {	
		if(gameScreen != null) {
			setGameRunning(true);
			updateMenu(gameScreen);
		}
	}
	public void onGameOver(ActionEvent ae) {
		killActiveTasks();	
		defScreen = createWidgets(createContent());
		gameScreen = null;	
		stage.getScene().setRoot(createGOScreen());
	}
	// Beendet das Spiel
	private void onQuit(ActionEvent ae) {
		Platform.exit();
		System.exit(0);
	}
	// Beendet laufende Aufgaben
	private void killActiveTasks() {
		if(futureTasks.size() != 0) {
			for(ScheduledFuture<?> future : futureTasks) {
				future.cancel(true);
			}
		}
	}
	// Aktualisiert den Status des Spiels (läuft oder nicht)
	private void setGameRunning(boolean running) {
		for(Snake sg : players) {
			sg.getPlayer().setRunning(running);
		}
		gameRunning.set(running);
	}

	public static void main(String[] args) {
		launch(args);
	}

}
