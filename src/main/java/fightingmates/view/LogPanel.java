package fightingmates.view;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.util.List;

public class LogPanel extends JPanel {
    private final JTextArea logArea;

    public LogPanel() {
        super(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Registro de partida"));

        logArea = new JTextArea(8, 40);
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        add(new JScrollPane(logArea), BorderLayout.CENTER);
    }

    public void refresh(List<String> messages) {
        logArea.setText(String.join(System.lineSeparator(), messages));
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
