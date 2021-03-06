package tr2sql.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Properties;

/**
 * Sonuclarin yazildigi alan.
 */
public class CikisAlani {
    private JPanel mainPanel;
    private JTextArea outputArea;

    public CikisAlani() {
        configure();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void setYazi(String yazi) {
        outputArea.setText(yazi);
    }

    public void configure() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(makeInputPanel(), BorderLayout.CENTER);
        mainPanel.setBorder(new TitledBorder(new EmptyBorder(2, 2, 2, 2), "\u00c7\u0131k\u0131\u015f alan\u0131"));
    }


    public JPanel makeInputPanel() {
        JPanel pq = new JPanel(new BorderLayout());
        outputArea = new JTextArea();
        outputArea.setBorder(new EmptyBorder(1, 3, 1, 3));
        outputArea.setLineWrap(true);
        outputArea.setEditable(false);

        // jTextPanel scroll bar icermez. O yuzden onu scroll pane'e ekliyoruz.
        JScrollPane ps = new JScrollPane();
        ps.getViewport().add(outputArea);

        pq.add(ps, BorderLayout.CENTER);
        return pq;
    }
}
