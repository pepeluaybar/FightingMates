package fightingmates.view;

import fightingmates.controller.GameController;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.GridLayout;

public class ActionPanel extends JPanel {
    private final JButton playUnitButton;
    private final JButton useObjectButton;
    private final JButton attackButton;
    private final JButton endTurnButton;

    public ActionPanel(GameController controller, HandPanel handPanel, BoardPanel boardPanel) {
        super(new GridLayout(1, 4, 8, 0));

        playUnitButton = new JButton("Jugar unidad");
        useObjectButton = new JButton("Usar objeto");
        attackButton = new JButton("Atacar");
        endTurnButton = new JButton("Finalizar turno");

        playUnitButton.addActionListener(event -> controller.playUnit(
                handPanel.getSelectedHandIndex(),
                boardPanel.getSelectedCurrentPosition()
        ));
        useObjectButton.addActionListener(event -> controller.useObject(
                handPanel.getSelectedHandIndex(),
                resolveTargetSide(boardPanel),
                resolveTargetPosition(boardPanel)
        ));
        attackButton.addActionListener(event -> controller.attack(
                boardPanel.getSelectedCurrentPosition(),
                boardPanel.getSelectedRivalPosition()
        ));
        endTurnButton.addActionListener(event -> controller.endTurn());

        add(playUnitButton);
        add(useObjectButton);
        add(attackButton);
        add(endTurnButton);
    }

    public void setActionsEnabled(boolean enabled) {
        playUnitButton.setEnabled(enabled);
        useObjectButton.setEnabled(enabled);
        attackButton.setEnabled(enabled);
        endTurnButton.setEnabled(enabled);
    }

    private GameController.BoardSide resolveTargetSide(BoardPanel boardPanel) {
        if (boardPanel.getSelectedRivalPosition() >= 0) {
            return GameController.BoardSide.RIVAL_PLAYER;
        }
        return GameController.BoardSide.CURRENT_PLAYER;
    }

    private int resolveTargetPosition(BoardPanel boardPanel) {
        int rivalPosition = boardPanel.getSelectedRivalPosition();
        if (rivalPosition >= 0) {
            return rivalPosition;
        }
        return boardPanel.getSelectedCurrentPosition();
    }
}
