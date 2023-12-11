package javagc.snake;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javagc.snake.AssetsLogic.FruitLogic.FRUITTYPE;

/**
 * 
Diese Klasse enthält alle logischen Komponenten der Spielressourcen (Früchte)
 * 
 * Die Fruit-Klasse in Assets enthält ein FruitLogic-Objekt und macht so eine vollständige Frucht bereit
 */
public class AssetsLogic {
	
	// Enthält die FruitLogic-Instanzen der Früchte
	private List<FruitLogic> fruits  = new ArrayList<>();
	
	//Diese Klasse enthält die Typ- und Positionseigenschaft einer Frucht
	public static class FruitLogic {
		
		// Diese Aufzählung definiert die Anzahl der Punkte, die eine bestimmte Obstart zum Score eines Spielers addiert
		
		public static enum FRUITTYPE {
			APPLE(3), CHERRY(1), LEMON(2), PLUM(2), WATERMELON(5);
			
			private final int points;
			
			private FRUITTYPE(int points) {
				this.points = points;
			}
		}
		// Enthält die Art der Frucht
		private FRUITTYPE type;
		// Enthält die Position der Frucht
		private Point pos;
		
		/**
		 * Erstellt eine neue FuitLogic-Instanz, die die logischen Komponenten einer Frucht enthält
		 * 
		 * @param type	-	Art der Frucht (Siehe Aufzählung FRUITTYPE)
		 * @param x		- 	Position auf der X-Achse
		 * @param y		-	Position auf der Y-Achse
		 */
		public FruitLogic(FRUITTYPE type, Double x, Double y) {
			this.type = type;
			pos = new Point(x, y, null);
		}
		
		
		// Erzeugt einen eindeutigen Hashcode für jede FruitLogic-Instanz mit derselben Position und demselben Typ
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((pos == null) ? 0 : pos.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}
		
		
		// Implementiert eine sichere Vergleichsmethode für FruitLogic-Objekte
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FruitLogic other = (FruitLogic) obj;
			if (pos == null) {
				if (other.pos != null)
					return false;
			} else if (!pos.equals(other.pos))
				return false;
			if (type != other.type)
				return false;
			return true;
		}
		/**
		 * Getter für die Art der Frucht
		 * 
		 * @return	-	gibt die Art der Frucht wieder
		 */
		public FRUITTYPE getType() {
			return type;
		}
		/**
		 * Getter für die Anzahl der Punkte, die die Frucht bringt
		 * 
		 * @return	-	gibt die Punkte wieder
		 */
		public synchronized int getPoints() {
			return type.points;
		}
		/**
		 * Getter für die Position der Frucht
		 * 
		 * @return	-	gibt die Position der Frucht wieder
		 */
		public synchronized Point getPos() {
			return pos;
		}
		/**
		 * Setter für die Position der Frucht
		 * 
		 * @param pos	-	die Position als Punkt 
		 */
		public synchronized void setPos(Point pos) {
			this.pos = pos;
		}
	}

	/**
	 * Getter für die Früchteliste
	 * 
	 * @return	-	gibt einen Verweis auf die Früchteliste zurück
	 */
	public synchronized List<FruitLogic> getFruits() {
		return fruits;
	}

	/**
	 * Erzeugt eine zufällige Frucht mit zufälliger Position und Art
	 * 
	 * @param paneWidth		-	Breite auf der die Frucht gerendert werden soll
	 * @param paneHeight	-	Höhe auf der die Frucht gerendert werden soll
	 * @return				-	gibt einen Verweis auf das neu erstellte Objekt der Klasse Frucht zurück
	 */
	public FruitLogic generateRandomFruit(double paneWidth, double paneHeight) {
		Random rand = new Random();
		
		double x = rand.nextInt((int)paneWidth);
		double y = rand.nextInt((int)paneHeight);
		int type = rand.nextInt(FRUITTYPE.values().length);
		
		return new FruitLogic(FRUITTYPE.values()[type], x, y);
	}
	/**
	 * Entfernt eine Instanz von FruitLogic aus der Früchteliste
	 * 
	 * @param fruit	-	das FruitLogic-Objekt, das entfernt werden soll
	 */
	public synchronized void removeFruit(FruitLogic fruit) {
		fruits.remove(fruit);
	}
}
