package presentacion;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import domain.*;
import presentacion.Auxiliar;
import presentacion.ImagePanel;
import presentacion.PokemonBattlePanel;

public class POOBkemonGUI extends JFrame implements Auxiliar {
    //entradas del dominio
    private boolean random = false; // si los stats son random
    private ArrayList<String> players = new ArrayList<>(); //[trainer1, trainer2]
    private HashMap<String,ArrayList<Integer>> pokemones = new HashMap<>(); //<trainer, pokemones(int)>
    private HashMap<String,ArrayList<Integer>> moves = new HashMap<>(); //trianer, moves (en el orden de los pokemones)>
    private HashMap<String,String[][]> items = new HashMap<>();
    private POOBkemon game;
    //
    private Clip clip;
    private JPanel IntroductionPanel;
    private JPanel menuPanel;
    private JPanel gameMode;
    private JPanel machinesPanel;
    private JPanel playersPanel;
    private JPanel gamePanel;
    private JLabel character;
    private JPanel choosePokemonPanel;
    private JPanel chooseMovesPanel;
    //
    private JMenuBar menuBar;
    private JMenu menuArchivo;
    private JMenu menuOption;
    private JMenu fondos;
    private JMenu frames;
    //
    private JMenuItem fondo1;
    private JMenuItem fondo2;
    private JMenuItem frame1;
    private JMenuItem frame2;
    private JMenuItem itemNuevo;
    private JMenuItem itemAbrir;
    private JMenuItem itemSalvar;
    private JMenuItem itemSalir;
    //
    private int fondo = 0,frame= 0;
    private ArrayList<Integer> order;
    private JButton playButton;
    private JButton pokedexButton;
    private JButton itemsButton;
    private JButton stastRandomButton;
    private JButton exitButton;
    private JButton playerVsPlayer;
    private JButton playerVsMachine;
    private JButton survival;
    private JButton machines;
    private JButton backButtonMenu;
    private JButton backButtonIntro;
    private JButton backButtonPlayersPanel;

    private JButton startButton;
    private JTextField player1Field;
    private JTextField player2Field;

    private String player1Name;
    private String player2Name;
    private String gameModeName;


    //
    private static final String CHARACTER = "resources/personaje/";
    private static final String MENU = "resources/menu/";
    private static final String POKEMONES = "resources/pokemones/Emerald/";
    private static final String BACK_PATH = POKEMONES + "Back/";
    private static final String BACK_SHINY_PATH = POKEMONES + "BackShiny/";
    private static final String NORMAL_PATH = POKEMONES + "Normal/";
    private static final String SHINY_PATH = POKEMONES + "Shiny/";
    private static final String PNG_EXT = ".png";
    private static final String songs =  "resources/songs/";
    private static final String selectionPanel = "resources/menu/battleMenuPanel.jpg";
    private static final String ITEMS = "resources/Items/";
    private static final String BUTTONS = "resources/menu/buttons/";
    private static final String POKEDEX = "resources/menu/pokedex.png";
    private static final String TYPES =  "resources/pokemones/Emerald/types/";
    private static final String GALERIA_ITEMS =  "resources/menu/items.png";
    private static final String EXIT_ICON = "resources/icon/exit_icon.png";
    private static final String FRAME = "resources/menu/frame/";
    private static final String APP_ICON = "resources/icon/window_icon.png";
    private static final String WINNER = "resources/menu/winner/";


