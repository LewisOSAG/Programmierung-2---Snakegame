package javagc.snake;
/**
 * Das ScoreChangedListener-Interface definiert einen Vertrag für Klassen, die benachrichtigt
 * werden möchten, wenn sich der Punktestand ändert.
 */

public interface ScoreChangedListener {

	void scoreChanged(int newVal, int playerNum);
}
