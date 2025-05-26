package presentacion;

import domain.Log;
import domain.POOBkemon;
import domain.POOBkemonException;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class PokemonBattlePanel extends JPanel implements Auxiliar {
    // Constantes de rutas
    private static final String CHARACTER = "resources/personaje/";
    private static final String MENU = "resources/menu/";
    private static final String MAP = MENU+"map/";
    private static final String FRAME_ATTACK = MENU+"frameAttack/";
    private static final String FRAME = MENU+"frame/";
    private static final String frontFloor = MENU+"frontFloor/";
    private static final String backFloor = MENU+"backFloor/";
    private static final String status = "resources/menu/status/";
    private static final String POKEMONES = "resources/pokemones/Emerald/";
    private static final String BACK_PATH = POKEMONES + "Back/";
    private static final String BACK_SHINY_PATH = POKEMONES + "BackShiny/";
    private static final String NORMAL_PATH = POKEMONES + "Normal/";
    private static final String SHINY_PATH = POKEMONES + "Shiny/";
    private static final String PNG_EXT = ".png";
    // Componentes UI
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private final Map<String, Supplier<JPanel>> panelBuilders = new HashMap<>();
    private POOBkemon game;
    private BattleListener battleListener;
    private int currentPlayer;
    private boolean newTurn = true;
    private boolean waitingForMachineDecision = false;

    private boolean turnInProgress = false;
    private int frame=0,fondo = 0;

    // Estado de la batalla
    private String[] decisionTrainer1 = null;
    private String[] decisionTrainer2 = null;
    private ArrayList<Integer> order;
    private Timer decisionTimer;
    private int timeLeft = 20;
    private JLabel timerLabel;
    private boolean isTimerPaused = false;
    private long pauseStartTime;
    private int remainingPausedTime;
    private boolean paused = false;


    public interface BattleListener {
        void onBattleEnd(boolean playerWon);
    }

    public PokemonBattlePanel(POOBkemon game,int fondo,int frame) {
        if (game == null) throw new IllegalArgumentException("Game cannot be null");
        this.game = game;
        this.order = game.getOrder();
        this.currentPlayer = game.getOrder().get(0);
        this.fondo = fondo;
        this.frame = frame;
        initializeUI();
    }

    private void initializeUI() {
        newTurn = false;
        setLayout(new BorderLayout());
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        add(mainPanel, BorderLayout.CENTER);
        // Registrar creadores de paneles
        panelBuilders.put("battle", this::createBattleView);
        panelBuilders.put("pokemon", this::createPokemonView);
        panelBuilders.put("attack", this::createAtaquesView);
        panelBuilders.put("items", this::createItemsView);
        panelBuilders.put("text",this::moveText);
        panelBuilders.put("pause",this::createPusePanel);
        JPanel initialPanel = panelBuilders.get("battle").get();
        initialPanel.setName("battle");
        mainPanel.add(initialPanel, "battle");
        SwingUtilities.invokeLater(() -> {
            if (!game.isMachine(currentPlayer)) {
                startDecisionTimer();
            }
        });
    }

    public void setBattleListener(BattleListener listener) {
        this.battleListener = listener;
    }
    //pantalla de pelea
    private JPanel createBattleView(){
        this.paused=false;
        JPanel panel = createUpPanel();
        HashMap<Integer, String[]> currentPokemons = this.game.getCurrentPokemons();

        JPanel framePanel = new ImagePanel(null,FRAME_ATTACK+this.frame+PNG_EXT);
        JLabel battleText = new JLabel("What should \n" +currentPokemons.get(this.currentPlayer)[1] + " do?");//game.getPlayerCurrentPokemonName()
        battleText.setFont(Auxiliar.cargarFuentePixel(5));
        battleText.setOpaque(false);

        JPanel buttonContainer = new JPanel(new BorderLayout());
        buttonContainer.setBackground(Color.GRAY);
        buttonContainer.setBorder(BorderFactory.createLineBorder(Color.GRAY, 6));

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        buttonPanel.setOpaque(false);
        String[] options = {"ATTACK", "ITEM", "POKÉMON", "RUN"};

        for (String option : options) {
            JButton btn = new JButton(option);
            btn.setFont(Auxiliar.cargarFuentePixel(18));
            btn.setFocusPainted(false);
            btn.setContentAreaFilled(true);
            btn.setBackground(Color.WHITE);
            btn.setForeground(Color.DARK_GRAY);
            btn.setBorder(BorderFactory.createLineBorder(new Color(211, 211, 211), 3));
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent evt) {
                    btn.setBackground(new Color(211, 211, 211));
                }

                public void mouseExited(MouseEvent evt) {
                    btn.setBackground(Color.WHITE);
                }
            });

            if (option.equals("POKÉMON")) {
                btn.addActionListener(e -> {
                    this.showPanel("pokemon");;
                });
            } else if(option.equals("ATTACK")) {
                btn.addActionListener(e -> showPanel("attack"));
            } else if(option.equals("RUN")) {
                btn.addActionListener(e -> {
                    setDecision(new String[]{"Run", ""+this.currentPlayer});
                });
            } else if(option.equals("ITEM")) {
                btn.addActionListener(e -> showPanel("items"));
            }
            buttonPanel.add(btn);
        }
        if(game.finishBattle()) {
            for(Component jb: buttonPanel.getComponents()){
                jb.setEnabled(false);
            }
            Timer timer = new Timer(1000, e -> {
                battleListener.onBattleEnd(false);
            });
            timer.setRepeats(false);
            timer.start();
        }
        if (game.isMachine(this.currentPlayer)) {
            for(Component jb: buttonPanel.getComponents()){
                jb.setEnabled(false);
            }
            waitingForMachineDecision = true;
            showPanel("attack");
            machineDecision();
        }
        buttonContainer.add(buttonPanel, BorderLayout.CENTER);
        timerLabel = new JLabel("", SwingConstants.CENTER);
        timerLabel.setFont(Auxiliar.cargarFuentePixel(25));
        timerLabel.setForeground(Color.RED);
        framePanel.add(timerLabel);
        framePanel.add(buttonContainer);
        framePanel.add(battleText);
        framePanel.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                int w = framePanel.getWidth();
                int h = framePanel.getHeight();
                int fontSize = Math.max(12, h / 24);
                battleText.setFont(Auxiliar.cargarFuentePixel(20));
                battleText.setForeground(Color.white);
                battleText.setBounds((int)(w * 0.03), (int)(h * 0.135), (int)(w * 0.465), (int)(h * 0.730));
                buttonContainer.setBounds((int)(w * 0.51), (int)(h * 0.03), (int)(w * 0.48), (int)(h * 0.95));
                timerLabel.setBounds((int)(w * 0.02), (int)(h * 0.1), 50, 30);
            }
        });


        panel.add(framePanel);

        panel.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                int w = panel.getWidth();
                int h = panel.getHeight();
                framePanel.setBounds((int)(w * 0), (int)(h * 0.70), (int)(w * 1), (int)(h * 0.3));
            }
        });
        if (!game.isMachine(this.currentPlayer) && newTurn) {
            startDecisionTimer();
        }
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escPressed");

        panel.getActionMap().put("escPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPanel("pause");
            }
        });
        return panel;
    }
    //cambio de pokemon
    private JPanel createPokemonView() {
        newTurn = false;
        HashMap<Integer, String[]> currentPokemons = this.game.getCurrentPokemons();
        String[] curentplayer = currentPokemons.get(this.currentPlayer);

        JPanel panel = new ImagePanel(null, MENU+"p.png");
        JButton confirmButton = Auxiliar.crearBotonEstilizado("Confirm", new Rectangle(1,1,1,1), new Color(4, 132, 25));
        JButton backButton = Auxiliar.crearBotonTransparente("Back", new Rectangle(1,1,1,1), false);
        JLabel message = new JLabel("Choose a Pokemon");
        confirmButton.setVisible(false);

        // Panel del Pokémon actual
        JPanel currentPokemonPanel = new JPanel(null);
        currentPokemonPanel.setOpaque(false);
        JPanel selectedPokemonImage = new ImagePanel(null, "resources/pokemones/Emerald/Icon/" + curentplayer[2] + ".png");
        selectedPokemonImage.setOpaque(false);
        JLabel selectedNameLabel = new JLabel(curentplayer[1]);
        JLabel selectedLevel = new JLabel("Nv. " + curentplayer[4]);
        JLabel selectedHPLabel = new JLabel(curentplayer[6] + "/" + curentplayer[5]);

        // Configuración de fuentes y colores
        selectedLevel.setFont(Auxiliar.cargarFuentePixel(20));
        selectedNameLabel.setFont(Auxiliar.cargarFuentePixel(20));
        selectedHPLabel.setFont(Auxiliar.cargarFuentePixel(20));
        selectedLevel.setForeground(Color.white);
        selectedNameLabel.setForeground(Color.white);
        selectedHPLabel.setForeground(Color.white);
        selectedHPLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        selectedLevel.setHorizontalAlignment(SwingConstants.LEFT);
        selectedNameLabel.setHorizontalAlignment(SwingConstants.LEFT);

        BarraVidaConImagen selectedHpBar = new BarraVidaConImagen(Integer.parseInt(curentplayer[5]));
        selectedHpBar.setValue(Integer.parseInt(curentplayer[6]));

        JPanel currentPokemonEfect = new ImagePanel(null, status+curentplayer[17]+PNG_EXT);
        currentPokemonPanel.add(selectedPokemonImage);
        currentPokemonPanel.add(selectedNameLabel);
        currentPokemonPanel.add(selectedLevel);
        currentPokemonPanel.add(selectedHPLabel);
        currentPokemonPanel.add(selectedHpBar);
        currentPokemonPanel.add(currentPokemonEfect);

        currentPokemonPanel.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                selectedPokemonImage.setBounds(0, 0, (int)(panel.getWidth() * 0.12), (int)(panel.getHeight() * 0.17));
                selectedNameLabel.setBounds((int)(currentPokemonPanel.getWidth() * 0.38), (int)(currentPokemonPanel.getHeight() * 0.20), (int)(panel.getWidth() * 0.3), 20);
                selectedLevel.setBounds((int)(currentPokemonPanel.getWidth() * 0.38), (int)(currentPokemonPanel.getHeight() * 0.40), (int)(panel.getWidth() * 0.3), 20);
                selectedHPLabel.setBounds((int)(currentPokemonPanel.getWidth() * 0.04), (int)(currentPokemonPanel.getHeight() * 0.76), (int)(panel.getWidth() * 0.3), 20);
                selectedHpBar.setBounds((int)(currentPokemonPanel.getWidth() * 0.04), (int)(currentPokemonPanel.getHeight() * 0.63), (int)(panel.getWidth() * 0.3), 15);
                currentPokemonEfect.setBounds((int)(currentPokemonPanel.getWidth() * 0.04), (int)(currentPokemonPanel.getHeight() * 0.78), (int)(panel.getWidth() * 0.065), (int)(panel.getHeight() * 0.038));
            }
        });

        // Obtener información de los Pokémon inactivos
        int[] pokeTeam = game.getPokemonsInactive(this.currentPlayer);
        String[] pokemonNames = new String[pokeTeam.length];
        int[] pokemonLevels = new int[pokeTeam.length];
        int[] pokemonHPs = new int[pokeTeam.length];
        int[] pokemonMaxHPs = new int[pokeTeam.length];
        int[] pokemonIdPokedex = new int[pokeTeam.length];
        int[] pokemonId = new int[pokeTeam.length];
        String[] pokemonEffects = new String[pokeTeam.length];

        try {
            for (int i = 0; i < pokeTeam.length; i++) {
                String[] pokemonInfo = game.getPokemonInfo(this.currentPlayer, pokeTeam[i]);
                pokemonNames[i] = pokemonInfo[1];
                pokemonLevels[i] = Integer.parseInt(pokemonInfo[4]);
                pokemonHPs[i] = Integer.parseInt(pokemonInfo[6]);
                pokemonMaxHPs[i] = Integer.parseInt(pokemonInfo[5]);
                pokemonIdPokedex[i] = Integer.parseInt(pokemonInfo[2]);
                pokemonId[i] = Integer.parseInt(pokemonInfo[0]);
                pokemonEffects[i] = game.getPokemonInfo(this.currentPlayer, pokeTeam[i])[17];
            }
        } catch (POOBkemonException e) {
            Log.record(e);
            throw new RuntimeException(e);
        }

        final AtomicInteger selectedPokemonId = new AtomicInteger(-1);
        ArrayList<JPanel> inactivePokemons = new ArrayList<>();

        // Crear paneles para cada Pokémon inactivo
        for(int i = 0; i < pokeTeam.length; i++) {
            final int index = i;
            JPanel pokemonPanel = new JPanel(null);
            pokemonPanel.setFont(Auxiliar.cargarFuentePixel(20));
            pokemonPanel.setOpaque(false);

            JPanel PokemonImage = new ImagePanel(null, "resources/pokemones/Emerald/Icon/" + pokemonIdPokedex[i] + ".png");
            PokemonImage.setOpaque(false);

            JLabel NameLabel = new JLabel(pokemonNames[i]);
            JLabel Level = new JLabel("Nv. " + pokemonLevels[i]);
            JLabel HPLabel = new JLabel(pokemonHPs[i] + "/" + pokemonMaxHPs[i]);

            Level.setFont(Auxiliar.cargarFuentePixel(20));
            NameLabel.setFont(Auxiliar.cargarFuentePixel(20));
            HPLabel.setFont(Auxiliar.cargarFuentePixel(20));
            Level.setForeground(Color.white);
            NameLabel.setForeground(Color.white);
            HPLabel.setForeground(Color.white);
            HPLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            Level.setHorizontalAlignment(SwingConstants.CENTER);
            NameLabel.setHorizontalAlignment(SwingConstants.CENTER);

            BarraVidaConImagen HpBar = new BarraVidaConImagen(pokemonMaxHPs[i]);
            HpBar.setValue(pokemonHPs[i]);

            JPanel pokemonEfect = new ImagePanel(null, status+pokemonEffects[i]+PNG_EXT);

            pokemonPanel.add(pokemonEfect);
            pokemonPanel.add(PokemonImage);
            pokemonPanel.add(NameLabel);
            pokemonPanel.add(Level);
            pokemonPanel.add(HPLabel);
            pokemonPanel.add(HpBar);

            pokemonPanel.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    PokemonImage.setBounds(0, 0, (int)(pokemonPanel.getHeight()), (int)(pokemonPanel.getHeight()));
                    NameLabel.setBounds((int)(pokemonPanel.getWidth() * 0.12), (int)(pokemonPanel.getHeight() * 0.10), (int)(panel.getWidth() * 0.2), 20);
                    Level.setBounds((int)(pokemonPanel.getWidth() * 0.12), (int)(pokemonPanel.getHeight() * 0.42), (int)(panel.getWidth() * 0.2), 20);
                    HPLabel.setBounds((int)(pokemonPanel.getWidth() * 0.45), (int)(pokemonPanel.getHeight() * 0.40), (int)(panel.getWidth() * 0.3), 20);
                    HpBar.setBounds((int)(pokemonPanel.getWidth() * 0.48), (int)(pokemonPanel.getHeight() * 0.10), (int)(panel.getWidth() * 0.285), 15);
                    pokemonEfect.setBounds((int)(pokemonPanel.getWidth() * 0.48), (int)(pokemonPanel.getHeight() * 0.43), (int)(panel.getWidth() * 0.065), (int)(panel.getHeight() * 0.038));
                }
            });

            pokemonPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if(pokemonHPs[index] > 0) {
                        selectedPokemonId.set(pokemonId[index]);
                        confirmButton.setVisible(true);
                        message.setText("Choose " + pokemonNames[index]);
                    }
                }
            });

            pokemonPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            inactivePokemons.add(pokemonPanel);
            panel.add(pokemonPanel);
        }

        // Configurar acción del botón de confirmación (FUERA DEL BUCLE)
        confirmButton.addActionListener(a -> {
            if(selectedPokemonId.get() != -1) {
                String[] decision = {"ChangePokemon", ""+currentPlayer, ""+selectedPokemonId.get()};
                setDecision(decision);
            } else {
                Auxiliar.mostrarError("Selection Error", "No Pokémon selected");
            }
        });

        backButton.addActionListener(e -> showPanel("battle"));

        panel.add(backButton);
        panel.add(currentPokemonPanel);
        panel.add(confirmButton);
        panel.add(message);

        panel.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                int w = panel.getWidth();
                int h = panel.getHeight();

                currentPokemonPanel.setFont(Auxiliar.cargarFuentePixel(20));
                currentPokemonPanel.setForeground(Color.WHITE);
                currentPokemonPanel.setBounds((int)(panel.getWidth() * 0.05), (int)(panel.getHeight() * 0.16), (int)(panel.getWidth() * 0.315), (int)(panel.getHeight() * 0.28));

                float b = 0.065f;
                for (int i = 0; i < pokeTeam.length; i++, b += 0.15f) {
                    inactivePokemons.get(i).setBounds((int)(panel.getWidth() * 0.41), (int)(panel.getHeight() * b), (int)(panel.getWidth() * 0.58), (int)(panel.getHeight() * 0.115));
                }

                confirmButton.setBounds((int)(currentPokemonPanel.getWidth() * 0.35), (int)(panel.getHeight() * 0.5), (int)(panel.getWidth() * 0.2), 50);
                confirmButton.setFont(Auxiliar.cargarFuentePixel(20));
                message.setBounds((int)(currentPokemonPanel.getWidth() * 0.08), (int)(panel.getHeight() * 0.84), (int)(panel.getWidth() * 0.69), (int)(panel.getHeight() * 0.115));
                message.setFont(Auxiliar.cargarFuentePixel(30));
                backButton.setBounds((int)(panel.getWidth() * 0.82), (int)(panel.getHeight() * 0.86), (int)(panel.getWidth() * 0.15), 40);
                backButton.setFont(Auxiliar.cargarFuentePixel(20));
            }
        });

        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escPressed");

        panel.getActionMap().put("escPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPanel("battle");
            }
        });

        return panel;
    }
    //
    private JPanel createAtaquesView() {
        newTurn=false;
        HashMap<Integer,String[]> currentPokemons = this.game.getCurrentPokemons();
        JPanel panel = createUpPanel();
        JPanel frame = new ImagePanel(null,FRAME_ATTACK+this.frame+PNG_EXT);
        JPanel buttonContainer = new JPanel(new BorderLayout());
        buttonContainer.setBackground(Color.GRAY);
        buttonContainer.setBorder(BorderFactory.createLineBorder(Color.GRAY, 6));
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        buttonPanel.setOpaque(false);

        JPanel textPanel = new JPanel(null);
        textPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 6));
        textPanel.setOpaque(true);
        panel.add(textPanel); //?
        JLabel pp = new JLabel("");
        JLabel cantPp = new JLabel("");
        JLabel tipo = new JLabel("");
        pp.setHorizontalAlignment(SwingConstants.LEFT); //Posicionamiento del texto
        cantPp.setHorizontalAlignment(SwingConstants.RIGHT);
        tipo.setHorizontalAlignment(SwingConstants.LEFT);
        textPanel.add(pp);
        textPanel.add(cantPp);
        textPanel.add(tipo);

        textPanel.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                pp.setBounds((int)(textPanel.getWidth()*0.1), (int)(textPanel.getHeight()*0.06), (int)(textPanel.getWidth()*0.5), (int)(textPanel.getHeight()*0.38));
                cantPp.setBounds((int)(textPanel.getWidth()*0.1), (int)(textPanel.getHeight()*0.06), (int)(textPanel.getWidth()*0.83), (int)(textPanel.getHeight()*0.38));
                tipo.setBounds((int)(textPanel.getWidth()*0.1), (int)(textPanel.getHeight()*0.55),(int)(textPanel.getWidth()*0.9), (int)(textPanel.getHeight()*0.38));
                pp.setFont(Auxiliar.cargarFuentePixel(25));
                cantPp.setFont(Auxiliar.cargarFuentePixel(25));
                tipo.setFont(Auxiliar.cargarFuentePixel(25));
            }
        });

        String[][] moves = game.getActiveAttacks().get(currentPlayer);

        String[] moveNames = new String[moves.length];
        String[] movePP = new String[moves.length];
        String[] moveMaxPP = new String[moves.length];
        String[] moveType = new String[moves.length];
        String[] moveId = new String[moves.length];
        for (int i = 0; i < moves.length; i++) {
            moveNames[i] = moves[i][0];
            movePP[i] = moves[i][4];
            moveType[i] = moves[i][1];
            moveMaxPP[i] = moves[i][5];
            moveId[i] = moves[i][8]; //agregar id de movimiento
        }


        for(int i=0; i<moves.length; i++) {
            final int index = i;
            JButton btn = new JButton(moveNames[i]);
            btn.setFont(Auxiliar.cargarFuentePixel(18));
            btn.setFocusPainted(false);
            btn.setContentAreaFilled(true);
            btn.setBackground(Color.WHITE);
            btn.setForeground(Color.DARK_GRAY);
            btn.setBorder(BorderFactory.createLineBorder(new Color(211, 211, 211), 3));
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent evt) {
                    pp.setText("PP");
                    if(movePP[index].equals("-1")){}else {cantPp.setText("Inf");
                        cantPp.setText(movePP[index]+"/"+moveMaxPP[index]);}
                    tipo.setText("TYPE/"+moveType[index]);
                }
                public void mouseExited(MouseEvent evt) {
                    pp.setText("");
                    cantPp.setText("");
                    tipo.setText("");
                }
            });

            btn.addActionListener(e ->{
                if(movePP[index].equals("0")) {}else {
                    String[] decision = {"Attack", moveId[index], currentPokemons.get(this.currentPlayer)[0], "" + currentPlayer};//moveId[index] añadir id de movimiento
                    setDecision(decision);
                }
            });

            buttonPanel.add(btn);
        }
        if (game.isMachine(this.currentPlayer)) {
            for(Component jb: buttonPanel.getComponents()){
                jb.setEnabled(false);
            }
        }
        buttonContainer.add(buttonPanel, BorderLayout.CENTER);
        frame.add(buttonContainer);
        frame.add(textPanel);
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                int w = frame.getWidth();
                int h = frame.getHeight();
                int fontSize = Math.max(12, h / 24);
                textPanel.setFont(Auxiliar.cargarFuentePixel(20));
                textPanel.setForeground(Color.WHITE);
                textPanel.setBounds((int)(w * 0.03), (int)(h * 0.135), (int)(w * 0.465), (int)(h * 0.730));
                buttonContainer.setBounds((int)(w * 0.51), (int)(h * 0.03), (int)(w * 0.48), (int)(h * 0.95));
            }
        });
        panel.add(frame);

        panel.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                int w = panel.getWidth();
                int h = panel.getHeight();
                frame.setBounds((int)(w * 0), (int)(h * 0.70), (int)(w * 1), (int)(h * 0.3));
            }
        });
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escPressed");

        panel.getActionMap().put("escPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPanel("battle");
            }
        });
        return panel;
    }
    //
    private JPanel createItemsView() {
        newTurn=false;
        JPanel panel = new ImagePanel(null,MENU+"i"+PNG_EXT);
        JButton use = Auxiliar.crearBotonTransparente("Confirm", new Rectangle(1, 1, 1, 1), false);
        int[] pokeTeam = game.getPokemonsPerTrainer(this.currentPlayer);
        String[] pokemonNames = new String[pokeTeam.length];
        int[] pokemonLevels = new int[pokeTeam.length];
        int[] pokemonHPs = new int[pokeTeam.length];
        int[] pokemonMaxHPs = new int[pokeTeam.length];
        int[] pokemonIdPokedex = new int[pokeTeam.length];
        int[] pokemonId = new int[pokeTeam.length];
        String[] pokemonEfects = new String[pokeTeam.length];
        try {
            for (int i = 0; i < pokeTeam.length; i++) {
                pokemonNames[i] = game.getPokemonInfo(this.currentPlayer, pokeTeam[i])[1];//game.getPokemonName(pokeTeam[i]);
                pokemonLevels[i] = Integer.parseInt(game.getPokemonInfo(this.currentPlayer, pokeTeam[i])[4]);//game.getPokemonLevel(team[i]);
                pokemonHPs[i] = Integer.parseInt(game.getPokemonInfo(this.currentPlayer, pokeTeam[i])[6]);//game.getPokemonHP(team[i]);
                pokemonMaxHPs[i] = Integer.parseInt(game.getPokemonInfo(this.currentPlayer, pokeTeam[i])[5]);//game.getPokemonMaxHP(team[i]);
                pokemonIdPokedex[i] = Integer.parseInt(game.getPokemonInfo(this.currentPlayer, pokeTeam[i])[2]);
                pokemonId[i] = Integer.parseInt(game.getPokemonInfo(this.currentPlayer, pokeTeam[i])[0]);
                pokemonEfects[i] = game.getPokemonInfo(this.currentPlayer,pokeTeam[i])[17];
            }
        } catch (POOBkemonException e) {
            throw new RuntimeException(e);
        }
        final int[] newindex = {0};
        final int[] health = {0};
        final boolean[] selectPokemon = {false};
        ArrayList<JPanel> inactivePokemons = new ArrayList<>();
        for (int i = 0; i < pokeTeam.length; i++) {
            final int index = i;
            JPanel pokemonPanel = new JPanel(null);
            pokemonPanel.setFont(Auxiliar.cargarFuentePixel(20));
            pokemonPanel.setOpaque(false);
            JPanel PokemonImage = new ImagePanel(null, "resources/pokemones/Emerald/Icon/" + pokemonIdPokedex[i] + ".png");
            PokemonImage.setOpaque(false);
            JLabel NameLabel = new JLabel(pokemonNames[i]);//getPlayerCurrentPokemonName()
            JLabel Level = new JLabel("Nv. " + pokemonLevels[i]);//getPlayerCurrentPokemonLevel()
            JLabel HPLabel = new JLabel(pokemonHPs[i] + "/" + pokemonMaxHPs[i]);//getEnemyCurrentPokemonHP()/getEnemyCurrentPokemonMaxHP()
            Level.setFont(Auxiliar.cargarFuentePixel(20));
            NameLabel.setFont(Auxiliar.cargarFuentePixel(20));
            HPLabel.setFont(Auxiliar.cargarFuentePixel(20));
            Level.setForeground(Color.white);
            NameLabel.setForeground(Color.white);
            HPLabel.setForeground(Color.white);
            HPLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            Level.setHorizontalAlignment(SwingConstants.CENTER);
            NameLabel.setHorizontalAlignment(SwingConstants.CENTER);
            BarraVidaConImagen HpBar = new BarraVidaConImagen(pokemonMaxHPs[i]);//getPlayerCurrentPokemonMaxHP())
            HpBar.setValue(pokemonHPs[i]);//getPlayerCurrentPokemonHP() // game.getPlayerCurrentPokemonHP() <(game.getPlayerCurrentPokemonMaxHP()
            JPanel pokemonEfect = new ImagePanel(null,status+pokemonEfects[i]+PNG_EXT);
            pokemonPanel.add(pokemonEfect);
            pokemonPanel.add(PokemonImage);
            pokemonPanel.add(NameLabel);
            pokemonPanel.add(Level);
            pokemonPanel.add(HPLabel);
            pokemonPanel.add(HpBar);
            pokemonPanel.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    PokemonImage.setBounds(0, 0, (int) (pokemonPanel.getHeight()), (int) (pokemonPanel.getHeight()));
                    NameLabel.setBounds((int) (pokemonPanel.getWidth() * 0.12), (int) (pokemonPanel.getHeight() * 0.10), (int) (panel.getWidth() * 0.2), 20);
                    Level.setBounds((int) (pokemonPanel.getWidth() * 0.12), (int) (pokemonPanel.getHeight() * 0.42), (int) (panel.getWidth() * 0.2), 20);
                    HPLabel.setBounds((int) (pokemonPanel.getWidth() * 0.45), (int) (pokemonPanel.getHeight() * 0.40), (int) (panel.getWidth() * 0.3), 20);
                    HpBar.setBounds((int) (pokemonPanel.getWidth() * 0.48), (int) (pokemonPanel.getHeight() * 0.10), (int) (panel.getWidth() * 0.285), 15);
                    pokemonEfect.setBounds((int)(pokemonPanel.getWidth() *0.48), (int)(pokemonPanel.getHeight() *0.43),  (int)(panel.getWidth() * 0.065), (int)(panel.getHeight() * 0.038));
                }
            });
            pokemonPanel.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    health[0] = pokemonHPs[index];
                    newindex[0] = pokemonId[index];
                    selectPokemon[0] = true;

                }
            });
            pokemonPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            inactivePokemons.add(pokemonPanel);
            panel.add(pokemonPanel);
        }
        //ArrayList<ArrayList<String>>
        String[][] items= null;
        try {
            items = this.game.getInfoItems(this.currentPlayer);
        } catch (POOBkemonException e) {
            Log.record(e);
        }
        final String[] itemName = {"0"};
        final String[] itemAmount = {"0"};
        final  boolean[] selectItem = {false};

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        JPanel itemsPanel = new JPanel(new GridBagLayout());
        itemsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        final int ITEM_HEIGHT = 50;
        final int ITEM_WIDTH = 280;
        for(int i = 0; i < items.length; i++){
            final int index = i;
            JPanel itemPanel = new JPanel(new BorderLayout(10, 0));
            itemPanel.setPreferredSize(new Dimension(ITEM_WIDTH, ITEM_HEIGHT));
            itemPanel.setMaximumSize(new Dimension(ITEM_WIDTH, ITEM_HEIGHT));
            itemPanel.setOpaque(false);
            JPanel imagePanel = new ImagePanel(null, "resources/Items/"+items[index][0]+PNG_EXT);
            imagePanel.setPreferredSize(new Dimension(50, 40));
            JLabel nameLabel = new JLabel(items[index][0]+" X "+items[index][1]);
            nameLabel.setFont(Auxiliar.cargarFuentePixel(14));
            itemPanel.add(imagePanel, BorderLayout.WEST);
            itemPanel.add(nameLabel, BorderLayout.CENTER);
            String[][] finalItems = items;
            itemPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    itemName[0] = finalItems[index][0];
                    itemAmount[0] = finalItems[index][1];
                    selectItem[0] = true;
                }
            });
            itemPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            itemsPanel.add(itemPanel, gbc);
            itemsPanel.add(Box.createRigidArea(new Dimension(0, 5)), gbc); // Espacio entre items
        }
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        panel.add(scrollPane);
        panel.add(use);
        use.addActionListener(e -> {
            if (selectItem[0] && selectPokemon[0]) {
                if (itemName[0].toLowerCase().equalsIgnoreCase("revive") && health[0] > 0) {
                    Auxiliar.mostrarError("Item", "This item cannot be used on Pokémon that are not fainted.");
                } else if (itemName[0].toLowerCase().contains("potion") && health[0] <= 0) {
                    Auxiliar.mostrarError("Item", "This item cannot be used on Pokémon that are fainted.");
                } else {
                    String[] decision = {"UseItem",String.valueOf(this.currentPlayer), String.valueOf(newindex[0]),itemName[0]};
                    setDecision(decision);
                }
            }else if (selectItem[0] && !selectPokemon[0]) {
                Auxiliar.mostrarError("Item", "unselected Pokemon");
            }else if (!selectItem[0] && selectPokemon[0]) {
                Auxiliar.mostrarError("Item", "unselected Item");
            }else {Auxiliar.mostrarError("Item", "no option was selected");}
        });
        panel.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                scrollPane.setBounds((int) (panel.getWidth() * 0.06), (int) (panel.getHeight() * 0.16), (int) (panel.getWidth() * 0.3), (int) (panel.getHeight() * 0.61));
                use.setBounds((int) (panel.getWidth() * 0.09), (int) (panel.getHeight() * 0.86), (int) (panel.getWidth() * 0.24), (int) (panel.getHeight() * 0.09));
                use.setHorizontalAlignment(SwingConstants.RIGHT);
                float b = 0.065f;
                for (int i = 0; i < pokeTeam.length; i++, b += 0.15f){
                    inactivePokemons.get(i).setBounds((int) (panel.getWidth() * 0.41), (int) (panel.getHeight() * b), (int) (panel.getWidth() * 0.58), (int) (panel.getHeight() * 0.115));
                }
            }
        });
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escPressed");

        panel.getActionMap().put("escPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPanel("battle");
            }
        });
        return panel;
    }
    //
    private JPanel createUpPanel(){
        HashMap currentPokemons = this.game.getCurrentPokemons();
        //final String[] player = (String[]) currentPokemons.get(this.order.get(0));
        //final String[] enemy = (String[]) currentPokemons.get(this.order.get(1));
        final String[] player = (String[]) currentPokemons.get(this.currentPlayer==this.order.get(0)?this.order.get(0):this.order.get(1));
        final String[] enemy = (String[]) currentPokemons.get(this.currentPlayer==this.order.get(0)?this.order.get(1):this.order.get(0));

        final String playerPokemon = player[16].equals("true")
                ? BACK_SHINY_PATH + player[2] + PNG_EXT
                : BACK_PATH + player[2] + PNG_EXT;
        final String enemyPokemon = enemy[16].equals("true")
                ? SHINY_PATH + enemy[2] + PNG_EXT
                : NORMAL_PATH + enemy[2] + PNG_EXT;

        final Image bg = new ImageIcon(MAP + this.fondo + PNG_EXT).getImage();
        final Image currentPlayerImg = new ImageIcon(CHARACTER + this.currentPlayer + PNG_EXT).getImage();
        final ImageIcon playerIcon = new ImageIcon(playerPokemon);
        final BufferedImage playerBufferedImg = toBufferedImage(playerIcon.getImage());
        final int playerLowestY = findAbsoluteLowestVisibleY(playerBufferedImg);
        final ImageIcon enemyIcon = new ImageIcon(enemyPokemon);
        final BufferedImage enemyBufferedImg = toBufferedImage(enemyIcon.getImage());
        final int enemyLowestY = findAbsoluteLowestVisibleY(enemyBufferedImg);
        final double PLAYER_TARGET_RATIO = 0.72;
        final double ENEMY_TARGET_RATIO = 0.47;
        final int MARGIN = 15;

        JPanel playerImagePanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(playerBufferedImg, 0, 0, getWidth(), getHeight(), this);
            }
        };
        playerImagePanel.setOpaque(false);

        JPanel enemyImagePanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(enemyBufferedImg, 0, 0, getWidth(), getHeight(), this);
            }
        };
        enemyImagePanel.setOpaque(false);

        JPanel panel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                final int w = getWidth();
                final int h = getHeight();
                super.paintComponent(g);
                g.drawImage(bg, 0, 0, w, h, this);
                g.drawImage(currentPlayerImg, (int)(w * 0.88), (int)(h * 0.01), (int)(w * 0.12), (int)(h * 0.15), this);
            }
        };

        panel.add(playerImagePanel);
        panel.add(enemyImagePanel);

        JLabel enemyNameLabel = new JLabel(enemy[1]);
        JLabel enemyLevelLabel = new JLabel("Nv. " + enemy[4]);
        JLabel enemyHPLabel = new JLabel(enemy[6] + "/" + enemy[5]);
        enemyHPLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel playerNameLabel = new JLabel(player[1]);
        JLabel playerLevelLabel = new JLabel("Nv. " + player[4]);
        JLabel playerHPLabel = new JLabel(player[6] + "/" + player[5]);
        playerHPLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        BarraVidaConImagen enemyHPBar = new BarraVidaConImagen(Integer.parseInt(enemy[5]));
        enemyHPBar.setValue(Integer.parseInt(enemy[6]));

        BarraVidaConImagen playerHPBar = new BarraVidaConImagen(Integer.parseInt(player[5]));
        playerHPBar.setValue(Integer.parseInt(player[6]));

        ImagePanel front = new ImagePanel(null, frontFloor + fondo + PNG_EXT);
        front.setOpaque(false);
        ImagePanel back = new ImagePanel(null, backFloor + fondo + PNG_EXT);
        back.setOpaque(false);

        ImagePanel playerPanel = new ImagePanel(null, MENU + "player" + PNG_EXT);
        playerPanel.setOpaque(false);
        JPanel playerEfects = new ImagePanel(null,status+player[17]+PNG_EXT);
        playerEfects.setOpaque(false);
        playerPanel.setVisible(true);
        playerPanel.add(playerEfects);
        playerPanel.add(playerNameLabel);
        playerPanel.add(playerLevelLabel);
        playerPanel.add(playerHPBar);
        playerPanel.add(playerHPLabel);

        playerPanel.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                int w = panel.getWidth();
                int h = panel.getHeight();
                playerEfects.setBounds((int)(playerPanel.getWidth() * 0.20), (int)(h * 0.135), (int)(w * 0.065), (int)(h * 0.04));
                playerNameLabel.setBounds((int)(w * 0.06), (int)(h * 0.02), (int)(w * 0.25), 30);
                playerLevelLabel.setBounds((int)(w * 0.28), (int)(h * 0.02), (int)(w * 0.15), 30);
                playerHPBar.setBounds((int)(playerPanel.getWidth() * 0.2), (int)(h * 0.09), (int)(w * 0.3), 15);
                playerHPLabel.setBounds((int)(playerPanel.getWidth() * 0.2), (int)(h * 0.12), (int)(w * 0.3), 30);
                playerHPLabel.setFont(Auxiliar.cargarFuentePixel(18));
                playerNameLabel.setFont(Auxiliar.cargarFuentePixel(18));
                playerLevelLabel.setFont(Auxiliar.cargarFuentePixel(18));
            }
        });

        ImagePanel enemyPanel = new ImagePanel(null, MENU + "enemy" + PNG_EXT);
        enemyPanel.setOpaque(false);
        JPanel enemyEfects = new ImagePanel(null,status+enemy[17]+PNG_EXT);
        enemyEfects.setOpaque(false);
        enemyPanel.add(enemyEfects);
        enemyPanel.add(enemyNameLabel);
        enemyPanel.add(enemyLevelLabel);
        enemyPanel.add(enemyHPBar);
        enemyPanel.add(enemyHPLabel);

        enemyPanel.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                int w = panel.getWidth();
                int h = panel.getHeight();
                enemyEfects.setBounds((int)(w * 0.065), (int)(h * 0.145), (int)(w * 0.065), (int)(h * 0.04));
                enemyNameLabel.setBounds((int)(w * 0.03), (int)(h * 0.03), (int)(w * 0.25), 30);
                enemyLevelLabel.setBounds((int)(w * 0.25), (int)(h * 0.03), (int)(w * 0.15), 30);
                enemyHPBar.setBounds((int)(enemyPanel.getWidth() * 0.15), (int)(h * 0.10), (int)(w * 0.3), 15);
                enemyHPLabel.setBounds((int)(enemyPanel.getWidth() * 0.15), (int)(h * 0.13), (int)(w * 0.3), 30);
                enemyHPLabel.setFont(Auxiliar.cargarFuentePixel(18));
                enemyNameLabel.setFont(Auxiliar.cargarFuentePixel(18));
                enemyLevelLabel.setFont(Auxiliar.cargarFuentePixel(18));
            }
        });

        panel.add(playerPanel);
        panel.add(enemyPanel);
        panel.add(front);
        panel.add(back);

        panel.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                int w = panel.getWidth();
                int h = panel.getHeight();

                int enemyDisplayWidth = (int)(w * 0.27);
                int enemyDisplayHeight = (int)(h * 0.4);
                double enemyScaleY = (double)enemyDisplayHeight / enemyBufferedImg.getHeight();
                int enemyTargetY = (int)(h * ENEMY_TARGET_RATIO) - (int)(enemyLowestY * enemyScaleY) - MARGIN;

                enemyImagePanel.setBounds((int)(w * 0.62), enemyTargetY, enemyDisplayWidth, enemyDisplayHeight);

                int playerDisplayWidth = (int)(w * 0.25);
                int playerDisplayHeight = (int)(h * 0.3);
                double playerScaleY = (double)playerDisplayHeight / playerBufferedImg.getHeight();
                int playerTargetY = (int)(h * PLAYER_TARGET_RATIO) - (int)(playerLowestY * playerScaleY) - MARGIN;

                playerImagePanel.setBounds((int)(w * 0.12), playerTargetY, playerDisplayWidth, playerDisplayHeight);

                back.setBounds((int)(w * 0.50), (int)(h * 0.355), (int)(w * 0.50), (int)(h * 0.15));
                front.setBounds(0, (int)(h * 0.577), (int)(w * 0.625), (int)(h * 0.12));
                enemyPanel.setBounds((int)(w * 0.05), (int)(h * 0.05), (int)(w * 0.43), (int)(h * 0.255));
                playerPanel.setBounds((int)(w * 0.53), (int)(h * 0.45), (int)(w * 0.43), (int)(h * 0.23));
            }
        });
        if (newTurn) {
            newTurn = false;

            playerPanel.setVisible(false);
            enemyPanel.setVisible(false);

            panel.addComponentListener(new ComponentAdapter() {
                public void componentShown(ComponentEvent e) {
                    int w = panel.getWidth();
                    int h = panel.getHeight();

                    int targetEnemyX = (int)(w * 0.62);
                    int targetBackX = (int)(w * 0.50);
                    int targetPlayerX = (int)(w * 0.12);
                    int targetFrontX = 0;

                    int targetEnemyY = enemyImagePanel.getY();
                    int targetBackY = back.getY();
                    int targetPlayerY = playerImagePanel.getY();
                    int targetFrontY = front.getY();

                    enemyImagePanel.setLocation(-enemyImagePanel.getWidth(), targetEnemyY);
                    back.setLocation(-back.getWidth(), targetBackY);
                    playerImagePanel.setLocation(w, targetPlayerY);
                    front.setLocation(w, targetFrontY);

                    Timer animationTimer = new Timer(10, null);
                    animationTimer.addActionListener(new ActionListener() {
                        int frame = 0;
                        final int totalFrames = 30;

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            frame++;
                            double progress = (double) frame / totalFrames;

                            enemyImagePanel.setLocation((int)(-enemyImagePanel.getWidth() * (1 - progress) + targetEnemyX * progress), targetEnemyY);
                            back.setLocation((int)(-back.getWidth() * (1 - progress) + targetBackX * progress), targetBackY);
                            playerImagePanel.setLocation((int)(w * (1 - progress) + targetPlayerX * progress), targetPlayerY);
                            front.setLocation((int)(w * (1 - progress) + targetFrontX * progress), targetFrontY);

                            if (frame >= totalFrames) {
                                animationTimer.stop();
                                enemyImagePanel.setLocation(targetEnemyX, targetEnemyY);
                                back.setLocation(targetBackX, targetBackY);
                                playerImagePanel.setLocation(targetPlayerX, targetPlayerY);
                                front.setLocation(targetFrontX, targetFrontY);
                                playerPanel.setVisible(true);
                                enemyPanel.setVisible(true);
                            }
                        }
                    });
                    animationTimer.start();
                }
            });
        }

        return panel;
    }
    //
    private JPanel moveText() {
        JPanel panel = createUpPanel();
        JPanel framePanel = new ImagePanel(null, FRAME + this.frame + PNG_EXT);

        // Etiqueta multilínea usando HTML
        JLabel battleText = new JLabel();
        battleText.setFont(Auxiliar.cargarFuentePixel(25));
        battleText.setOpaque(false);
        battleText.setForeground(Color.WHITE);
        battleText.setVerticalAlignment(SwingConstants.TOP);

        framePanel.setLayout(null);
        framePanel.add(battleText);

        // Animación de escritura (máquina de escribir)
        String fullText = this.game.getLastMoves().replace("\n", "<br>");
        String htmlStart = "<html><body style='width:100%;'>";
        String htmlEnd = "</body></html>";
        final int[] index = {0};

        Timer writer = new Timer(20, null);
        writer.addActionListener(e -> {
            if (index[0] < fullText.length()) {
                battleText.setText(htmlStart + fullText.substring(0, index[0] + 1) + htmlEnd);
                index[0]++;
            } else {
                ((Timer) e.getSource()).stop();
            }
        });
        writer.start();

        // Ajuste del tamaño del texto dentro del framePanel
        framePanel.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                int w = framePanel.getWidth();
                int h = framePanel.getHeight();
                int fontSize = Math.max(24, h / 24);
                battleText.setFont(Auxiliar.cargarFuentePixel(fontSize));
                battleText.setBounds((int)(w * 0.03), (int)(h * 0.135), (int)(w * 0.94), (int)(h * 0.730));
            }
        });

        panel.setLayout(null);
        panel.add(framePanel);
        panel.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                int w = panel.getWidth();
                int h = panel.getHeight();
                framePanel.setBounds(0, (int)(h * 0.70), w, (int)(h * 0.3));
            }
        });

        return panel;
    }

    //
    private JPanel createPusePanel() {
        this.paused =true;
        pauseTimer();
        newTurn=true;
        JPanel pausePanel = new JPanel(new BorderLayout());
        pausePanel.setBackground(Color.BLACK); // Fondo completamente negro
        pausePanel.setName("pause");

        JLabel message = new JLabel("<html><div style='text-align: center;'>game paused<br>press enter</div></html>", SwingConstants.CENTER);
        message.setFont(Auxiliar.cargarFuentePixel(30)); // Ajusta si quieres otro tamaño
        message.setForeground(Color.WHITE);

        pausePanel.add(message, BorderLayout.CENTER);

        // Soporte para presionar ENTER y continuar
        pausePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enterPressed");

        pausePanel.getActionMap().put("enterPressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resumeTimer();             // Reanuda temporizador si lo tienes
                showPanel("battle");       // Vuelve al panel principal
            }
        });

        return pausePanel;
    }
    //
    /**
     * Inicia el temporizador de decisión
     */
    private void startDecisionTimer() {
        stopDecisionTimer(); // Asegurarse de detener cualquier temporizador existente

        // Solo iniciar si es turno de jugador humano
        if (game.isMachine(currentPlayer)) {
            return;
        }

        timeLeft = 20;
        updateTimerLabel();

        decisionTimer = new Timer(1000, e -> {
            timeLeft--;
            updateTimerLabel();

            if (timeLeft <= 0) {
                timeOutAction();
            }
        });
        decisionTimer.start();
    }

    /**
     * Detiene el temporizador de decisión
     */
    private void stopDecisionTimer() {
        if (decisionTimer != null) {
            decisionTimer.stop();
            decisionTimer = null;
        }
        // Resetear visualización del temporizador
        if (timerLabel != null) {
            timerLabel.setText("");
        }
    }

    /**
     * Pausa el temporizador
     */
    public void pauseTimer() {
        if (decisionTimer != null && decisionTimer.isRunning() && !isTimerPaused) {
            isTimerPaused = true;
            pauseStartTime = System.currentTimeMillis();
            remainingPausedTime = timeLeft;
            decisionTimer.stop();
            timerLabel.setForeground(Color.GRAY); // Cambiar color para indicar pausa
        }
    }

    /**
     * Reanuda el temporizador
     */
    public void resumeTimer() {
        if (isTimerPaused) {
            isTimerPaused = false;
            timeLeft = remainingPausedTime;
            updateTimerLabel();

            if (decisionTimer != null) {
                decisionTimer.start();
            } else {
                startDecisionTimer();
            }
        }
    }

    /**
     * Actualiza la visualización del temporizador
     */
    private void updateTimerLabel() {
        if (timerLabel != null) {
            timerLabel.setText(String.valueOf(timeLeft));
            // Cambiar color según el tiempo restante
            timerLabel.setForeground(timeLeft <= 5 ? Color.RED :
                    timeLeft <= 10 ? Color.ORANGE : Color.GREEN);
        }
    }

    /**
     * Acción cuando se agota el tiempo
     */
    private void timeOutAction() {
        HashMap<Integer,String[]> currentPokemons = this.game.getCurrentPokemons();
        stopDecisionTimer();
        String[] timeOver = {"timeOver",""+this.currentPlayer,currentPokemons.get(this.currentPlayer)[0]};
        showPanel("attack");
        setDecision(timeOver);
    }

    /**
     * Establece la decisión del jugador actual
     */
    private void setDecision(String[] decision) {
        stopDecisionTimer();

        if (currentPlayer == order.get(0)) {
            decisionTrainer1 = decision;
        } else {
            decisionTrainer2 = decision;
        }

        if (decision[0].equals("Run")) {
            try {
                this.game.takeDecision(decision);
                battleListener.onBattleEnd(false);
            } catch (POOBkemonException e) {
                // Manejar error
            }
            return;
        }

        if (decisionTrainer1 != null && decisionTrainer2 != null && !turnInProgress) {
            turnInProgress = true;
            executeTurn();
        } else if (!game.finishBattle()) {
            switchPlayer();
        }
    }

    /**
     * Cambia al siguiente jugador
     */
    private void switchPlayer() {
        stopDecisionTimer();
        newTurn = true;
        currentPlayer = (currentPlayer == order.get(0)) ? order.get(1) : order.get(0);
        showPanel("battle");
        // Solo iniciar temporizador si es jugador humano
        if (!game.isMachine(currentPlayer)) {
            startDecisionTimer();
        }
    }

    private void executeTurn() {
        try {
            // Ejecutar primera decisión
            this.game.takeDecision(decisionTrainer1);
            showPanel("text");
            if(this.game.finishBattle()) {
                battleListener.onBattleEnd(false);
                return;
            }
            newTurn = true;
            this.game.takeDecision(decisionTrainer2);
            Timer timer = new Timer(3000, e -> {
                showPanel("text");
            });
            newTurn = false;
            if(this.game.finishBattle()) {
                battleListener.onBattleEnd(false);
                return;
            }
            Timer timer2 = new Timer(6000, e -> {
                resetForNextTurn();
            });
            timer2.setRepeats(false);
            timer2.start();
            timer.setRepeats(false);
            timer.start();

        } catch (POOBkemonException e) {
            System.err.println("Error al procesar turno: " + e.getMessage());
            resetForNextTurn();
        }
    }

    /**
     * Reinicia el estado para el siguiente turno
     */
    private void resetForNextTurn() {
        stopDecisionTimer();
        newTurn = true;
        decisionTrainer1 = null;
        decisionTrainer2 = null;
        turnInProgress = false;
        waitingForMachineDecision = false;
        currentPlayer = order.get(0); // Volver al primer jugador

        showPanel("battle");

        if (!game.isMachine(currentPlayer)) {
            startDecisionTimer();
        }
    }



    private void machineDecision() {
        stopDecisionTimer();
        Timer timer = new Timer(2000, e -> {
            try {
                String[] decision = game.machineDecision(this.currentPlayer);
                if(!this.paused){
                setDecision(decision);}
            } catch (POOBkemonException ex) {
                Log.record(ex);
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void showIntermediatePanel(final String targetPanelName) {
        // Crear panel intermedio (puedes personalizar esto)
        JPanel intermediatePanel = new JPanel(new BorderLayout());
        intermediatePanel.setBackground(new Color(0, 0, 0, 150)); // Fondo semitransparente

        JLabel message = new JLabel("Cargando...", SwingConstants.CENTER);
        message.setFont(Auxiliar.cargarFuentePixel(30));
        message.setForeground(Color.WHITE);
        intermediatePanel.add(message, BorderLayout.CENTER);

        intermediatePanel.setName("intermediate");
        mainPanel.add(intermediatePanel, "intermediate");

        // Mostrar panel intermedio
        cardLayout.show(mainPanel, "intermediate");

        // Programar la transición al panel objetivo después de un breve retraso
        Timer transitionTimer = new Timer(10, e -> {
            // Eliminar panel intermedio
            mainPanel.remove(intermediatePanel);

            // Volver a llamar a showPanel para el panel objetivo
            showPanel(targetPanelName);
        });
        transitionTimer.setRepeats(false);
        transitionTimer.start();
    }

    public void showPanel(String name) {
        for (Component comp : mainPanel.getComponents()) {
            if (name.equals(comp.getName())) {
                mainPanel.remove(comp);
                break;
            }
        }
        Supplier<JPanel> builder = panelBuilders.get(name);
        if (builder == null) {
            System.err.println("No panel builder found for: " + name);
            return;
        }
        JPanel panel = builder.get();
        panel.setName(name); // ¡Muy importante!
        mainPanel.add(panel, name);
        Timer timer = new Timer(00, e -> {
            cardLayout.show(mainPanel, name);
        });
        timer.setRepeats(false);
        timer.start();
    }
}