    private POOBkemonGUI() {
        this.game = POOBkemon.getInstance();
        setTitle("POOBkemon");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(750, 550);
        setMinimumSize(new Dimension(750, 550));
        setResizable(false);
        setLocationRelativeTo(null);
        prepareElements();
        prepareActions();
    }
    private void prepareElements() {
        prepareElementsMenu();
        prepareIntroductionPanel();
        prepareMenuPanel();
        prepareGameMode();
        add(IntroductionPanel);
        IntroductionPanel.setFocusable(true);
        IntroductionPanel.requestFocusInWindow();
        setIconImage(new ImageIcon(APP_ICON).getImage());
    }
    private void prepareActions(){
        prepareActionsMenuBar();
        prepareIntroductionAction();
        prepareActionsMenuPanel();
        prepareActionsGameMode();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });
    }
    private void  refresh(JPanel panel) {
        getContentPane().removeAll();
        add(panel);
        revalidate();
        repaint();
        panel.requestFocusInWindow();
    }
    private void prepareElementsMenu() {
        menuBar = new JMenuBar();
        //
        menuArchivo = new JMenu("Archivo");
        menuOption = new JMenu("Opciones");
        fondos = new JMenu("Fondos");
        frames = new JMenu("Frames");
        //
        frame1 = new JMenuItem("Dorado");
        frame2 = new JMenuItem("Clasico");
        fondo1 = new JMenuItem("Hierba alta");
        fondo2 = new JMenuItem("Alto Mando");
        itemNuevo = new JMenuItem("Nuevo Juego");
        itemAbrir = new JMenuItem("Abrir Partida");
        itemSalvar = new JMenuItem("Guardar Partida");
        itemSalir = new JMenuItem("Salir");
        //
        menuArchivo.add(itemNuevo);
        menuArchivo.add(itemAbrir);
        menuArchivo.add(itemSalvar);
        menuArchivo.addSeparator();
        menuArchivo.add(itemSalir);
        frames.add(frame1);
        frames.add(frame2);
        fondos.add(fondo1);
        fondos.add(fondo2);
        menuOption.add(fondos);
        menuOption.add(frames);
        //
        menuBar.add(menuArchivo);
        menuBar.add(menuOption);
        //
        setJMenuBar(menuBar);
    }
    private void prepareActionsMenuBar() {
        itemNuevo.addActionListener(e -> startNewGame());
        itemAbrir.addActionListener(e -> openGame());
        itemSalvar.addActionListener(e -> saveGame());
        itemSalir.addActionListener(e -> confirmExit());
        fondo1.addActionListener(e -> {fondo=0;});
        fondo2.addActionListener(e -> {fondo=1;});
        frame1.addActionListener(e -> {frame=0;});
        frame2.addActionListener(e -> {frame=1;});
    }

    private void prepareIntroductionPanel() {
        IntroductionPanel = new JPanel(new BorderLayout()){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
            }
        };
        IntroductionPanel.setOpaque(false);
        ImageIcon icon = new ImageIcon(MENU + "start2.gif");
        JLabel gifLabel = new JLabel(icon);

        IntroductionPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension size = IntroductionPanel.getSize();
                gifLabel.setBounds(0, 0, size.width, size.height);
            }
        });

        IntroductionPanel.add(gifLabel);
        IntroductionPanel.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (IntroductionPanel.isShowing()) {
                    reproducirSonido("1-03TitleTheme.wav");
                } else {
                    detenerSonido();
                }
            }
        });
    }

    private void prepareIntroductionAction() {
        InputMap inputMap = IntroductionPanel.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = IntroductionPanel.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enterAction");
        actionMap.put("enterAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refresh(menuPanel);
            }
        });

        IntroductionPanel.setFocusable(true);
        IntroductionPanel.requestFocusInWindow(); // Fuerza el foco
    }
    private void prepareMenuPanel() {
        menuPanel = new JPanel(new BorderLayout()) {
            private ImageIcon gifIcon = new ImageIcon(MENU + "menu.gif");
            @Override
            protected void paintComponent(Graphics g) {
                g.drawImage(gifIcon.getImage(), 0, 0, getWidth(), getHeight(), this);
                super.paintComponent(g);
            }
        };
        menuPanel.setOpaque(false);
        prepareElementsMenuPanel();
    }

    private void prepareElementsMenuPanel() {

        JPanel centerPanel = new JPanel(null);
        centerPanel.setOpaque(false);
        //

        playButton = Auxiliar.crearBotonEstilizado("Jugar",new Rectangle(275, 110, 200, 60),new Color(240, 240, 240, 200));
        pokedexButton = Auxiliar.crearBotonEstilizado("Pokedex",new Rectangle(275, 180, 200, 60),new Color(240, 240, 240, 200));
        itemsButton = Auxiliar.crearBotonEstilizado("Items",new Rectangle(275, 250, 200, 60),new Color(240, 240, 240, 200));
        stastRandomButton = Auxiliar.crearBotonEstilizado(this.random ? "Stat Aleatorios" : "Stat Base",new Rectangle(275, 320, 200, 60),new Color(240, 240, 240, 200));
        backButtonIntro = Auxiliar.crearBotonEstilizado("Volver", new Rectangle(10, 450, 100, 30), new Color(240, 240, 240, 200));
        playButton.setPreferredSize(new Dimension(200, 60));
        pokedexButton.setPreferredSize(new Dimension(200, 60));
        itemsButton.setPreferredSize(new Dimension(200, 60));
        stastRandomButton.setPreferredSize(new Dimension(200, 60));

        centerPanel.add(playButton);
        centerPanel.add(pokedexButton);
        centerPanel.add(itemsButton);
        centerPanel.add(stastRandomButton);
        centerPanel.add(backButtonIntro);

        menuPanel.add(centerPanel, BorderLayout.CENTER);
    }

    private void prepareActionsMenuPanel() {
        playButton.addActionListener(e -> startNewGame());
        pokedexButton.addActionListener(e -> showPokedex());
        itemsButton.addActionListener(e -> showItemsGalery());
        stastRandomButton.addActionListener(e -> actualizarTextoDificultad());
        backButtonIntro.addActionListener(e -> refresh(IntroductionPanel));
        menuPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    refresh(IntroductionPanel);
                }
            }
        });
        menuPanel.setFocusable(true);
    }

    private void showPokedex() {

        JPanel pokedexPanel = new ImagePanel(null, POKEDEX);
        ArrayList<String[]> pokemones = this.game.getPokInfo();
        final int[] currentIndex = {0};

        // Panel para la imagen del Pokémon (IZQUIERDA)
        JLabel imagenLabel = new JLabel();
        imagenLabel.setBounds(215, 150, 150, 150); // CENTRO
        pokedexPanel.add(imagenLabel);

        JLabel type1 = new JLabel();
        type1.setBounds(20, 120, 150, 150); // CENTRO
        pokedexPanel.add(type1);

        JLabel type2 = new JLabel();
        type2.setBounds(20, 200, 150, 150); // CENTRO
        pokedexPanel.add(type2);

        // NUEVOS: Imagen de Pokémon anterior (arriba)
        JLabel imagenArriba = new JLabel();
        imagenArriba.setBounds(215, 82, 150, 55); // Más pequeño
        pokedexPanel.add(imagenArriba);

        // NUEVOS: Imagen de Pokémon siguiente (abajo)
        JLabel imagenAbajo = new JLabel();
        imagenAbajo.setBounds(215, 310, 150, 55); // Más pequeño
        pokedexPanel.add(imagenAbajo);

        // Área de información
        JTextPane infoPane = new JTextPane();
        infoPane.setBounds(440, 95, 280, 320);
        infoPane.setEditable(false);
        infoPane.setFont(cargarFuentePixel(20));
        infoPane.setOpaque(false);



        pokedexPanel.add(infoPane);

        JPanel listaPanel = new JPanel();
        listaPanel.setLayout(new BoxLayout(listaPanel, BoxLayout.Y_AXIS));
        listaPanel.setOpaque(false);

        ImageIcon arrowDown = new ImageIcon(MENU + "flechaAbajo.png");
        ImageIcon arrowUp = new ImageIcon(MENU + "flechaArriba.png");

        Image scaledArrowDown = arrowDown.getImage().getScaledInstance(190, 60, Image.SCALE_SMOOTH);
        Image scaledArrowUp = arrowUp.getImage().getScaledInstance(190, 60, Image.SCALE_SMOOTH);

        JButton upButton = new JButton();
        upButton.setBounds(475, 0, 190, 60);
        upButton.setIcon(new ImageIcon(scaledArrowUp));
        upButton.setBorderPainted(false);
        upButton.setContentAreaFilled(false);

        JButton downButton = new JButton();
        downButton.setBounds(475, 435, 190, 60);
        downButton.setIcon(new ImageIcon(scaledArrowDown));
        downButton.setBorderPainted(false);
        downButton.setContentAreaFilled(false);

        JButton backButton = Auxiliar.crearBotonTransparente("Volver", new Rectangle(30, 395, 130, 40),true);

        pokedexPanel.add(upButton);
        pokedexPanel.add(downButton);
        pokedexPanel.add(backButton);

        Runnable actualizarVista = () -> {
            listaPanel.removeAll();

            for (int i = 0; i < pokemones.size(); i++) {
                String[] p = pokemones.get(i);
                JLabel pokemonLabel = new JLabel((i + 1) + ". " + p[1]);
                pokemonLabel.setFont(cargarFuentePixel(20));

                if (i == currentIndex[0]) {
                    try {
                        ImageIcon icon = new ImageIcon(POKEMONES +"Normal/"+(i+1)+".png");
                        imagenLabel.setIcon(new ImageIcon(icon.getImage().getScaledInstance(
                                150, 150, Image.SCALE_SMOOTH)));
                        ImageIcon t1 = new ImageIcon(TYPES+p[2]+".png");
                        ImageIcon t2 = new ImageIcon(TYPES+p[3]+".png");
                        type1.setIcon(new ImageIcon(t1.getImage().getScaledInstance(
                                128, 56, Image.SCALE_SMOOTH)));
                        type2.setIcon(new ImageIcon(t2.getImage().getScaledInstance(
                                128, 56, Image.SCALE_SMOOTH)));
                    } catch (Exception e) {
                        Log.record(e);
                        imagenLabel.setIcon(null);
                    }

                    // Imagen del Pokémon anterior
                    if (currentIndex[0] > 0) {
                        try {
                            String[] anterior = pokemones.get(currentIndex[0] - 1);
                            ImageIcon iconAnterior = new ImageIcon(POKEMONES +"Normal/"+(i)+".png");
                            imagenArriba.setIcon(new ImageIcon(iconAnterior.getImage().getScaledInstance(
                                    130, 55, Image.SCALE_SMOOTH)));
                        } catch (Exception e) {
                            Log.record(e);
                            imagenArriba.setIcon(null);
                        }
                    } else {
                        imagenArriba.setIcon(null);
                    }
                    // Imagen del Pokémon siguiente
                    if (currentIndex[0] < pokemones.size() - 1) {
                        try {
                            String[] siguiente = pokemones.get(currentIndex[0] + 1);
                            ImageIcon iconSiguiente = new ImageIcon(POKEMONES +"Normal/"+ (i+2)+".png");
                            imagenAbajo.setIcon(new ImageIcon(iconSiguiente.getImage().getScaledInstance(
                                    130, 55, Image.SCALE_SMOOTH)));
                        } catch (Exception e) {
                            Log.record(e);
                            imagenAbajo.setIcon(null);
                        }
                    } else {
                        imagenAbajo.setIcon(null);
                    }

                    // Actualizar información
                    try {
                        String P = this.getListPokemones(i, pokemones);
                        infoPane.getStyledDocument().insertString(0, P, null);
                    } catch (Exception e) {}
                }

                listaPanel.add(pokemonLabel);
            }

            listaPanel.revalidate();
            listaPanel.repaint();
        };

        upButton.addActionListener(e -> {
            if (currentIndex[0] > 0) {
                currentIndex[0]--;
                actualizarVista.run();
            }
        });

        downButton.addActionListener(e -> {
            if (currentIndex[0] < pokemones.size() - 1) {
                currentIndex[0]++;
                actualizarVista.run();
            }
        });

        backButton.addActionListener(e -> refresh(menuPanel));
        InputMap inputMap = pokedexPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = pokedexPanel.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("UP"), "arriba");
        inputMap.put(KeyStroke.getKeyStroke("W"), "arriba");
        inputMap.put(KeyStroke.getKeyStroke("DOWN"), "abajo");
        inputMap.put(KeyStroke.getKeyStroke("S"), "abajo");

        actionMap.put("arriba", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentIndex[0] > 0) {
                    currentIndex[0]--;
                    actualizarVista.run();
                }
            }
        });

        actionMap.put("abajo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentIndex[0] < pokemones.size() - 1) {
                    currentIndex[0]++;
                    actualizarVista.run();
                }
            }
        });
        actualizarVista.run();

        getContentPane().removeAll();
        add(pokedexPanel);
        revalidate();
        repaint();
    }
    private String getListPokemones(int a, ArrayList<String[]> pokemones) {
        String resultado="";
        for(int i=a-2;i<=a+3; i++) {
            try {
                String[] pokemon = pokemones.get(i);
                resultado += "N°." + pokemon[0] + "  " +pokemon[1] + "\n" + "\n";
            }catch (Exception e) {
                Log.record(e);
                resultado += "\n"+"\n";
            }
        }
        return resultado;
    }
    private void showItemsGalery() {

        JPanel itemsPanel = new ImagePanel(null, GALERIA_ITEMS);
        ArrayList<ArrayList<String>> items = this.game.getItemInfo();
        final int[] currentIndex = {0}; // Para trackear el primer item visible

        // Panel para mostrar los items (4 máximo)
        JPanel itemsDisplayPanel = new JPanel(null);
        itemsDisplayPanel.setBounds(0, 0, 750, 550);
        itemsDisplayPanel.setOpaque(false);


        // Área de información (DERECHA de la imagen) - TRANSPARENTE
        JTextPane infoPanel = new JTextPane();  // Cambiamos a JTextPane para mejor control
        infoPanel.setBounds(300, 40, 390, 200);
        infoPanel.setEditable(false);
        infoPanel.setFont(cargarFuentePixel(18));
        infoPanel.setOpaque(false);  // Hacemos el fondo transparente

        //Aumenta el espacio entre lineas
        StyleContext sc = new StyleContext();
        DefaultStyledDocument doc = new DefaultStyledDocument(sc);
        Style defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setLineSpacing(defaultStyle, 0.9f); // Aumentar el espaciado (0.5 = 50% más espacio)
        infoPanel.setDocument(doc);
        itemsPanel.add(infoPanel);

        JLabel messageLabel = new JLabel("ITEMS");
        messageLabel.setFont(cargarFuentePixel(32));
        messageLabel.setForeground(Color.white);
        messageLabel.setHorizontalAlignment(JLabel.LEFT);
        messageLabel.setBounds(30, 10, 400, 50);

        JLabel message1Label = new JLabel("INFORMACION SOBRE LOS ITEMS DEL JUEGO");
        message1Label.setFont(cargarFuentePixel(24));
        message1Label.setForeground(Color.white);
        message1Label.setHorizontalAlignment(JLabel.LEFT);
        message1Label.setBounds(120, 360, 600, 100);


        ImageIcon arrowNext = new ImageIcon(MENU + "flechaDerecha.png");
        ImageIcon arrowPrev = new ImageIcon(MENU + "flechaIzquierda.png");

        Image scaledArrowNext = arrowNext.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        Image scaledArrowPrev = arrowPrev.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);

        JButton nextButton = new JButton();
        nextButton.setBounds(160, 150, 100, 100);
        nextButton.setIcon(new ImageIcon(scaledArrowNext));
        nextButton.setBorderPainted(false);
        nextButton.setContentAreaFilled(false);

        JButton prevButton = new JButton();
        prevButton.setBounds(-15, 150, 100, 100);
        prevButton.setIcon(new ImageIcon(scaledArrowPrev));
        prevButton.setBorderPainted(false);
        prevButton.setContentAreaFilled(false);


        JButton backButton = Auxiliar.crearBotonTransparente("Volver", new Rectangle(0, 395, 130, 40),true);

        itemsPanel.add(nextButton);
        itemsPanel.add(prevButton);
        itemsPanel.add(messageLabel);
        itemsPanel.add(message1Label);
        itemsPanel.add(backButton);
        itemsPanel.add(itemsDisplayPanel);
        itemsPanel.add(infoPanel);


        Runnable updateItemsDisplay = () -> {
            itemsDisplayPanel.removeAll();

            if (!items.isEmpty()) {
                ArrayList<String> item = items.get(currentIndex[0]);
                JButton itemButton = createImageButton(ITEMS + item.get(0) + ".png", 65, 140, 100, 100);
                itemsDisplayPanel.add(itemButton);
                String texto = item.get(1).replace("\n", "\n\n");
                infoPanel.setText(texto);
            }
            itemsDisplayPanel.revalidate();
            itemsDisplayPanel.repaint();
        };

        nextButton.addActionListener(e -> {
            if (currentIndex[0] < items.size() - 1) {
                currentIndex[0]++;
                updateItemsDisplay.run();
            }
        });

        prevButton.addActionListener(e -> {
            if (currentIndex[0] > 0) {
                currentIndex[0]--;
                updateItemsDisplay.run();
            }
        });

        // Acciones de navegación
        backButton.addActionListener(e -> refresh(menuPanel));

        // Mostrar los primeros items
        updateItemsDisplay.run();
        getContentPane().removeAll();
        add(itemsPanel);
        revalidate();
        repaint();
    }

    private void prepareGameMode() {
        gameMode = new JPanel(new BorderLayout()) {
            private ImageIcon gifGameMode = new ImageIcon(MENU + "gameModeMenu.gif");
            protected void paintComponent(Graphics g) {
                g.drawImage(gifGameMode.getImage(), 0, 0, getWidth(), getHeight(), this);
                super.paintComponent(g);
            }
        };
        gameMode.setOpaque(false);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(null);
        centerPanel.setOpaque(false);
        //

        JLabel messageLabel = new JLabel("Modos de Juego");
        messageLabel.setFont(cargarFuentePixel(27));
        messageLabel.setForeground(Color.white);
        messageLabel.setHorizontalAlignment(JLabel.LEFT);
        messageLabel.setBounds(30, 12, 400, 50);

        JLabel messageLabel1 = new JLabel("Jugador vs Maquina");
        messageLabel1.setFont(cargarFuentePixel(16));
        messageLabel1.setForeground(Color.white);
        messageLabel1.setHorizontalAlignment(JLabel.LEFT);
        messageLabel1.setBounds(160, 60, 550, 108);

        JLabel messageLabel11 = new JLabel("<html>Modo clasico, un jugador<br>juega contra una maquina.</html>");
        messageLabel11.setFont(cargarFuentePixel(12));
        messageLabel11.setForeground(Color.white);
        messageLabel11.setHorizontalAlignment(JLabel.LEFT);
        messageLabel11.setBounds(160, 108, 223, 108);

        JLabel messageLabel2 = new JLabel("Supervivencia");
        messageLabel2.setFont(cargarFuentePixel(16));
        messageLabel2.setForeground(Color.white);
        messageLabel2.setHorizontalAlignment(JLabel.LEFT);
        messageLabel2.setBounds(185, 225, 550, 50);

        JLabel messageLabel21 = new JLabel("<html>Un jugador juega contra<br>otro jugador y tendran que sobrevivir.</html>");
        messageLabel21.setFont(cargarFuentePixel(12));
        messageLabel21.setForeground(Color.white);
        messageLabel21.setHorizontalAlignment(JLabel.LEFT);
        messageLabel21.setBounds(160, 265, 223, 50);

        JLabel messageLabel3 = new JLabel("Maquina vs Maquina");
        messageLabel3.setFont(cargarFuentePixel(16));
        messageLabel3.setForeground(Color.white);
        messageLabel3.setHorizontalAlignment(JLabel.LEFT);
        messageLabel3.setBounds(163, 360, 550, 50);

        JLabel messageLabel31 = new JLabel("<html>Modo clasico, una maquina<br>juega contra otra maquina.</html>");
        messageLabel31.setFont(cargarFuentePixel(12));
        messageLabel31.setForeground(Color.white);
        messageLabel31.setHorizontalAlignment(JLabel.LEFT);
        messageLabel31.setBounds(160, 404, 223, 50);

        JLabel messageLabel4 = new JLabel("Jugador vs Jugador");
        messageLabel4.setFont(cargarFuentePixel(16));
        messageLabel4.setForeground(Color.white);
        messageLabel4.setHorizontalAlignment(JLabel.LEFT);
        messageLabel4.setBounds(516, 93, 223, 50);

        JLabel messageLabel41 = new JLabel("<html>Modo clasico, un jugador<br>juega contra otro jugador.</html>");
        messageLabel41.setFont(cargarFuentePixel(12));
        messageLabel41.setForeground(Color.white);
        messageLabel41.setHorizontalAlignment(JLabel.LEFT);
        messageLabel41.setBounds(520, 135, 223, 50);

        ImageIcon message = new ImageIcon(FRAME + "4.png");

        Image scaledMessage = message.getImage().getScaledInstance(400, 60, Image.SCALE_SMOOTH);
        Image scaledMessage1 = message.getImage().getScaledInstance(223, 108, Image.SCALE_SMOOTH);
        Image scaledMessage2 = message.getImage().getScaledInstance(223, 108, Image.SCALE_SMOOTH);
        Image scaledMessage3 = message.getImage().getScaledInstance(223, 108, Image.SCALE_SMOOTH);
        Image scaledMessage4 = message.getImage().getScaledInstance(223, 108, Image.SCALE_SMOOTH);

        JButton messageButton = new JButton(new ImageIcon(scaledMessage));
        JButton messageButton1 = new JButton(new ImageIcon(scaledMessage1));
        JButton messageButton2 = new JButton(new ImageIcon(scaledMessage2));
        JButton messageButton3 = new JButton(new ImageIcon(scaledMessage3));
        JButton messageButton4 = new JButton(new ImageIcon(scaledMessage4));

        messageButton.setBounds(10, 10, 400, 60);
        messageButton1.setBounds(143, 90, 223, 108);
        messageButton2.setBounds(143, 225, 223, 108);
        messageButton3.setBounds(143, 360, 223, 108);
        messageButton4.setBounds(503, 90, 223, 108);

        messageButton.setBorderPainted(false);
        messageButton1.setBorderPainted(false);
        messageButton2.setBorderPainted(false);
        messageButton3.setBorderPainted(false);
        messageButton4.setBorderPainted(false);

        messageButton.setContentAreaFilled(false);
        messageButton1.setContentAreaFilled(false);
        messageButton2.setContentAreaFilled(false);
        messageButton3.setContentAreaFilled(false);
        messageButton4.setContentAreaFilled(false);

        playerVsPlayer = createImageButton(BUTTONS + "pvp.png", 370, 80, 128, 128);
        playerVsMachine = createImageButton(BUTTONS+"pvm.png",10, 80, 128, 128);
        survival = createImageButton(BUTTONS+"survival.png",10, 215, 128, 128);
        machines = createImageButton(BUTTONS+"mvm.png",10, 350, 128, 128);
        backButtonMenu = Auxiliar.crearBotonEstilizado("Volver",new Rectangle(640, 10, 80, 25),new Color(240, 240, 240, 200));

        centerPanel.add(messageLabel);
        centerPanel.add(messageLabel1);
        centerPanel.add(messageLabel2);
        centerPanel.add(messageLabel3);
        centerPanel.add(messageLabel4);
        centerPanel.add(messageLabel11);
        centerPanel.add(messageLabel21);
        centerPanel.add(messageLabel31);
        centerPanel.add(messageLabel41);
        centerPanel.add(messageButton);
        centerPanel.add(messageButton1);
        centerPanel.add(messageButton2);
        centerPanel.add(messageButton3);
        centerPanel.add(messageButton4);
        centerPanel.add(playerVsPlayer);
        centerPanel.add(playerVsMachine);
        centerPanel.add(survival);
        centerPanel.add(machines);
        centerPanel.add(backButtonMenu);
        gameMode.add(centerPanel, BorderLayout.CENTER);
    }
    private void prepareActionsGameMode() {
        playerVsPlayer.addActionListener(e -> {
            gameModeName = "pvp";
            preparePlayersBeforeGame();
        });

        playerVsMachine.addActionListener(e -> {
            gameModeName = "pvm";
            preparePlayersBeforeGame();
        });

        survival.addActionListener(e -> {
            gameModeName = "survival";
            preparePlayersBeforeGame();
        });
        machines.addActionListener(e -> {
            gameModeName = "mvm";
            preparePlayersBeforeGame();
        });
        backButtonMenu.addActionListener(e -> refresh(menuPanel));
    }

    private void preparePlayersBeforeGame(){ // Revisar diseño, si se le puede mejorar
        String titleMessage = "";
        playersPanel = new JPanel(new BorderLayout()) {
            private ImageIcon gifPlayers = new ImageIcon(MENU + "fondoPre3.gif");
            protected void paintComponent(Graphics g) {
                g.drawImage(gifPlayers.getImage(), 0, 0, getWidth(), getHeight(), this);
                super.paintComponent(g);
            }
        };
        playersPanel.setOpaque(false);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(null);
        centerPanel.setOpaque(false);

        switch (gameModeName) {
            case "pvp", "survival" -> titleMessage = "Elige los nombres de los jugadores";
            case "pvm" -> titleMessage = "Elige nombres del jugador y maquina";
            case "mvm" -> titleMessage = "Elige los nombres de las maquinas";
        }

        JLabel titleLabel = new JLabel(titleMessage, JLabel.CENTER);
        titleLabel.setFont(cargarFuentePixel(25));
        titleLabel.setForeground(Color.white);
        titleLabel.setBounds(65, 12, 600, 50);


        ImageIcon titleImage =  new ImageIcon(FRAME + "4.png");
        Image scaledTitleImage = titleImage.getImage().getScaledInstance(600, 60, Image.SCALE_SMOOTH);
        JButton titleLabelButton = new JButton(new ImageIcon(scaledTitleImage));
        titleLabelButton.setBounds(0, 10, 740, 60);
        titleLabelButton.setBorderPainted(false);
        titleLabelButton.setContentAreaFilled(false);

        ImageIcon p1 = new ImageIcon(CHARACTER + "Bruno.png");
        ImageIcon p2 = new ImageIcon(CHARACTER + "Aura.png");

        Image scaledP1 = p1.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        Image scaledP2 = p2.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);

        JButton p1Button = new JButton(new ImageIcon(scaledP1));
        JButton p2Button = new JButton(new ImageIcon(scaledP2));

        p1Button.setBounds(120, 150, 200, 200);
        p2Button.setBounds(430, 150, 200, 200);

        p1Button.setBorderPainted(false);
        p2Button.setBorderPainted(false);

        p1Button.setContentAreaFilled(false);
        p2Button.setContentAreaFilled(false);

        // Campos para jugador 1
        JLabel player1Label = new JLabel("Jugador 1:");
        player1Label.setFont(cargarFuentePixel(20));
        player1Label.setForeground(Color.white);
        player1Label.setBounds(150, 140, 150, 30);

        player1Field = new JTextField();
        player1Field.setFont(cargarFuentePixel(20));
        player1Field.setBounds(120, 360, 200, 30);

        // Campos para jugador 2
        JLabel player2Label = new JLabel("Jugador 2:");
        player2Label.setFont(cargarFuentePixel(20));
        player2Label.setForeground(Color.white);
        player2Label.setBounds(460, 140, 150, 30);

        player2Field = new JTextField();
        player2Field.setFont(cargarFuentePixel(20));
        player2Field.setBounds(420, 360, 200, 30);

        switch (gameModeName){
            case "pvp", "survival" -> {
                player1Label.setText("Jugador 1:");
                player2Label.setText("Jugador 2:");
            }
            case "pvm" -> {
                player1Label.setText(" Jugador:");
                player2Label.setText(" Maquina:");
            }
            case "mvm" -> {
                player1Label.setText("Maquina 1:");
                player2Label.setText("Maquina 2:");
            }
        }


        backButtonMenu = Auxiliar.crearBotonEstilizado("Volver",new Rectangle(10, 450, 80, 25),new Color(240, 240, 240, 200));
        startButton = Auxiliar.crearBotonEstilizado("Siguiente", new Rectangle(605, 450, 120, 25), new Color(240, 240, 240, 200));
        centerPanel.add(titleLabel);
        centerPanel.add(titleLabelButton);
        centerPanel.add(p1Button);
        centerPanel.add(p2Button);
        centerPanel.add(player1Label);
        centerPanel.add(player2Label);
        centerPanel.add(player1Field);
        centerPanel.add(player2Field);
        centerPanel.add(backButtonMenu);
        centerPanel.add(startButton);

        playersPanel.add(centerPanel, BorderLayout.CENTER);
        prepareActionsPlayersBeforeGame();
        refresh(playersPanel);
    }

    private void prepareActionsPlayersBeforeGame() {
        startButton.addActionListener(e -> {
            player1Name = player1Field.getText().trim();
            player2Name = player2Field.getText().trim();

            if (player1Name.isEmpty() || player2Name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor ingresa nombres para ambos jugadores", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (player1Name.equals(player2Name)) {
                JOptionPane.showMessageDialog(this, "Los nombres de los jugadores no pueden ser iguales", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (player1Name.length() > 10 || player2Name.length() > 10) {
                JOptionPane.showMessageDialog(this, "Los nombres no pueden tener mas de 10 caracteres", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            } else if (player1Name.length() < 3 || player2Name.length() < 3) {
                JOptionPane.showMessageDialog(this, "Los nombres no pueden tener menos de 3 caracteres", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            switch (gameModeName){ //Revisar si afecta el juego
                case("pvp") -> {
                    createTrainers("Player1","Player2");
                    prepareItem();
                    choosePokemon();
                }
                case("pvm") -> {
                    String machine = chooseMachine("Escoge maquina","Por escoger una maquina");
                    createTrainers("Player1",machine);
                    prepareItem();
                    choosePokemon();
                }
                case("survival") -> {
                    createTrainers("Player1","Player2");
                    if(booleanInput("Quiere inicial partida en survival?")) {
                        createDataForGame();
                        showTimer("s");
                    }
                }
                case("mvm") -> {
                    String machine1 = chooseMachine("Escoge maquina1","Por escoger una maquina");
                    String machine2 = chooseMachine("Escoge maquina2","Por escoger una maquina");
                    createTrainers(machine1+"1",machine2+"2");
                    prepareItem();
                    choosePokemon();
                }
            }
        });
        backButtonMenu.addActionListener(e -> refresh(gameMode));
    }

    private void choosePokemon() { //Estudiar Codigo
        choosePokemonPanel = new JPanel(new BorderLayout()){
            private ImageIcon gifBg = new ImageIcon(MENU + "fondoPre7.png");
            @Override
            protected void paintComponent(Graphics g) {
                g.drawImage(gifBg.getImage(), 0, 0, getWidth(), getHeight(), this);
                super.paintComponent(g);
            }
        };
        choosePokemonPanel.setOpaque(false);
        choosePokemonPanel.setLayout(null);

        ArrayList<Integer> selectedPokemons1 = new ArrayList<>();
        ArrayList<Integer> selectedPokemons2 = new ArrayList<>();
        final int[] currentPlayer = {1};

        // Título
        JLabel titleLabel = new JLabel("Elige los pokemones", JLabel.LEFT);
        titleLabel.setFont(cargarFuentePixel(25));
        titleLabel.setForeground(Color.white);
        titleLabel.setBounds(30, 10, 400, 60);
        choosePokemonPanel.add(titleLabel);

        // Marco del título
        JButton titleFrame = new JButton(new ImageIcon(new ImageIcon(FRAME + "4.png").getImage()
                .getScaledInstance(340, 60, Image.SCALE_SMOOTH)));
        titleFrame.setBounds(10, 10, 340, 60);
        titleFrame.setBorderPainted(false);
        titleFrame.setContentAreaFilled(false);
        choosePokemonPanel.add(titleFrame);

        // Etiqueta del turno
        JLabel turnLabel = new JLabel(player1Name + " elige: ", JLabel.LEFT);
        turnLabel.setFont(cargarFuentePixel(22));
        turnLabel.setForeground(Color.black);
        turnLabel.setBounds(55, 335, 220, 35);
        choosePokemonPanel.add(turnLabel);

        // Personaje actual
        JLabel characterImage = new JLabel(Auxiliar.scaleIcon(new ImageIcon(CHARACTER + "Bruno.png"), 250, 250));
        characterImage.setBounds(20, 95, 250, 250);
        choosePokemonPanel.add(characterImage);

        // Grid de Pokémon
        JPanel gridPanel = new JPanel(new GridLayout(0, 6, 5, 5));
        gridPanel.setOpaque(false);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ScrollPane
        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBounds(275, 85, 450, 350);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 100), 3),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        verticalBar.setUnitIncrement(16);
        verticalBar.setPreferredSize(new Dimension(10, 0));

        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        choosePokemonPanel.add(scrollPane);

        // Botones de navegación
        JButton backButton = Auxiliar.crearBotonEstilizado("Volver", new Rectangle(10, 450, 100, 30),
                new Color(240, 240, 240, 200));
        choosePokemonPanel.add(backButton);

        JButton doneButton = Auxiliar.crearBotonEstilizado("Listo", new Rectangle(630, 450, 100, 30),
                new Color(240, 240, 240, 200));
        doneButton.setVisible(false);
        choosePokemonPanel.add(doneButton);

        // Añadir Pokémon a la grid con diseño mejorado
        for (int i = 1; i <= 386; i++) {
            final int pokemonId = i;
            JButton pokemonButton = createImageButton(POKEMONES + "Icon/" + i + ".png", 1, 1, 50, 50);
            pokemonButton.setOpaque(false);
            pokemonButton.setContentAreaFilled(false);
            pokemonButton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 2),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));

            pokemonButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    pokemonButton.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(255, 255, 255, 200), 2),
                            BorderFactory.createEmptyBorder(5, 5, 5, 5)
                    ));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    pokemonButton.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 2),
                            BorderFactory.createEmptyBorder(5, 5, 5, 5)
                    ));
                }
            });

            pokemonButton.addActionListener(e -> {
                ArrayList<Integer> currentSelection = currentPlayer[0] == 1 ? selectedPokemons1 : selectedPokemons2;

                if (currentSelection.size() < 6) {
                    currentSelection.add(pokemonId);
                    pokemonButton.setEnabled(false);

                    if (currentSelection.size() == 6) {
                        if (currentPlayer[0] == 1) {
                            // Cambiar al jugador 2
                            currentPlayer[0] = 2;
                            turnLabel.setText(player2Name + " elige: ");
                            characterImage.setIcon(Auxiliar.scaleIcon(new ImageIcon(CHARACTER + "Aura.png"), 250, 250));

                            // Deshabilitar solo los Pokémon seleccionados por el jugador 1
                            Component[] components = gridPanel.getComponents();
                            for (Component comp : components) {
                                if (comp instanceof JButton) {
                                    JButton btn = (JButton) comp;
                                    int btnId = components.length - Arrays.asList(components).indexOf(btn);
                                    if (selectedPokemons1.contains(btnId)) {
                                        btn.setEnabled(false);
                                    } else {
                                        btn.setEnabled(true); // Habilitar el resto de botones
                                    }
                                }
                            }
                        } else {
                            doneButton.setVisible(true);
                        }
                    }
                }
            });
            gridPanel.add(pokemonButton);
        }

        // Eventos de botones
        backButton.addActionListener(e -> {
            choosePokemonPanel.removeAll();
            refresh(playersPanel);
        });

        doneButton.addActionListener(e -> {
            assingPokemon(selectedPokemons1, selectedPokemons2);
            chooseMoves();
        });
        setContentPane(choosePokemonPanel);
        revalidate();
        repaint();
    }

    private void chooseMoves() {
        chooseMovesPanel = new JPanel(new BorderLayout()) {
            private ImageIcon gifBg = new ImageIcon(MENU + "fondoPre7.png");
            @Override
            protected void paintComponent(Graphics g) {
                g.drawImage(gifBg.getImage(), 0, 0, getWidth(), getHeight(), this);
                super.paintComponent(g);
            }
        };
        chooseMovesPanel.setOpaque(false);
        chooseMovesPanel.setLayout(null);

        ArrayList<Integer> selectedMoves1 = new ArrayList<>();
        ArrayList<Integer> selectedMoves2 = new ArrayList<>();
        ArrayList<Integer> pokemones = new ArrayList<>();

        pokemones.addAll(this.pokemones.get(players.get(0)));
        pokemones.addAll(this.pokemones.get(players.get(1)));

        // Título
        JLabel titleLabel = new JLabel("Elige los movimientos", JLabel.LEFT);
        titleLabel.setFont(cargarFuentePixel(25));
        titleLabel.setForeground(Color.white);
        titleLabel.setBounds(30, 10, 400, 60);
        chooseMovesPanel.add(titleLabel);

        // Marco del título
        JButton titleFrame = new JButton(new ImageIcon(new ImageIcon(FRAME + "4.png").getImage().getScaledInstance(350, 60, Image.SCALE_SMOOTH)));
        titleFrame.setBounds(10, 10, 360, 60);
        titleFrame.setBorderPainted(false);
        titleFrame.setContentAreaFilled(false);
        chooseMovesPanel.add(titleFrame);

        // Etiqueta del turno
        JLabel turnLabel = new JLabel(player1Name + " elige para:", JLabel.CENTER);
        turnLabel.setFont(cargarFuentePixel(22));
        turnLabel.setForeground(Color.black);
        turnLabel.setBounds(15, 230, 250, 35);
        chooseMovesPanel.add(turnLabel);

        JLabel characterImage = new JLabel(Auxiliar.scaleIcon(new ImageIcon(CHARACTER + "Bruno.png"), 150, 150));
        characterImage.setBounds(50, 75, 150, 150);
        chooseMovesPanel.add(characterImage);

        JLabel characterImage2 = new JLabel(Auxiliar.scaleIcon(new ImageIcon(CHARACTER + "Aura.png"), 150, 150));
        characterImage2.setBounds(50, 75, 150, 150);
        characterImage2.setVisible(false);
        chooseMovesPanel.add(characterImage2);

        // Panel para la imagen del Pokémon actual
        JLabel pokemonImage = new JLabel(Auxiliar.scaleIcon(new ImageIcon(NORMAL_PATH + pokemones.get(0) + ".png"), 150, 150));
        pokemonImage.setBounds(50, 270, 150, 150);
        chooseMovesPanel.add(pokemonImage);

        // Grid de movimientos
        JPanel gridPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        gridPanel.setOpaque(false);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ScrollPane
        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBounds(275, 85, 450, 350);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 100), 3),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // Configurar scrollbar
        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        verticalBar.setUnitIncrement(16);
        verticalBar.setPreferredSize(new Dimension(10, 0));

        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        chooseMovesPanel.add(scrollPane);

        JButton backButtonGameMode = Auxiliar.crearBotonEstilizado("Volver", new Rectangle(10, 450, 100, 30), new Color(240, 240, 240, 200));
        chooseMovesPanel.add(backButtonGameMode);

        JButton addButton = Auxiliar.crearBotonEstilizado("Añadir", new Rectangle(630, 450, 100, 30), new Color(240, 240, 240, 200));
        addButton.setVisible(false);
        chooseMovesPanel.add(addButton);


        JButton doneButton = Auxiliar.crearBotonEstilizado("Listo", new Rectangle(630, 450, 100, 30), new Color(240, 240, 240, 200));
        doneButton.setBackground(new Color(200, 200, 200, 150));
        doneButton.setVisible(false);
        chooseMovesPanel.add(doneButton);

        final int[] pokemonActualIndex = {0};
        ArrayList<ArrayList<Component>> buttonsPerPokemon = new ArrayList<>();
        ArrayList<ArrayList<String[]>> moves = new ArrayList<>();
        for (Integer i : pokemones){
            moves.add(game.getCompatibleAttacks(i));
        }
        for (ArrayList<String[]> movesPokemon : moves){
            ArrayList<Component> buttons = new ArrayList<>();
            for (int i = 0; i < movesPokemon.size(); i++) {
                final Integer pokemonId = i;

                ImageIcon original = new ImageIcon(POKEMONES + "Normal/" + pokemones.get(0) + ".png");
                Image scaled = original.getImage().getScaledInstance(138, 138, Image.SCALE_SMOOTH);
                pokemonImage.setIcon(new ImageIcon(scaled));

                JButton pokemonButton = Auxiliar.crearBotonTransparente(this.game.getAttackId(Integer.parseInt(movesPokemon.get(i)[0])), new Rectangle(10, 10, 5, 5), false);
                pokemonButton.setOpaque(false);
                pokemonButton.setContentAreaFilled(false);
                pokemonButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
                pokemonButton.setFocusPainted(true);

                pokemonButton.addActionListener(e -> {
                    addButton.setVisible(true);

                    for (ActionListener al : addButton.getActionListeners()) {
                        addButton.removeActionListener(al);
                    }

                    addButton.addActionListener(ae -> {
                        if (selectedMoves1.size() < 24) {
                            selectedMoves1.add(Integer.valueOf(movesPokemon.get(pokemonId)[0]));

                            if (selectedMoves1.size() % 4 == 0) {
                                pokemonActualIndex[0]++;

                                if (pokemonActualIndex[0] < pokemones.size() / 2) {
                                    ImageIcon pk = new ImageIcon(NORMAL_PATH + pokemones.get(pokemonActualIndex[0]) + ".png");
                                    Image pkscaled = pk.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                                    pokemonImage.setIcon(new ImageIcon(pkscaled));
                                }
                            }

                            if (selectedMoves1.size() == 24) {
                                characterImage.setVisible(false);
                                characterImage2.setVisible(true);
                                pokemonActualIndex[0] = pokemones.size() / 2;
                                ImageIcon pk = new ImageIcon(NORMAL_PATH + pokemones.get(pokemonActualIndex[0]) + ".png");
                                Image pkscaled = pk.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                                pokemonImage.setIcon(new ImageIcon(pkscaled));
                                turnLabel.setText(player2Name + " elige para: ");
                            }

                            addButton.setVisible(false);
                        } else if (selectedMoves1.size() == 24 && selectedMoves2.size() < 24) {
                            selectedMoves2.add(Integer.valueOf(movesPokemon.get(pokemonId)[0]));

                            if (selectedMoves2.size() % 4 == 0) {
                                pokemonActualIndex[0]++;

                                if (pokemonActualIndex[0] < pokemones.size()) {
                                    ImageIcon pk = new ImageIcon(NORMAL_PATH + pokemones.get(pokemonActualIndex[0]) + ".png");
                                    Image pkscaled = pk.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                                    pokemonImage.setIcon(new ImageIcon(pkscaled));
                                }

                                // Si se completaron todos los movimientos
                                if (selectedMoves2.size() == 24) {

                                    doneButton.setVisible(true);
                                    assingMoves(selectedMoves1, selectedMoves2);
                                    chooseItems();
                                    return;
                                }
                            }
                            addButton.setVisible(false);
                        }
                    });
                });
                buttons.add(pokemonButton);
            }
            buttonsPerPokemon.add(buttons);
        }

        for (Component component: buttonsPerPokemon.get(pokemonActualIndex[0])){
            gridPanel.add(component);
        }

        backButtonGameMode.addActionListener(e -> choosePokemon());
        doneButton.addActionListener(ev -> {
            assingMoves(selectedMoves1, selectedMoves2);
            chooseItems();
        });

        setContentPane(chooseMovesPanel);
        revalidate();
        repaint();
    }

    private void actualizarGrid(ImagePanel gridPanel, int pokemonActualIndex, ArrayList<ArrayList<Component>> buttonsPerPokemon) {
        gridPanel.removeAll();
        for (Component component: buttonsPerPokemon.get(pokemonActualIndex)){
            gridPanel.add(component);
        }
    }
    private void chooseItems() {
        JPanel chooseItemsPanel = new ImagePanel(new BorderLayout(), MENU + "fondoPre7.png");
        chooseItemsPanel.setOpaque(false);
        //
        JLabel turnLabel = new JLabel(player1Name + "elige", JLabel.CENTER);
        turnLabel.setOpaque(true);  // Esto es crucial para que el fondo sea visible
        turnLabel.setBackground(new Color(50, 50, 50));
        turnLabel.setFont(cargarFuentePixel(18));
        turnLabel.setForeground(Color.blue);
        chooseItemsPanel.add(turnLabel, BorderLayout.NORTH);
        //
        JButton backButtonGameMode = Auxiliar.crearBotonEstilizado("Volver",new Rectangle(275, 100, 20, 60),new Color(240, 240, 240, 200));
        //
        JPanel panelSur = new JPanel(new BorderLayout());
        panelSur.setOpaque(false);
        panelSur.add(new JLabel(" "), BorderLayout.SOUTH);
        panelSur.add(backButtonGameMode, BorderLayout.CENTER);
        //panelSur.add(new JLabel(" "), BorderLayout.WEST);
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension((int) (getWidth() * 0.25), getHeight()));
        leftPanel.add(panelSur, BorderLayout.SOUTH);
        //
        ImageIcon Character = new ImageIcon(CHARACTER + "Bruno.png");
        ImageIcon scaledCharacter = Auxiliar.scaleIcon(Character, 192, 192);
        JLabel characterImage = new JLabel(scaledCharacter);
        characterImage.setHorizontalAlignment(JLabel.CENTER);

        ImageIcon Character2 = new ImageIcon(CHARACTER + "Aura.png");
        ImageIcon scaledCharacter2 = Auxiliar.scaleIcon(Character2, 192, 192);
        JLabel characterImage2 = new JLabel(scaledCharacter2);
        characterImage2.setHorizontalAlignment(JLabel.CENTER);

        JButton doneButton = Auxiliar.crearBotonEstilizado("Listo", new Rectangle(275, 100, 100, 60), new Color(240, 240, 240, 200));
        doneButton.setBackground(new Color(200, 200, 200, 150));

        JPanel rightContent = new JPanel(new GridBagLayout());
        rightContent.setOpaque(false);
        JPanel rightContentPanel = new JPanel(new BorderLayout());
        rightContentPanel.setOpaque(false);
        rightContentPanel.add(characterImage,BorderLayout.NORTH);
        rightContentPanel.add(doneButton,BorderLayout.SOUTH);
        rightContent.add(rightContentPanel);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension((int) (getWidth() * 0.25), getHeight()));
        rightPanel.add(rightContent,BorderLayout.CENTER);
        //
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);

        ImagePanel gridPanel = new ImagePanel(new GridLayout(0, 3, 0, 0), MENU + "blue.png");

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setPreferredSize(new Dimension(300, 400));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        InputMap inputMap = scrollPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = scrollPane.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), "up");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "down");
        actionMap.put("up", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getValue() - vertical.getUnitIncrement());
            }
        });
        actionMap.put("down", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getValue() + vertical.getUnitIncrement());
            }
        });

        JPanel scrollContainer = new JPanel();
        scrollContainer.setOpaque(false);
        scrollContainer.setLayout(new BoxLayout(scrollContainer, BoxLayout.Y_AXIS));
        scrollContainer.add(Box.createVerticalGlue());
        scrollContainer.add(scrollPane);
        scrollContainer.add(Box.createVerticalGlue());
        centerPanel.add(scrollContainer, BorderLayout.CENTER);
        //botones item
        final int[] contador = {0};
        JButton itemButton1 = createImageButton("x2", ITEMS+"potion.png",1,1,100,100,20,false,false);
        JButton itemButton2= createImageButton("x2", ITEMS+"superPotion.png",1,1,100,100,20,false,false);
        JButton itemButton3 = createImageButton("x2", ITEMS+"hyperPotion.png",1,1,100,100,20,false,false);
        JButton itemButton4 = createImageButton("x1", ITEMS+"revive.png",1,1,100,100,20,false,false);

        itemButton1.addActionListener(ev -> {
            String currentPlayer = players.get(contador[0]);
            if(Integer.parseInt(this.items.get(currentPlayer)[0][1]) < 2){
                assingItem(contador[0], 0);
                itemButton1.setText("x"+(2-Integer.parseInt(this.items.get(currentPlayer)[0][1])));
            } else {
                Auxiliar.mostrarError("Maximo de item", "Ya tiene el maximo de este item");
            }
        });
        itemButton2.addActionListener(ev -> {
            String currentPlayer = players.get(contador[0]);
            if(Integer.parseInt(this.items.get(currentPlayer)[1][1]) < 2){
                assingItem(contador[0], 1);
                itemButton2.setText("x"+(2-Integer.parseInt(this.items.get(currentPlayer)[1][1])));
            } else {
                Auxiliar.mostrarError("Maximo de item", "Ya tiene el maximo de este item");
            }
        });
        itemButton3.addActionListener(ev -> {
            String currentPlayer = players.get(contador[0]);
            if(Integer.parseInt(this.items.get(currentPlayer)[2][1]) < 2){
                assingItem(contador[0], 2);
                itemButton3.setText("x"+(2-Integer.parseInt(this.items.get(currentPlayer)[2][1])));
            } else {
                Auxiliar.mostrarError("Maximo de item", "Ya tiene el maximo de este item");
            }
        });
        itemButton4.addActionListener(ev -> {
            String currentPlayer = players.get(contador[0]);
            if(Integer.parseInt(this.items.get(currentPlayer)[3][1]) < 1){
                assingItem(contador[0], 3);
                itemButton4.setText("x"+(1-Integer.parseInt(this.items.get(currentPlayer)[3][1])));
            } else {
                Auxiliar.mostrarError("Maximo de item", "Ya tiene el maximo de este item");
            }
        });

        gridPanel.add(itemButton1);
        gridPanel.add(itemButton2);
        gridPanel.add(itemButton3);
        gridPanel.add(itemButton4);
        backButtonGameMode.addActionListener(e -> chooseMoves());
        doneButton.addActionListener(ev -> {
            if(contador[0] == 0){
                rightContentPanel.removeAll();
                itemButton1.setText("x2");
                itemButton2.setText("x2");
                itemButton3.setText("x2");
                itemButton4.setText("x1");
                gridPanel.repaint();
                rightContentPanel.add(characterImage2, BorderLayout.NORTH);
                rightContentPanel.add(doneButton, BorderLayout.SOUTH);
                rightContentPanel.revalidate();
                rightContentPanel.repaint();
                gridPanel.setBackgroundImage(MENU + "red.png");
                turnLabel.setText(player1Name + "elige");
                turnLabel.setForeground(new Color(255, 100, 100));
                contador[0]++;
            }else{
                showTimer("p");
            }
        });
        chooseItemsPanel.add(leftPanel, BorderLayout.WEST);
        chooseItemsPanel.add(centerPanel, BorderLayout.CENTER);
        chooseItemsPanel.add(rightPanel, BorderLayout.EAST);

        setContentPane(chooseItemsPanel);
        revalidate();
        repaint();
    }
    private void showTimer(String mode) {
        JPanel timerPanel = new ImagePanel(null, MENU + "3.png");
        refresh(timerPanel);

        Timer timer1 = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((ImagePanel) timerPanel).setBackgroundImage(MENU + "2.png");
                timerPanel.repaint(); // Redibuja para aplicar el cambio visual
            }
        });
        Timer timer2 = new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((ImagePanel) timerPanel).setBackgroundImage(MENU + "1.png");
                timerPanel.repaint();
            }
        });

        Timer timer3 = new Timer(3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showBattleStart(mode);
            }
        });

        timer1.setRepeats(false);
        timer2.setRepeats(false);
        timer3.setRepeats(false);

        timer1.start();
        timer2.start();
        timer3.start();
    }
    private void showBattleStart(String mode) {
        JPanel BattleStartPanel = new ImagePanel(new BorderLayout(), MENU + "battleStart.png");
        BattleStartPanel.setLayout(new BorderLayout());

        // Panel para contener los GIFs (sin layout para posicionamiento manual)
        JPanel gifContainer = new JPanel(null);
        gifContainer.setOpaque(false);

        // Cargar el GIF original
        ImageIcon originalGif = new ImageIcon(MENU + "brillo.gif");
        JLabel gifLabel1 = new JLabel();
        JLabel gifLabel2 = new JLabel();

        // Ajustar el tamaño de los GIFs al cambiar el tamaño del contenedor
        gifContainer.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int containerHeight = gifContainer.getHeight();
                // Escalar el GIF proporcionalmente para que coincida con la altura del contenedor
                int newWidth = (int) (containerHeight * ((double) originalGif.getIconWidth() / originalGif.getIconHeight()));
                Image scaledImg = originalGif.getImage().getScaledInstance(newWidth, containerHeight, Image.SCALE_DEFAULT);
                ImageIcon scaledGif = new ImageIcon(scaledImg);

                gifLabel1.setIcon(scaledGif);
                gifLabel2.setIcon(scaledGif);
                gifLabel1.setSize(newWidth, containerHeight);
                gifLabel2.setSize(newWidth, containerHeight);

                // Posición inicial: dos GIFs uno al lado del otro
                gifLabel1.setLocation(0, 0);
                gifLabel2.setLocation(newWidth, 0);
            }
        });

        // Añadir los GIFs al contenedor
        gifContainer.add(gifLabel1);
        gifContainer.add(gifLabel2);

        // Temporizador para la animación de movimiento continuo
        Timer animationTimer = new Timer(16, e -> { // ~60 FPS (1000ms/60 ≈ 16ms)
            // Mover ambos GIFs hacia la izquierda
            gifLabel1.setLocation(gifLabel1.getX() - 2, 0); // Velocidad ajustable
            gifLabel2.setLocation(gifLabel2.getX() - 2, 0);

            // Si un GIF sale completamente por la izquierda, lo reposicionamos a la derecha del otro
            if (gifLabel1.getX() + gifLabel1.getWidth() <= 0) {
                gifLabel1.setLocation(gifLabel2.getX() + gifLabel2.getWidth(), 0);
            }
            if (gifLabel2.getX() + gifLabel2.getWidth() <= 0) {
                gifLabel2.setLocation(gifLabel1.getX() + gifLabel1.getWidth(), 0);
            }
        });

        // Iniciar/detener animación cuando el panel se muestra/oculta
        BattleStartPanel.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (BattleStartPanel.isShowing()) {
                    animationTimer.start();
                    reproducirSonido("1-14.ReceivedBattlePoints_.wav");
                } else {
                    animationTimer.stop();
                    detenerSonido();

                }
            }
        });
        Timer timer1 = new Timer(4300, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initMode(mode);
            }
        });
        timer1.setRepeats(false);
        timer1.start();
        BattleStartPanel.add(gifContainer, BorderLayout.CENTER);
        refresh(BattleStartPanel);
    }
    private void startBattle(POOBkemon game) {
        PokemonBattlePanel battlePanel = new PokemonBattlePanel(game, fondo, frame);
        battlePanel.setBattleListener(playerWon -> {
            showFishPanel();
            //game.deleteGame();
        });
        refresh(battlePanel);
    }
    private void showFishPanel(){
        int winnerId = 0;
        try {
            winnerId = game.getWinner();
        } catch (POOBkemonException e) {
            throw new RuntimeException(e);
        }
        JPanel FishPanel = new ImagePanel(new BorderLayout(), WINNER+winnerId+PNG_EXT);
        JLabel message = new JLabel("Jugador " +winnerId+" a ganado", SwingConstants.CENTER);
        message.setFont(Auxiliar.cargarFuentePixel(30));
        message.setForeground(Color.WHITE);
        FishPanel.add(message, BorderLayout.CENTER);
        Timer timer = new Timer(5000, e -> {
            refresh(menuPanel);
        });
        timer.setRepeats(false);
        timer.start();
        getContentPane().removeAll();
        add(FishPanel);
        revalidate();
        repaint();
    }
    // ========== Métodos auxiliares ========== //
    private void initMode(String mode){
        try {
            if (mode.equals("s")) {
                this.game.resetInstance();
                this.game = Survive.getInstance();
                game.initGame(this.players, this.pokemones, this.items, this.moves, this.random);
            } else if (mode.equals("p")) {
                this.game.resetInstance();
                this.game = POOBkemon.getInstance();
                game.initGame(this.players, this.pokemones, this.items, this.moves, this.random);
            }
            startBattle(game);
        }catch (POOBkemonException e){
            Log.record(e);
            refresh(IntroductionPanel);
            Auxiliar.mostrarError("POOBkemon Error",e.getMessage());
        }
    }
    private JButton createImageButton(String imagePath, int x, int y, int width, int height) {
        JButton button = new JButton();
        button.setBounds(x, y, width, height);

        try {
            // Cargar y escalar la imagen
            BufferedImage originalImage = ImageIO.read(new File(imagePath));
            Image scaledImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(scaledImage));
        } catch (IOException e) {
            Log.record(e);
            button.setText("No image");
        }

        // Hacer el botón transparente
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);

        return button;
    }
    private JButton createImageButton(String text, String imagePath, int x, int y, int width, int height, int fontSize, boolean alineado, boolean cubrirBoton) {
        JButton button = new JButton();

        // Soporte para saltos de línea usando HTML
        String formattedText = "<html>" + text.replace("\n", "<br>") + "</html>";
        button.setText(formattedText);

        // Si cubrirBoton es true, la imagen será del tamaño completo del botón
        int iconWidth = cubrirBoton ? width : 50;
        int iconHeight = cubrirBoton ? height : 50;

        ImageIcon icon = new ImageIcon(imagePath);
        Image scaledImage = icon.getImage().getScaledInstance(iconWidth, iconHeight, Image.SCALE_SMOOTH);
        button.setIcon(new ImageIcon(scaledImage));

        if (cubrirBoton) {
            // Imagen como fondo completo, texto encima
            button.setHorizontalTextPosition(SwingConstants.CENTER);
            button.setVerticalTextPosition(SwingConstants.CENTER);
        } else {
            // Imagen pequeña con texto al costado
            button.setHorizontalTextPosition(SwingConstants.RIGHT);
            button.setVerticalTextPosition(SwingConstants.CENTER);
        }

        button.setFont(cargarFuentePixel(fontSize));
        button.setForeground(Color.BLACK);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setBounds(x, y, width, height);

        if (alineado) {
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setMargin(new Insets(0, 20, 0, 0));
        }

        // Efecto hover para cambiar el color del texto
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                button.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                button.setForeground(Color.BLACK);
            }
        });

        return button;
    }
    private JButton createfillImageButton(String text, String imagePath, int x, int y, int width, int height, int fontSize, boolean alineado, boolean cubrirBoton) {
        String formattedText = "<html>" + text.replace("\n", "<br>") + "</html>";

        JButton button = new JButton(formattedText) {
            Image image = new ImageIcon(imagePath).getImage();

            @Override
            protected void paintComponent(Graphics g) {
                // 1. Fondo transparente
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setComposite(AlphaComposite.SrcOver.derive(0.0f)); // Fondo 100% transparente
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();

                // 2. Dibujar imagen de fondo (solo si cubrirBoton = true)
                if (cubrirBoton && image != null) {
                    g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
                }
                super.paintComponent(g); // Dibuja el texto
            }
        };

        // 3. Configuración base del botón
        button.setFont(cargarFuentePixel(fontSize));
        button.setForeground(Color.BLACK);
        button.setBounds(x, y, width, height);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false); // Importante para transparencia

        // 4. Margen interno (para evitar texto pegado)
        int marginLeft = alineado ? 15 : 5; // 15px si está alineado, 5px si no
        button.setMargin(new Insets(5, marginLeft, 5, 5)); // Arriba, Izq, Abajo, Der

        // 5. Comportamiento según cubrirBoton
        if (!cubrirBoton) {
            ImageIcon icon = new ImageIcon(
                    new ImageIcon(imagePath).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)
            );
            button.setIcon(icon);
            button.setHorizontalTextPosition(SwingConstants.RIGHT);
            button.setVerticalTextPosition(SwingConstants.CENTER);
            button.setIconTextGap(10); // Espacio entre ícono y texto
        } else {
            button.setHorizontalTextPosition(SwingConstants.CENTER);
            button.setVerticalTextPosition(SwingConstants.CENTER);
        }

        // 6. Efecto hover (opcional)
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                button.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                button.setForeground(Color.BLACK);
            }
        });

        return button;
    }
    private boolean booleanInput(String m){
        int respuesta = JOptionPane.showConfirmDialog(
                null,
                m,
                "Confirmación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                new ImageIcon("resources/icon/confirmationGif.gif")
        );
        boolean resultado = (respuesta == JOptionPane.YES_OPTION);
        return resultado;
    }
    private static Font cargarFuentePixel(float tamaño) {
        try {
            Font fuenteBase = Font.createFont(Font.TRUETYPE_FONT,
                    new File("resources/fonts/themevck-text.ttf"));
            Font fuenteNegrita = fuenteBase.deriveFont(Font.BOLD, tamaño);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(fuenteNegrita);
            return fuenteNegrita;

        } catch (FontFormatException | IOException e) {
            Log.record(e);
            return new Font("Monospaced", Font.BOLD, (int)tamaño);
        }
    }
    private void reproducirSonido(String sonido) {
        try {
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(new File(songs+sonido));
            clip = AudioSystem.getClip();
            clip.open(audioInput);
            clip.loop(Clip.LOOP_CONTINUOUSLY); // Repetir mientras el panel esté visible
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void detenerSonido() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
    }
    //Metodos de los botones.
    private void actualizarTextoDificultad() {

        random = !random;
        stastRandomButton.setText(random ? "Stat Aleatorios" : "Stat Base");

        // Efecto visual de cambio
        Timer timer = new Timer(50, null);
        timer.addActionListener(new ActionListener() {
            float opacity = 1.0f;
            boolean fadingOut = true;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (fadingOut) {
                    opacity -= 0.1f;
                    if (opacity <= 0) {
                        fadingOut = false;
                        stastRandomButton.setText(random ? "Stat Aleatorios" : "Stat Base");
                    }
                } else {
                    opacity += 0.1f;
                    if (opacity >= 1.0f) {
                        timer.stop();
                    }
                }
                stastRandomButton.repaint();
            }
        });
        timer.start();
    }
    private String chooseMachine(String tittle, String mensaje) {
        String[] opciones = {"Defensive", "Offensive", "Random", "Expert"};

        int respuesta = JOptionPane.showOptionDialog(
                null,
                mensaje,
                tittle,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]
        );
        switch (respuesta) {
            case 0: return opciones[0];
            case 1: return opciones[1];
            case 2: return opciones[2];
            case 3: return opciones[3];
            default: return opciones[0];
        }
    }
    private void startNewGame() {
        refresh(gameMode);
    }
    private void openGame() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String fileName = selectedFile.getName();
            try {
                game = game.open(selectedFile);
                startBattle(game);
            } catch (POOBkemonException e){
                System.out.println("Exepción" + e.getMessage());
            }
        }
    }

    private void saveGame() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String fileName = selectedFile.getName();
            try{
                game.save(selectedFile);
            } catch (POOBkemonException e){
                System.out.println("Exepción" + e.getMessage());
            }
        }
    }
    private void confirmExit(){
        int option = JOptionPane.showConfirmDialog(
                this,
                "¿Estás seguro de que quieres salir?",
                "Confirmar salida",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,new ImageIcon(EXIT_ICON));

        if (option == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
    //
    private void createDataForGame(){
        createPokemones();
        createMoves();
        prepareItem();
    }
    private void createPokemones(){
        ArrayList<Integer> pokemones1 = new ArrayList<>();
        ArrayList<Integer> pokemones2 = new ArrayList<>();
        for(int i = 0; i<6; i++) {
            pokemones1.add(getNumerRandom(386));
            pokemones2.add(getNumerRandom(386));
        }
        assingPokemon(pokemones1, pokemones2);
    }
    private void createMoves(){
        ArrayList<Integer> pokemons1_moves = new ArrayList<>();
        ArrayList<Integer> pokemons2_moves = new ArrayList<>();
        for(int i = 0; i<24; i++) {
            pokemons1_moves.add(getNumerRandom(354));
            pokemons2_moves.add(getNumerRandom(354));
        }

        assingMoves(pokemons1_moves, pokemons2_moves);
    }
    private void prepareItem(){
        for(int i =0 ; i<2; i++) {
            String[][] items ={{"Potion", "0","25"},{"Potion", "0","50"},{"Potion", "0","100"},{"Revive", "0"}};
            this.items.put(this.players.get(i), items);
        }
    }
    public static int getNumerRandom(int limit) {
        Random random = new Random();
        return random.nextInt(limit) + 1;
    }
    private void createTrainers(String trainer1, String trainer2){
        this.players.clear();
        players.add(trainer1);
        players.add(trainer2);
    }
    private void assingPokemon(ArrayList<Integer> trainer1, ArrayList<Integer> trainer2){
        pokemones.put(players.get(0),trainer1);
        pokemones.put(players.get(1),trainer2);
    }
    private void assingMoves(ArrayList<Integer> trainer1, ArrayList<Integer> trainer2){
        moves.put(players.get(0),trainer1);
        moves.put(players.get(1),trainer2);
    }
    private void assingItem(int player, int item){
        String[][] items = this.items.get(players.get(player));
        items[item][1] = String.valueOf(Integer.parseInt(items[item][1])+1);
    }
    //

    //
    public static void main(String[] args) {
        POOBkemonGUI ventana = new POOBkemonGUI();
        ventana.setVisible(true);
    }
}