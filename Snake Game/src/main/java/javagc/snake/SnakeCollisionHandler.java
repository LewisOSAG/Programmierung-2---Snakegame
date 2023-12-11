package javagc.snake;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javagc.snake.Assets.Fruit;

public class SnakeCollisionHandler {

	List<Snake> players;
	Assets assets;
	Pane gameField;

	SnakeCollisionHandler (List<Snake> players, Assets assets, Pane gameField) {
		this.players = players;
		this.assets = assets;
		this.gameField = gameField;
	}

	public void collide() {
		collideWithWalls();
		for(Snake s : players) {
			collideWithSelf(s);
		}
		collideWithPlayers();
		collideWithAssets();
	}

	private void collideWithPlayers() {
		Set<Snake> collided = new HashSet<>();

		for(int i = 0; i < players.size(); i++) {
			if(players.get(i).getPlayer().getCollisionEnabled()) {
				Point snakeHead = players.get(i).getPlayer().getSnake().get(0);

				for(Snake s : players) {
					if(!(s == players.get(i))) {
						if(s.getPlayer().getSnake().contains(snakeHead)) {
							collided.add(players.get(i));
						}
					}
				}
			}
		}
		for(Snake s : collided) {
			Platform.runLater(() -> {
				s.destroySnake();
				players.remove(s);
			});
		}
	}
	private void collideWithSelf(Snake player) {
		if(player.getPlayer().getCollisionEnabled()) {
			Point snakeHead = player.getPlayer().getSnake().get(0);

			List<Point> collider = new ArrayList<>();

			collider.addAll(player.getPlayer().getSnake());
			collider.remove(snakeHead);

			if(collider.contains(snakeHead)) {
				Platform.runLater(() -> {
					player.destroySnake();
					players.remove(player);
				});
			}
		}
	}
	private void collideWithAssets() {
		for(Snake s : players) {
			// Zwischenspeicher
			List<Fruit> fruits = new ArrayList<>();
			fruits.addAll(assets.getFruits());
			
			Point snakeHead = s.getPlayer().getSnake().get(0);

			for(Fruit f : fruits) {
				if(snakeHead.equals(f.getFruit().getPos())) {
					s.addScore(f.getFruit().getPoints());
					Platform.runLater(() -> {
						s.growSnake();
						assets.removeFruit(f);
					});
				}
			}
		}
	}
	private void collideWithWalls() {
		for(Snake s : players) {
			boolean collided = false;
			
			if(s.getX() < 0 || s.getX() >= gameField.getMinWidth()) {
				collided = true;
			}
			if(s.getY() < 0 || s.getY() >= gameField.getMinHeight()) {
				collided = true;
			}

			if(collided) {
				Platform.runLater(() -> {
					s.destroySnake();
					players.remove(s);
				});
			}
		}
	}
}
