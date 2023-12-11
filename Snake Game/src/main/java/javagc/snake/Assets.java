package javagc.snake;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javagc.snake.AssetsLogic.FruitLogic;

/**
 * Diese Klasse enthält alle grafischen Komponenten der Spielressourcen (Früchte)
 * 
 * Eine Instanz der Fruitclass in Kombination mit einer Instanz der FruitLogic-Klasse (in AssetsLogic enthalten) ergibt eine vollständige Frucht
 */
public class Assets {

	// Eine Instanz von AssetsLogic, um Zugriff auf deren Inhalte zu erhalten
	private AssetsLogic assets = new AssetsLogic();

	
	private final double scale;
	private List<Fruit> fruits;
	private Runnable assetTask;
	private Pane gameField;
	
	/**
	 * Diese Klasse enthält die FruitLogic und die Form einer Frucht
	 */
	public static class Fruit {
		
		// Eine Instanz von FruitLogic für den logischen Teil der Frucht
		private final FruitLogic fruit;

		// Eine Form für den grafischen Teil der Frucht
		private Shape shape;

		/**
		 * Erzeugt eine Frucht
		 * 
		 * @param fruit	-	die FuitLogic für die Frucht
		 * @param scale	-	der Maßstab des Gitters
		 */
		public Fruit(FruitLogic fruit, Double scale) {
			this.fruit = fruit;
			double fruitSize = scale/5 * 4;

			shape = new Circle((fruit.getPos().getX())*scale+fruitSize/2, (fruit.getPos().getY())*scale+fruitSize/2, fruitSize/2);

			Color color = Color.TRANSPARENT;
			switch(fruit.getType()) {
			case APPLE:
				color = Color.CHARTREUSE;
				break;
			case CHERRY:
				color = Color.CRIMSON;
				break;
			case LEMON:
				color = Color.GOLD;
				break;
			case PLUM:
				color = Color.INDIGO;
				break;
			case WATERMELON:
				color = Color.DARKGREEN;
				break;
			}

			shape.setFill(color);
		}
		
		/**
		 * Getter für die FruitLogic
		 * 
		 * @return	-	gibt einen Verweis auf seine FruitLogic zurück
		 */
		public synchronized FruitLogic getFruit() {
			return fruit;
		}

		/**
		 * Getter für die Form
		 * 
		 * @return	-	gibt einen Verweis auf die Form zurück
		 */
		public Shape getShape() {
			return shape;
		}
		
		/**
		 * Setter für die Form
		 * 
		 * @param shape	-	die Form, die eingestellt werden soll
		 */
		public void setShape(Shape shape) {
			this.shape = shape;
		}
	}

	/**
	 * Erstellt eine Instanz von Spielressourcen
	 * 
	 * @param gameField	-	Bereich zum für das Spielfeld
	 * @param scale		-	Rastermaßstab
	 */
	public Assets(Pane gameField, Double scale) {
		this.gameField = gameField;
		this.scale = scale;

		fruits = new ArrayList<>();
	}
	
	/**
	 * Getter für die Früchteliste
	 * 
	 * @return	-	gibt einen Verweis auf die Früchteliste zurück
	 */

	public List<Fruit> getFruits() {
		return fruits;
	}

	/**
	 * Getter runnable Sppielressourcen 
	 * 
	 * @return	-	gibt die runnalble Spielressourcen zurück
	 */
	public Runnable getTask() {
		return assetTask;
	}

	public void addFruit(Pane pane) {
		Fruit fruit = new Fruit(assets.generateRandomFruit(pane.getMinWidth()/scale, pane.getMinHeight()/scale), scale);
		synchronized(this) {
			fruits.add(fruit);
		}

		Platform.runLater(() -> {
			gameField.getChildren().add(fruit.getShape());
		});
	}

	/**
	 * Entfernt die angegebene Frucht aus dem Bereich und der Früchteliste
	 * 
	 * @param fruit	-	die Frucht die entfernt werden soll
	 */
	public void removeFruit(Fruit fruit) {
		assets.removeFruit(fruit.getFruit());

		synchronized(fruits) {
			fruits.remove(fruit);
		}

		Platform.runLater(() -> {
			gameField.getChildren().remove(fruit.getShape());
		});
	}

	/**
	 * Erstellt ein Runnable zur Ausführung in main
	 * 
	 * @param paneWidth		-	Breite auf der die Früchte gerendert werden sollen
	 * @param paneHeight	-	Höhe auf der die Früchte gerendert werden sollen
	 */
	public void createAssetTask() {
		assetTask = new Runnable() {
			@Override
			public void run() {
				Platform.runLater(() -> {
					addFruit(gameField);
				});
			}
		};
	}
}
