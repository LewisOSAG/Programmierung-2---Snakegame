package javagc.snake;

import java.sql.Connection;

public class DbContext {


    private final Connection connection; // Die Verbindungsinstanz zur Datenbank
    // Konstruktor für die DbContext-Klasse
    public DbContext(Connection connection) {
        this.connection = connection; // Initialisiert die Verbindungsinstanz
    }
    // Gibt die bestehende Datenbankverbindung zurück.
    public Connection getConnection() {
        return connection; // Gibt die gespeicherte Verbindung zurück
    }
}
