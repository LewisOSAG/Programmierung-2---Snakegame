package javagc.snake;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Menü mit zugehörigen Widgets und Daten
 */
public class SnakeMenu
{
	HBox root;
	int playerCount;
	String[] playerNames;
	Color[] playerColors;
	Label[] labels = new Label[4];
	int[] scores = {0, 0, 0, 0};
	
	public SnakeMenu()
	{
	}
	
	void createMenuWidgets(HBox root, int playerCount, String[] playerNames, Color[] playerColors)
	{
		
		 //Erstellt Widgets der Menüzeile
		this.root = root;
		this.playerCount = playerCount;
		this.playerNames = playerNames;
		this.playerColors = playerColors;
				
		root.setSpacing(16);
		
		// Anzeige der Spielerpunkte
		HBox playerPoints = new HBox();
		playerPoints.setSpacing(16);
		for(int i=0; i<playerCount; i++)
		{
			labels[i] = new Label(playerNames[i] + ": " + scores[i] + " P"); // Setzt die Farbe des Labels entsprechend der Spielerfarbe
			labels[i].setStyle("-fx-text-fill: #" +  String.format("%06X", colorToInt(playerColors[i])));
			labels[i].setFont(new Font(20));
			playerPoints.getChildren().add(labels[i]);
		}

		root.getChildren().add(playerPoints); // Fügt die Spielerpunkte-Anzeige zum Layout hinzu
	}
	// Aktualisiert die Anzeige der Spielerpunkte basierend auf den aktuellen Daten
	public void updatePlayerLabels() {
		for(int i=0; i<playerCount; i++) {
			labels[i].setText(playerNames[i] + ": " + scores[i] + " P");
		}
	}
	// Konvertiert eine JavaFX-Farbe in das Hexadezimalformat
	private int colorToInt(Color col) {
		int c;
		c = ((int) Math.round((Double) col.getRed() * 255) << 16);
		c += ((int) Math.round((Double) col.getGreen() * 255) << 8);
		c += ((int) Math.round((Double) col.getBlue() * 255));
		return c;
	}
	// Aktualisiert den Punktestand eines Spielers und die Anzeige
	public void updateScore(int nv, int playerNumber) {
		scores[playerNumber] = nv;
		labels[playerNumber].setText(playerNames[playerNumber] + ": " + scores[playerNumber] + " P");
	}
	 // Setzt die Punktestände aller Spieler zurück
	public void setScores() {
		for(int i=0; i<scores.length; i++) {
			scores[i] = 0;
		}
	}
	 // Gibt den aktuellen Punktestand aller Spieler zurück
	public int[] getScores(){
		
		return scores;
	}
}
