/*
 *  ***** BEGIN LICENSE BLOCK *****
 *
 *  Version: MPL 1.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is "Zemberek Dogal Dil Isleme Kutuphanesi"
 *
 *  The Initial Developer of the Original Code is
 *  Ahmet A. Akin, Mehmet D. Akin.
 *  Portions created by the Initial Developer are Copyright (C) 2006
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *
 *  ***** END LICENSE BLOCK *****
 */

package tr2sql.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


/**
 */
public class GirisAlani {
    private JPanel mainPanel;
    private JTextArea inputArea;
    private char[] ozelKarakterler;
    private JPanel buttonPanel;

    public GirisAlani(char[] ozelKarakterler) {
        this.ozelKarakterler = ozelKarakterler;
        configure();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void setYazi(String yazi) {
        inputArea.setText(yazi);
    }

    public String getYazi() {
        return inputArea.getText();
    }

    private void configure() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(new TitledBorder(new EmptyBorder(2,2,2,2), "Giri\u015f alan\u0131"));
        ozelKarakterDugmeAlaniOlustur(ozelKarakterler);
        mainPanel.add(makeInputPanel(), BorderLayout.CENTER);
    }


    private JPanel makeInputPanel() {
        JPanel pq = new JPanel(new BorderLayout());
        inputArea = new JTextArea();
        inputArea.setBorder(new EmptyBorder(1, 3, 1, 3));
        inputArea.setLineWrap(true);

        // jTextPanel scroll bar icermez. O yuzden onu scroll pane'e ekliyoruz.
        JScrollPane ps = new JScrollPane();
        ps.getViewport().add(inputArea);

        pq.add(ps,BorderLayout.CENTER);
        return pq;
    }

    public void ozelKarakterDugmeAlaniOlustur(char[] ozelKarakterler) {

        if(buttonPanel!=null)
          mainPanel.remove(buttonPanel);

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
        //makechars
        for (char ozelKarakter : ozelKarakterler) {
            JButton button = SwingFactory.getRegularButton(String.valueOf(ozelKarakter));
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    String turkceChar = ((JButton) evt.getSource()).getText();
                    int pos = inputArea.getCaretPosition();
                    inputArea.insert(turkceChar, pos);
                    inputArea.setCaretPosition(pos + 1);
                    inputArea.requestFocus();
                }
            });
            buttonPanel.add(button);
        }
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.repaint();
        mainPanel.validate();
    }

}
