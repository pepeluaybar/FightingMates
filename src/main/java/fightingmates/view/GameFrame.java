package fightingmates.view;

import fightingmates.controller.GameController;
import fightingmates.controller.GameState;
import fightingmates.controller.GameView;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;

/**
 * Ventana principal de FightingMates. La vista solo conoce al controlador y
 * estados inmutables, por lo que no puede modificar directamente el modelo.
 */
public class GameFrame extends JFrame implements GameView {
    private final GameController controller;
    private final BoardPanel boardPanel;
    private final HandPanel handPanel;
    private final ActionPanel actionPanel;
    private final LogPanel logPanel;
    private final JLabel statusLabel;

    public GameFrame(GameController controller) {
        super("FightingMates");
        this.controller = controller;
        this.boardPanel = new BoardPanel();
        this.handPanel = new HandPanel();
        this.actionPanel = new ActionPanel(controller, handPanel, boardPanel);
        this.logPanel = new LogPanel();
        this.statusLabel = new JLabel(" ");

        configureWindow();
        controller.setView(this);
    }

    @Override
    public void refresh(GameState state) {
        Runnable update = () -> {
            statusLabel.setText(formatStatus(state));
            boardPanel.refresh(state);
            handPanel.refresh(state.getHand());
            logPanel.refresh(state.getLogMessages());
            actionPanel.setActionsEnabled(!state.hasWinner());
        };

        if (SwingUtilities.isEventDispatchThread()) {
            update.run();
        } else {
            SwingUtilities.invokeLater(update);
        }
    }

    private void configureWindow() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));

        JPanel centerPanel = new JPanel(new BorderLayout(12, 12));
        centerPanel.add(boardPanel, BorderLayout.CENTER);
        centerPanel.add(handPanel, BorderLayout.SOUTH);

        add(statusLabel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);
        add(logPanel, BorderLayout.EAST);

        setSize(1100, 650);
        setLocationRelativeTo(null);
    }

    private String formatStatus(GameState state) {
        if (state.hasWinner()) {
            return "Ganador: " + state.getWinnerName();
        }
        return "Turno " + state.getTurnsPlayed() + " | Jugador actual: "
                + state.getCurrentPlayer().getName();
    }

    public GameController getController() {
        return controller;
    }
}
