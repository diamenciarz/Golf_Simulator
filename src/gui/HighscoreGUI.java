package gui;//package crazyPutting;

import java.io.*;
import java.util.*;
import javax.swing.*;

public class HighscoreGUI extends JFrame {
    //although different arrays are used, they correspond to each other (name, score)
    static String[] strNames; //starting names array
    static int[] strScores; //starting scores array
    
    static String[] finNames; //final names array
    static int[] finScores; //final scores array

    static String filePath = System.getProperty("user.dir") + "/Phase 1/src/GUI/resources/hs.txt";//path of the file
    
    static boolean empty =false;//boolean of whether text file has any scores or not - avoids errors
    //if text file is not empty, takes names and scores from text file and puts them into their starting arrays
    public static void populateArrays(){
        int cnt =0;
        try {  
            Scanner sc = new Scanner(new File(filePath));
            if (sc.hasNextLine()){//if file not empty
                while(sc.hasNextLine()) {//counts how many lines are in text file
                    sc.nextLine();
                    cnt++;
                }
                sc = new Scanner(new File(filePath));//reitterates the scanner
            
                //System.out.println(cnt);
                strNames =new String[cnt];//creates the starting array
                strScores =new int[cnt];//creates the starting array
            
                for(int i =0; i <cnt; i++) {//populates the array
                    Scanner scan =new Scanner(sc.nextLine()).useDelimiter(",");//using delimeter ","
                    strNames[i] = scan.next();
                    strScores[i] = Integer.parseInt(scan.next());
                }
            }else empty=true;//if text file is empty
            sc.close();
            } catch (FileNotFoundException e) {
                JOptionPane.showMessageDialog(null, "Problem with file");//error message
                //e.printStackTrace();
            }
        //debug code
        //finNames =new String[] {"tom","will","will","j","tom","q"};
        //finScores =new int[] {1,9,8,7,18,5};
        
        /*
        for (int i=0; i<strNames.length; i++) {
            System.out.println(strNames[i]+" "); 
            System.out.println(strScores[i]+"\n");
        }
        */
    }
    
    //sort both arrays based on score, largest to smallest, using bubblesort
    //existing sort methods cannot be used as the name array needs to be adjusted too
    public static void bubbleSortScores() {
        String strTemp;
        int temp;
        boolean srt = false;
        while(!srt) {
            srt = true;
            for (int i = 0; i < strScores.length - 1; i++) {
                if (strScores[i] > strScores[i+1]) {
                    srt = false;
                    strTemp = strNames[i];
                    temp = strScores[i];
                    
                    strNames[i] = strNames[i+1];
                    strScores[i] = strScores[i+1];
                    
                    strNames[i+1] = strTemp;
                    strScores[i+1] = temp;
                }
            }
        }
    }
    
    //removes duplicate names from names array
    public static void removeDuplicateNames() {

        int end = strNames.length;

        for (int i = 0; i < end; i++) {
            for (int j = i + 1; j < end; j++) {
                if (strNames[i].equals(strNames[j])) {                  
                    int move = j;
                    for (int k = j+1; k < end; k++, move++) {
                        strNames[move] = strNames[k];
                        strScores[move] =strScores[k];
                    }
                    end--;
                    j--;
                }
            }
        }
        //creates final arrays for name and scores, which will be shorter if any names are removed
        finNames =new String[end];
        finScores =new int[end];
        
        //populates arrays
        for (int i=0; i<end; i++) {
            finNames[i] =strNames[i];
            finScores[i] =strScores[i];
            //System.out.println("\n"+"\n"+finNames[i]);
        }
    }
    
