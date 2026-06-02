package fightingmates.view;

import fightingmates.Carta;
import fightingmates.Juego;
import fightingmates.Jugador;
import fightingmates.Tablero;
import fightingmates.Unidad;
import fightingmates.controller.GameController;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Ventana principal de Fighting Mates para una primera interfaz gráfica Swing.
 */
public class GameFrame extends JFrame implements GameController.GameView {
    private final GameController controller;
    private final BoardPanel boardPanel;
    private final HandPanel handPanel;
    private final ActionPanel actionPanel;
    private final LogPanel logPanel;
    private final JLabel statusLabel;

    public GameFrame(GameController controller) {
        super("Fighting Mates");
        this.controller = controller;
        this.controller.setView(this);
        this.boardPanel = new BoardPanel();
        this.handPanel = new HandPanel();
        this.actionPanel = new ActionPanel();
        this.logPanel = new LogPanel();
        this.statusLabel = new JLabel();
        configurarVentana();
        refresh(controller.getJuego(), "Interfaz preparada.");
    }

    private void configurarVentana() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1080, 760));
        setLayout(new BorderLayout(10, 10));

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        center.add(boardPanel, BorderLayout.CENTER);
        center.add(handPanel, BorderLayout.SOUTH);

        statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));

        add(statusLabel, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.EAST);
        add(logPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
    }

    @Override
    public void refresh(Juego juego, String mensaje) {
        Runnable refresco = () -> {
            Jugador actual = juego.getJugadorActual();
            Jugador rival = juego.getJugadorRival(actual);
            statusLabel.setText("Turno " + juego.getTurnosJugados() + " · " + actual.getNombre()
                    + " (Vida " + actual.getVida() + ") vs " + rival.getNombre() + " (Vida " + rival.getVida() + ")");
            boardPanel.refresh(juego, actual, rival);
            handPanel.refresh(actual);
            actionPanel.refresh();
            if (mensaje != null && !mensaje.isBlank()) {
                logPanel.addMessage(mensaje);
            }
            revalidate();
            repaint();
        };

        if (SwingUtilities.isEventDispatchThread()) {
            refresco.run();
        } else {
            SwingUtilities.invokeLater(refresco);
        }
    }

    private final class BoardPanel extends JPanel {
        private BoardPanel() {
            setLayout(new GridLayout(2, 1, 8, 8));
            setBorder(BorderFactory.createTitledBorder("Tablero"));
        }

        private void refresh(Juego juego, Jugador actual, Jugador rival) {
            removeAll();
            add(crearFilaCampo("Rival: " + rival.getNombre(), juego.getTablero().getCampo(rival), false));
            add(crearFilaCampo("Actual: " + actual.getNombre(), juego.getTablero().getCampo(actual), true));
        }

        private JPanel crearFilaCampo(String titulo, Unidad[] unidades, boolean aliado) {
            JPanel fila = new JPanel(new BorderLayout(6, 6));
            fila.setBorder(BorderFactory.createTitledBorder(titulo));
            JPanel casillas = new JPanel(new GridLayout(1, Tablero.TAMANIO_CAMPO, 8, 8));

            for (int i = 0; i < unidades.length; i++) {
                JPanel slot = crearSlotUnidad(unidades[i], i, aliado);
                casillas.add(slot);
            }

            fila.add(casillas, BorderLayout.CENTER);
            return fila;
        }

        private JPanel crearSlotUnidad(Unidad unidad, int posicion, boolean aliado) {
            JPanel slot = new JPanel(new BorderLayout());
            slot.setBorder(BorderFactory.createTitledBorder("Posición " + posicion));
            slot.setBackground(aliado ? new Color(238, 248, 255) : new Color(255, 240, 240));

            if (unidad == null) {
                JLabel vacio = new JLabel("Vacío", JLabel.CENTER);
                vacio.setForeground(Color.GRAY);
                slot.add(vacio, BorderLayout.CENTER);
            } else {
                CardView cardView = new CardView(unidad);
                slot.add(cardView, BorderLayout.CENTER);
            }

            slot.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    actionPanel.setBoardSelection(posicion, aliado);
                }
            });
            return slot;
        }
    }

    private final class HandPanel extends JPanel {
        private int selectedHandIndex = -1;

        private HandPanel() {
            setLayout(new BorderLayout(6, 6));
            setBorder(BorderFactory.createTitledBorder("Mano del jugador actual"));
        }

        private void refresh(Jugador actual) {
            removeAll();
            JPanel cards = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));

            for (int i = 0; i < actual.getNumCartasMano(); i++) {
                Carta carta = actual.obtenerCartaMano(i);
                CardView cardView = new CardView(carta);
                int indice = i;
                cardView.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(indice == selectedHandIndex ? Color.BLUE : new Color(45, 52, 65), 3),
                        BorderFactory.createEmptyBorder(6, 6, 6, 6)
                ));
                cardView.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        selectedHandIndex = indice;
                        actionPanel.setHandSelection(indice);
                        refresh(controller.getJugadorActual());
                    }
                });
                cards.add(cardView);
            }

            if (actual.getNumCartasMano() == 0) {
                cards.add(new JLabel("No hay cartas en mano."));
            }

            add(new JScrollPane(cards), BorderLayout.CENTER);
        }
    }

    private final class ActionPanel extends JPanel {
        private final JSpinner handIndexSpinner;
        private final JSpinner ownPositionSpinner;
        private final JSpinner targetPositionSpinner;
        private final JCheckBox alliedTargetCheck;

        private ActionPanel() {
            setLayout(new GridLayout(0, 1, 6, 6));
            setPreferredSize(new Dimension(240, 0));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("Acciones"),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8)
            ));

            handIndexSpinner = crearSpinner(0, 0, Jugador.MANO_MAXIMA - 1);
            ownPositionSpinner = crearSpinner(0, 0, Tablero.TAMANIO_CAMPO - 1);
            targetPositionSpinner = crearSpinner(0, 0, Tablero.TAMANIO_CAMPO - 1);
            alliedTargetCheck = new JCheckBox("Objetivo aliado");

            JButton startButton = new JButton("Iniciar partida");
            JButton playUnitButton = new JButton("Jugar unidad");
            JButton useObjectButton = new JButton("Usar objeto");
            JButton attackButton = new JButton("Atacar unidad");
            JButton directAttackButton = new JButton("Atacar jugador");
            JButton endTurnButton = new JButton("Finalizar turno");

            startButton.addActionListener(e -> controller.iniciarPartida());
            playUnitButton.addActionListener(e -> controller.jugarUnidad(valor(handIndexSpinner), valor(ownPositionSpinner)));
            useObjectButton.addActionListener(e -> controller.usarObjeto(valor(handIndexSpinner), valor(targetPositionSpinner), alliedTargetCheck.isSelected()));
            attackButton.addActionListener(e -> controller.atacar(valor(ownPositionSpinner), valor(targetPositionSpinner)));
            directAttackButton.addActionListener(e -> controller.atacarJugador(valor(ownPositionSpinner)));
            endTurnButton.addActionListener(e -> controller.finalizarTurno());

            add(new JLabel("Carta en mano:"));
            add(handIndexSpinner);
            add(new JLabel("Posición propia / atacante:"));
            add(ownPositionSpinner);
            add(new JLabel("Posición objetivo:"));
            add(targetPositionSpinner);
            add(alliedTargetCheck);
            add(startButton);
            add(playUnitButton);
            add(useObjectButton);
            add(attackButton);
            add(directAttackButton);
            add(endTurnButton);
        }

        private void refresh() {
            // Punto de extensión para habilitar/deshabilitar acciones por fase en futuras versiones.
        }

        private void setHandSelection(int indice) {
            handIndexSpinner.setValue(indice);
        }

        private void setBoardSelection(int posicion, boolean aliado) {
            if (aliado) {
                ownPositionSpinner.setValue(posicion);
            } else {
                targetPositionSpinner.setValue(posicion);
            }
            alliedTargetCheck.setSelected(aliado);
        }

        private JSpinner crearSpinner(int valorInicial, int minimo, int maximo) {
            return new JSpinner(new SpinnerNumberModel(valorInicial, minimo, maximo, 1));
        }

        private int valor(JSpinner spinner) {
            return ((Number) spinner.getValue()).intValue();
        }
    }

    private static final class LogPanel extends JPanel {
        private final JTextArea textArea;

        private LogPanel() {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createTitledBorder("Registro"));
            textArea = new JTextArea(6, 80);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            add(new JScrollPane(textArea), BorderLayout.CENTER);
        }

        private void addMessage(String message) {
            textArea.append("• " + message + System.lineSeparator());
            textArea.setCaretPosition(textArea.getDocument().getLength());
        }
    }
}
