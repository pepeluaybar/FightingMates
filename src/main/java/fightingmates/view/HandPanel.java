package fightingmates.view;

import fightingmates.controller.GameState;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.util.List;

public class HandPanel extends JPanel {
    private final DefaultListModel<GameState.CardState> handModel;
    private final JList<GameState.CardState> handList;

    public HandPanel() {
        super(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Mano del jugador actual"));

        handModel = new DefaultListModel<>();
        handList = new JList<>(handModel);
        handList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(handList), BorderLayout.CENTER);
    }

    public void refresh(List<GameState.CardState> cards) {
        int selectedIndex = handList.getSelectedIndex();
        handModel.clear();
        for (GameState.CardState card : cards) {
            handModel.addElement(card);
        }
        if (selectedIndex >= 0 && selectedIndex < handModel.size()) {
            handList.setSelectedIndex(selectedIndex);
        }
    }

    public int getSelectedHandIndex() {
        GameState.CardState selected = handList.getSelectedValue();
        return selected != null ? selected.getHandIndex() : -1;
    }
}