    public HighscoreGUI() {
        
        initComponents();
        
        populateArrays();//works
        
        if (!empty){
            bubbleSortScores();//works
            removeDuplicateNames();//works 
            //since arrays have alread been sorted by score, largest score will be the score left after duplicates are removed
            
            highscoreTF.setText(finNames[0]+": "+finScores[0]);//sets highest score field
            for (int i=1; i<finNames.length; i++) {//sets other scores area
                 otherScoreTA.append(finNames[i]+": "+finScores[i]+"\n"); 
            }
        }
        
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        jPanel1 = new JPanel();
        jPanel2 = new JPanel();
        jScrollPane1 = new JScrollPane();
        otherScoreTA = new JTextArea();
        clearScoreB = new JButton();
        jLabel3 = new JLabel();
        jLabel4 = new JLabel();
        highscoreTF = new JTextField();
        backB = new JButton();

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Highscores");
        setCursor(new java.awt.Cursor(java.awt.Cursor.CROSSHAIR_CURSOR));

        jPanel2.setBackground(new java.awt.Color(0, 102, 0));
        jPanel2.setCursor(new java.awt.Cursor(java.awt.Cursor.CROSSHAIR_CURSOR));

        otherScoreTA.setEditable(false);
        otherScoreTA.setBackground(new java.awt.Color(0, 255, 0));
        otherScoreTA.setColumns(20);
        otherScoreTA.setFont(new java.awt.Font("Agency FB", 3, 14)); // NOI18N
        otherScoreTA.setRows(5);
        otherScoreTA.setBorder(BorderFactory.createLineBorder(new java.awt.Color(51, 51, 255), 4));
        jScrollPane1.setViewportView(otherScoreTA);

        clearScoreB.setBackground(new java.awt.Color(0, 0, 0));
        clearScoreB.setFont(new java.awt.Font("Tw Cen MT Condensed Extra Bold", 1, 18)); // NOI18N
        clearScoreB.setForeground(new java.awt.Color(204, 204, 204));
        clearScoreB.setText("CLEAR ALL SCORES");
        clearScoreB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearScoreBActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Rockwell Extra Bold", 1, 36)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 51, 153));
        jLabel3.setText("HIGHSCORE");

        jLabel4.setBackground(new java.awt.Color(255, 0, 153));
        jLabel4.setFont(new java.awt.Font("Rockwell Extra Bold", 0, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 0, 153));
        jLabel4.setText("OTHER SCORES");

        highscoreTF.setEditable(false);
        highscoreTF.setBackground(new java.awt.Color(51, 255, 51));
        highscoreTF.setFont(new java.awt.Font("Agency FB", 3, 24)); // NOI18N
        highscoreTF.setForeground(new java.awt.Color(51, 51, 51));
        highscoreTF.setHorizontalAlignment(JTextField.CENTER);
        highscoreTF.setBorder(BorderFactory.createLineBorder(new java.awt.Color(51, 204, 255), 4));
        highscoreTF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                highscoreTFActionPerformed(evt);
            }
        });

        backB.setBackground(new java.awt.Color(0, 0, 0));
        backB.setFont(new java.awt.Font("Gill Sans MT Ext Condensed Bold", 0, 36)); // NOI18N
        backB.setForeground(new java.awt.Color(153, 153, 153));
        backB.setText("<");
        backB.setPreferredSize(new java.awt.Dimension(120, 40));
        backB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backBActionPerformed(evt);
            }
        });

        GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(backB, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
                        .addGap(61, 61, 61)
                        .addComponent(jLabel3))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(154, 154, 154)
                        .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(clearScoreB, GroupLayout.PREFERRED_SIZE, 190, GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(140, 140, 140)
                        .addComponent(highscoreTF, GroupLayout.PREFERRED_SIZE, 220, GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(169, 169, 169)
                        .addComponent(jLabel4)))
                .addContainerGap(123, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jLabel3))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(backB, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(highscoreTF, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(clearScoreB, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
        );

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    //deletes all scores in the text file
    private void clearScoreBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearScoreBActionPerformed
        int i =JOptionPane.showConfirmDialog(null, "Are you sure you want to clear all scores?");
        if(i==0){//confirms task with user
            PrintWriter writer;
            try {
                writer = new PrintWriter(filePath);
                writer.print("");
                writer.close();
            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(null, "Problem with file");
                //Logger.getLogger(HsF.class.getName()).log(Level.SEVERE, null, ex);
            }
            highscoreTF.setText("");//clears text field
            otherScoreTA.setText(""); //clears text area
            
        }
    }//GEN-LAST:event_clearScoreBActionPerformed

    private void highscoreTFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_highscoreTFActionPerformed
        
    }//GEN-LAST:event_highscoreTFActionPerformed

    private void backBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backBActionPerformed
        new MenuGUI().setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_backBActionPerformed
 
     
    public static void main(String args[]) throws IOException {
        
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(HighscoreGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(HighscoreGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(HighscoreGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(HighscoreGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new HighscoreGUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton backB;
    private JButton clearScoreB;
    private JTextField highscoreTF;
    private JLabel jLabel3;
    private JLabel jLabel4;
    private JPanel jPanel1;
    private JPanel jPanel2;
    private JScrollPane jScrollPane1;
    private JTextArea otherScoreTA;
    // End of variables declaration//GEN-END:variables
}
