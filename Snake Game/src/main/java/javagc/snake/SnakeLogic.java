package javagc.snake;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javagc.snake.Point.ORIENTATION;

/**
 * SnakeLogic definiert das Verhalten des Snakes:
 * - Der Snake steht für einen Spieler 
 * - hält alle Statistiken und Inputmappings
 * - reagiert auf andere Snakes mit bösem Zischen ;-) 
 */
public class SnakeLogic {

	private List<Point> snake = new ArrayList<>(); // Liste von Punkten repräsentiert die Schlange

	private AtomicInteger score;
	//Geschwindigkeitsanpassung Schlange
	private AtomicBoolean running;
	private AtomicBoolean collisionEnabled;
	private AtomicBoolean speedUpEnabled;
	private boolean speedUpRequired = false;
	private int speedUpScoreDivisor = 30;
	
	private final double gridSize = 1.0; // Größe des Grids, wichtig für move()
	private double threshold = 1e-5; // maximaler Unterschied zwischen den Double-Koordinaten, damit diese als gleich angenommen werden

	private final double coreSpeed = 1./8; // Grundgeschwindigkeit in Pixeln
	private int animSpeedFactor = 1;
	private AtomicBoolean reactionNeeded;

	// Klasse für die Tastenzuweisungen
	public final static class KeyBindings {

		private final KeyCode up;
		private final KeyCode left;
		private final KeyCode down;
		private final KeyCode right;

		public KeyBindings(String keyCodeUp, String keyCodeLeft, String keyCodeDown, String keyCodeRight) {
			up = KeyCode.getKeyCode(keyCodeUp.toUpperCase());
			left = KeyCode.getKeyCode(keyCodeLeft.toUpperCase());
			down = KeyCode.getKeyCode(keyCodeDown.toUpperCase());
			right = KeyCode.getKeyCode(keyCodeRight.toUpperCase());
		}
	}
	final KeyBindings keyBindings;
	// Tastenzuweisungen
	final EventHandler<KeyEvent> keyHandler;
	

	public Integer getScore() {  // Gibt die Punktzahl zurück
		return score.get();
	}
	public void addScore(int scorePlus) {
		int lastScore = score.getAndAdd(scorePlus);
		
		if(speedUpEnabled.get()) {
			if(score.get()/speedUpScoreDivisor > lastScore/speedUpScoreDivisor && getSpeed() < 1.0)
				speedUpRequired = true; // Prüft, ob eine Geschwindigkeitsanpassung erforderlich ist
		}
	}
	// Weitere Getter und Setter-Funktionen für verschiedene Spielparameter
	public boolean isRunning() {
		return running.get();
	}
	public void setRunning(boolean running) {
		this.running.set(running);
	}
	
	public boolean getCollisionEnabled() {
		return collisionEnabled.get();
	}
	public void setCollisionEnabled (boolean collision) {
		this.collisionEnabled.set(collision);
	}

	public double getSpeed() {
		return coreSpeed*animSpeedFactor;
	}
	public void setSpeed(Integer ExpOf2) {
		// multiplying coreSpeed by powers of two doesn't destroy the snake...
		this.animSpeedFactor *= (int) Math.pow(2.0, ExpOf2);
	}

	public ORIENTATION getOrientation() {
		return snake.get(0).getOrientation();
	}

	public List<Point> getSnake() {
		return snake;
	}

	public ORIENTATION getCurrInstruction() {
		return snake.get(0).getCurrInstruction();
	}
	public void setCurrInstruction(ORIENTATION currInstruction) {
		snake.get(0).setCurrInstruction(currInstruction);
	}

	public EventHandler<KeyEvent> getHandler() {
		return keyHandler;
	}

