package gui;

import javax.swing.JPanel;
import javax.swing.JTextField;

public class InputPanel {
    public InputPanel(JPanel panel,JTextField textField){
        this.textField = textField;
        this.panel = panel;
    }

        public JPanel panel;
        public JTextField textField;
}
