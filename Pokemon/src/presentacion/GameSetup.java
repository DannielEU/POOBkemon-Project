package presentacion;

import domain.POOBkemon;
import domain.POOBkemonException;
import domain.Survive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Datos que el jugador va armando en las pantallas previas a la batalla
 * (entrenadores, equipos, movimientos e ítems) y su traspaso al dominio.
 * No depende de Swing: la ventana solo lo consulta y lo modifica.
 */
final class GameSetup {

    enum Mode { NORMAL, SURVIVAL }

    static final int TEAM_SIZE = 6;
    static final int MOVES_PER_POKEMON = 4;
    private static final int MIN_NAME_LENGTH = 3;
    private static final int MAX_NAME_LENGTH = 10;
    /** Cantidad inicial de cada ítem: el jugador reparte las unidades en la pantalla de ítems. */
    private static final String[][] EMPTY_BAG = {
            {"Potion", "0", "25"}, {"Potion", "0", "50"}, {"Potion", "0", "100"}, {"Revive", "0"}};

    private boolean randomStats;
    private final ArrayList<String> players = new ArrayList<>();
    private final HashMap<String, ArrayList<Integer>> pokemones = new HashMap<>();
    private final HashMap<String, ArrayList<Integer>> moves = new HashMap<>();
    private final HashMap<String, String[][]> items = new HashMap<>();

    boolean isRandomStats() {
        return randomStats;
    }

    /** Alterna entre estadísticas base y aleatorias. */
    boolean toggleRandomStats() {
        randomStats = !randomStats;
        return randomStats;
    }

    /** Define los dos entrenadores y deja sus equipos, movimientos e ítems con valores aleatorios/vacíos. */
    void startSetup(String trainer1, String trainer2) {
        players.clear();
        players.add(trainer1);
        players.add(trainer2);
        pokemones.clear();
        moves.clear();
        items.clear();
        for (String player : players) {
            pokemones.put(player, randomIds(TEAM_SIZE, POOBkemon.pokemonCatalogSize()));
            moves.put(player, randomIds(TEAM_SIZE * MOVES_PER_POKEMON, POOBkemon.moveCatalogSize()));
            items.put(player, emptyBag());
        }
    }

    List<String> players() {
        return players;
    }

    String player(int index) {
        return players.get(index);
    }

    ArrayList<Integer> pokemonsOf(int playerIndex) {
        return pokemones.get(players.get(playerIndex));
    }

    ArrayList<Integer> movesOf(int playerIndex) {
        return moves.get(players.get(playerIndex));
    }

    String[][] itemsOf(int playerIndex) {
        return items.get(players.get(playerIndex));
    }

    void assignPokemons(ArrayList<Integer> player1, ArrayList<Integer> player2) {
        pokemones.put(players.get(0), player1);
        pokemones.put(players.get(1), player2);
    }

    void assignMoves(ArrayList<Integer> player1, ArrayList<Integer> player2) {
        moves.put(players.get(0), player1);
        moves.put(players.get(1), player2);
    }

    /**
     * Valida los nombres de los jugadores humanos.
     *
     * @return el mensaje de error para mostrar, o {@code null} si los nombres son válidos
     */
    static String validatePlayerNames(String name1, String name2) {
        if (name1.isEmpty() || name2.isEmpty()) {
            return "Por favor ingresa nombres para ambos jugadores";
        }
        if (name1.equals(name2)) {
            return "Los nombres de los jugadores no pueden ser iguales";
        }
        if (name1.length() > MAX_NAME_LENGTH || name2.length() > MAX_NAME_LENGTH) {
            return "Los nombres no pueden tener mas de " + MAX_NAME_LENGTH + " caracteres";
        }
        if (name1.length() < MIN_NAME_LENGTH || name2.length() < MIN_NAME_LENGTH) {
            return "Los nombres no pueden tener menos de " + MIN_NAME_LENGTH + " caracteres";
        }
        return null;
    }

    /** Suma una unidad al ítem indicado del jugador. */
    void addItem(int playerIndex, int itemIndex) {
        String[] item = itemsOf(playerIndex)[itemIndex];
        item[1] = String.valueOf(Integer.parseInt(item[1]) + 1);
    }

    /**
     * Crea la partida en el dominio con los datos reunidos.
     *
     * @return el juego ya inicializado
     */
    POOBkemon createGame(Mode mode) throws POOBkemonException {
        POOBkemon.resetInstance();
        POOBkemon game = (mode == Mode.SURVIVAL) ? Survive.getInstance() : POOBkemon.getInstance();
        game.initGame(new ArrayList<>(players), pokemones, items, moves, randomStats);
        return game;
    }

    private static ArrayList<Integer> randomIds(int count, int maxId) {
        ArrayList<Integer> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ids.add(ThreadLocalRandom.current().nextInt(maxId) + 1);
        }
        return ids;
    }

    private static String[][] emptyBag() {
        String[][] copy = new String[EMPTY_BAG.length][];
        for (int i = 0; i < EMPTY_BAG.length; i++) {
            copy[i] = EMPTY_BAG[i].clone();
        }
        return copy;
    }
}
