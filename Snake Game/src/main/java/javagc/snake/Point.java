package javagc.snake;

/**
 * Die Klasse Point repräsentiert einen Punkt im Koordinatensystem.
 * Sie speichert die Koordinaten (x, y) sowie die Ausrichtung des Punktes.
 * Diese Klasse wird in der Snake-Logik verwendet.
 */

public class Point {

    // Koordinaten des Punktes
	private Double x;
	private Double y;
	// Himmelsrichtungen (Ost, Nord, West, Süd)
	public static enum ORIENTATION {
		ORIENTATION_EAST, ORIENTATION_NORTH, ORIENTATION_WEST, ORIENTATION_SOUTH;
	}
	private ORIENTATION orientation; // Aktuelle Ausrichtung

	private ORIENTATION currInstruction = null; // Aktuelle Anweisung für die Bewegung
	
    // Getter und Setter für x-Koordinate
	public synchronized Double getX() {
		return x;
	}
	public synchronized void setX(Double x) {
		this.x = x;
	}
	// Getter und Setter für y-Koordinate
	public synchronized Double getY() {
		return y;
	}
	public synchronized void setY(Double y) {
		this.y = y;
	}
	 // Getter und Setter für Ausrichtung
	public synchronized ORIENTATION getOrientation() {
		return orientation;
	}
	public synchronized void setOrientation(ORIENTATION orientation) {
		this.orientation = orientation;
	}
	// Getter und Setter für aktuelle Anweisung
	public synchronized ORIENTATION getCurrInstruction() {
		return currInstruction;
	}
	public synchronized void setCurrInstruction(ORIENTATION currInstruction) {
		this.currInstruction = currInstruction;
	}
	// Konstruktor, der einen Punkt mit Koordinaten und Anfangsausrichtung initialisiert
	public Point(Double x, Double y, ORIENTATION initialOrientation) {
		this.x = x;
		this.y = y;
		this.orientation = initialOrientation;
	}
	// Überschreiben der hashCode()-Methode für die Verwendung in Datenstrukturen wie HashSet
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		result = prime * result + ((y == null) ? 0 : y.hashCode());
		return result;
	}
	// Überschreiben der equals()-Methode für die Verwendung in Datenstrukturen wie HashSet
	@Override
	public boolean equals(Object obj) {
		double threshold = 0.45;
		
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Point other = (Point) obj;
		if (x == null) {
			if (other.x != null)
				return false;
		} else if (Math.abs(x-other.x) > threshold)
			return false;
		if (y == null) {
			if (other.y != null)
				return false;
		} else if (Math.abs(y-other.y) > threshold)
			return false;
		return true;
	}
}
