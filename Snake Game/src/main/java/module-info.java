module javagc.snake {
   /**
	* Exportiert das Modulpaket, um es für andere Module verfügbar zu machen.
    * Damit kann die Haupt-JavaFx-Klasse (Main JavaFx class) im gleichen Modul gestartet werden,
    * ohne eine separate Klasse verwenden zu müssen.
	*/
    exports javagc.snake;
    requires transitive javafx.base;
    requires transitive javafx.controls;
    requires transitive javafx.graphics;
    requires java.sql;
 
}