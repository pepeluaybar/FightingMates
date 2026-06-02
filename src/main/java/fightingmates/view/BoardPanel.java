package fightingmates.view;

import fightingmates.controller.GameState;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;

public class BoardPanel extends JPanel {
    private final JLabel currentPlayerLabel;
    private final JLabel rivalPlayerLabel;
    private final DefaultListModel<GameState.UnitState> currentBoardModel;
    private final DefaultListModel<GameState.UnitState> rivalBoardModel;
    private final JList<GameState.UnitState> currentBoardList;
    private final JList<GameState.UnitState> rivalBoardList;

    public BoardPanel() {
        super(new GridLayout(1, 2, 12, 0));
        setBorder(BorderFactory.createTitledBorder("Tablero"));

        currentPlayerLabel = new JLabel();
        rivalPlayerLabel = new JLabel();
        currentBoardModel = new DefaultListModel<>();
        rivalBoardModel = new DefaultListModel<>();
        currentBoardList = createBoardList(currentBoardModel);
        rivalBoardList = createBoardList(rivalBoardModel);

        add(createPlayerBoard("Jugador actual", currentPlayerLabel, currentBoardList));
        add(createPlayerBoard("Rival", rivalPlayerLabel, rivalBoardList));
    }

    public void refresh(GameState state) {
        currentPlayerLabel.setText(formatPlayer(state.getCurrentPlayer()));
        rivalPlayerLabel.setText(formatPlayer(state.getRivalPlayer()));
        refreshBoard(currentBoardModel, currentBoardList, state.getCurrentBoard());
        refreshBoard(rivalBoardModel, rivalBoardList, state.getRivalBoard());
    }

    public int getSelectedCurrentPosition() {
        return selectedPosition(currentBoardList);
    }

    public int getSelectedRivalPosition() {
        return selectedPosition(rivalBoardList);
    }

    private JList<GameState.UnitState> createBoardList(DefaultListModel<GameState.UnitState> model) {
        JList<GameState.UnitState> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return list;
    }

    private JPanel createPlayerBoard(String title, JLabel label, JList<GameState.UnitState> list) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(label, BorderLayout.NORTH);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        return panel;
    }

    private void refreshBoard(
            DefaultListModel<GameState.UnitState> model,
            JList<GameState.UnitState> list,
            List<GameState.UnitState> units
    ) {
        int selectedIndex = list.getSelectedIndex();
        model.clear();
        for (GameState.UnitState unit : units) {
            model.addElement(unit);
        }
        if (selectedIndex >= 0 && selectedIndex < model.size()) {
            list.setSelectedIndex(selectedIndex);
        }
    }

    private int selectedPosition(JList<GameState.UnitState> list) {
        GameState.UnitState selected = list.getSelectedValue();
        return selected != null ? selected.getPosition() : -1;
    }

    private String formatPlayer(GameState.PlayerState player) {
        return player.getName() + " | Vida: " + player.getLife()
                + " | Mazo: " + player.getDeckSize()
                + " | Descarte: " + player.getDiscardSize();
    }
}