	public SnakeLogic(Double x, Double y, ORIENTATION initialOrientation, KeyBindings keyBindings) {
		createSnake(x, y, initialOrientation, Integer.valueOf(7));

		score = new AtomicInteger(0);
		reactionNeeded = new AtomicBoolean(false);

		this.keyBindings = keyBindings;
		keyHandler = new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if(!reactionNeeded.get()) {
					onKeyEvent(ke);
				}
			}
		};

		// for collisionHandler
		collisionEnabled = new AtomicBoolean(true);
		speedUpEnabled = new AtomicBoolean(true);
		
		running = new AtomicBoolean(false);
	}

	/**
	 * Funktion zum richtigen Bewegen des Snakes
	 */
	public void move() {
		// wenn x und y ein vielfaches von gridSize sind darf der snake die Richtung ändern (praktisch küstliche Schienen und nur an Kreuzungen darf abgebogen werden)
		if((Math.abs(snake.get(0).getX()%gridSize) < threshold || Math.abs(snake.get(0).getX()%gridSize) > (gridSize-threshold)) && (Math.abs(snake.get(0).getY()%gridSize) < threshold || Math.abs(snake.get(0).getY()%gridSize) > (gridSize-threshold))) {
			execInstructions();
			if(speedUpRequired) {
				setSpeed(1);
				speedUpRequired = false;
			}
		}

		for(Point p : snake) {
			double xChange = 0;
			double yChange = 0;

			switch(p.getOrientation()) {
			case ORIENTATION_NORTH:
				yChange--;
				break;
			case ORIENTATION_EAST:
				xChange++;
				break;
			case ORIENTATION_SOUTH:
				yChange++;
				break;
			case ORIENTATION_WEST:
				xChange--;
				break;
			}

			p.setX(p.getX()+xChange*getSpeed());
			p.setY(p.getY()+yChange*getSpeed());
		}
	}

	/**
	 * Funktion zum vergrößern des Snakes um ein Element
	 */
	public Point grow() {
		double offsetX = 0;
		double offsetY = 0;

		Point tail = snake.get(snake.size()-1);

		switch(tail.getOrientation()) {
		case ORIENTATION_NORTH:
			offsetY++;
			break;
		case ORIENTATION_EAST:
			offsetX--;
			break;
		case ORIENTATION_SOUTH:
			offsetY--;
			break;
		case ORIENTATION_WEST:
			offsetX++;
			break;
		}

		Point part = new Point(tail.getX() + offsetX, tail.getY() + offsetY, tail.getOrientation());
		snake.add(part);
		
		return part;
	}
	
	public void stopSnake() {
		running.set(false);
	}
	
	/**
	 * Funktion zum Abarbeiten von KeyEvents
	 * 
	 * @param ke
	 * ke ist das aktuelle KeyEvent (aka gerade gedrückte Taste)
	 */
	private void onKeyEvent(KeyEvent ke) {
		if(getOrientation() == ORIENTATION.ORIENTATION_NORTH || getOrientation() == ORIENTATION.ORIENTATION_SOUTH)
			reactionNeeded.set(needReactionVertical(ke));
		else 
			reactionNeeded.set(needReactionHorizontal(ke));
	}
	// Hilfsfunktionen für onKeyEvent()
	private boolean needReactionVertical(KeyEvent ke) {
		if(ke.getCode().equals(keyBindings.left)) {
			setCurrInstruction(ORIENTATION.ORIENTATION_WEST);
			return true;
		}
		else if(ke.getCode().equals(keyBindings.right)) {
			setCurrInstruction(ORIENTATION.ORIENTATION_EAST);
			return true;
		}
		return false;
	}
	private boolean needReactionHorizontal(KeyEvent ke) {
		if(ke.getCode().equals(keyBindings.up)) {
			setCurrInstruction(ORIENTATION.ORIENTATION_NORTH);
			return true;
		}
		else if(ke.getCode().equals(keyBindings.down)) {
			setCurrInstruction(ORIENTATION.ORIENTATION_SOUTH);
			return true;
		}
		return false;
	}

	/**
	 * Funktion zum Ausführen der aktuellen Instruktionen für die jeweiligen Elemente
	 */
	private void execInstructions() {
		List<ORIENTATION> reactions = getCurrentInstructions();
		for(int i = 0; i < snake.size(); i++) {
			if(reactions.get(i) != null)
				snake.get(i).setOrientation(reactions.get(i));
		}

		reactionNeeded.set(false);
	}
	private void updateInstructionQueue() {
		for(int i = 0; i < snake.size()-1; i++) {
			snake.get(snake.size()-(1+i)).setCurrInstruction(snake.get(snake.size() -(1+(i+1))).getCurrInstruction());
		}
	}
	private List<ORIENTATION> getCurrentInstructions() {
		List<ORIENTATION> currentInstructions = new ArrayList<>();

		for(Point p : snake) {
			currentInstructions.add(p.getCurrInstruction());
		}

		updateInstructionQueue();

		return currentInstructions;
	}

	/**
	 * Funktion zum Erzeugen des Snakes
	 * 
	 * @param headX
	 * headX ist die x-Koordinat des Kopfes des Snakes
	 * @param headY
	 * headY ist die y-Koordinate des Kopfes des Snakes
	 * @param initialOrientation
	 * initialOrientation legt die initiale Orientierung des Snakes fest
	 * @param initialSize
	 * initialSize legt die initiale Anzahl an Elementen fest aus denen ein Snake besteht
	 */
	private void createSnake(Double headX, Double headY, ORIENTATION initialOrientation, Integer initialSize) {
		int offsetX = 0;
		int offsetY = 0;

		switch(initialOrientation) {
		case ORIENTATION_NORTH:
			offsetY++;
			break;
		case ORIENTATION_EAST:
			offsetX--;
			break;
		case ORIENTATION_SOUTH:
			offsetY--;
			break;
		case ORIENTATION_WEST:
			offsetX++;
			break;
		}

		for(Integer i = Integer.valueOf(0); i < initialSize; i++) {
			Point p = new Point(headX + offsetX*i, headY + offsetY*i, initialOrientation);
			snake.add(p);
		}
	}
}
