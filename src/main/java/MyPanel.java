import java.awt.*;
import java.awt.event.*;
import javax.swing.*;



public class MyPanel extends JPanel implements ActionListener{
    

    final int PANEL_WIDTH = 1000;
    final int PANEL_HIGHT = 800;
    final int START_TIME = 5;
    final int START_TEAM_TIME = 5;
    final int IMAGE_SIZE = 120;
    final int MAX_BAN_SIZE = 75;

    Timer timer;
    Image[] leftPicks = {null, null, null, null, null};
    Image[] rightPicks = {null, null, null, null, null};
    Image[] leftBans;
    Image[] rightBans;
    int leftBanCount = 0;
    int rightBanCount = 0;
    int leftBanSize;
    int rightBanSize;
    int time = START_TIME;
    int leftTime = START_TEAM_TIME;
    int rightTime = START_TEAM_TIME;
    String mode;
    int modeIndex = 0;


    // mode string: L = left pick, R = right pick, l = left ban, r = right ban.
    // ex standard draft would be - lrlrLRRLLrlRRLLR
    MyPanel(String m){
        this.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HIGHT));
        this.setBackground(Color.black); 
        this.mode = m;
        this.setupBans();
        timer = new Timer(1000, this);
        timer.start();
    }

    public void paint(Graphics g1d){
        super.paint(g1d);

        Graphics2D g = (Graphics2D) g1d;
        g.setColor(Color.white);
        
        this.drawTimers(g);
        this.drawLeftTeam(g);
        this.drawRightTeam(g);
        this.drawBans(g);

    }
    
    private void drawTimers(Graphics2D g){
        g.drawString(Integer.toString(this.time), PANEL_WIDTH/2, 10);
        g.drawString(Integer.toString(this.leftTime), PANEL_WIDTH/2 - 20, 10);
        g.drawString(Integer.toString(this.rightTime), PANEL_WIDTH/2 + 20, 10);
    }


    private void drawLeftTeam(Graphics2D g){
            for(int i = 0; i<this.leftPicks.length; i++){

                g.drawImage(this.leftPicks[i], 10, (IMAGE_SIZE + 10) * (i+1), null);
            }
        
    }


    private void drawRightTeam(Graphics2D g){
            for(int i = 0; i<this.rightPicks.length; i++){
                g.drawImage(this.rightPicks[i], (PANEL_WIDTH - IMAGE_SIZE - 10), (IMAGE_SIZE + 10) * (i+1), null);
            }
    }

    private void drawBans(Graphics2D g){
        if(this.leftBanCount > 0){
            for(int i = 0; i < leftBans.length; i++){
                if(leftBans[i] != null){
                    g.drawImage(leftBans[i], ((10 + this.leftBanSize) * i) + 10, 10, null);
                }
            }
        }
        if(this.rightBanCount > 0){
            for(int i = 0; i < rightBans.length; i++){
                if(rightBans[i] != null){
                    g.drawImage(rightBans[i], PANEL_WIDTH - ((10 + this.rightBanSize) * i) - rightBanSize - 10, 10, null);
                }
            }
        }
    }

    private void setupBans(){



        for(int i = 0; i < this.mode.length(); i++){
            if(this.mode.charAt(i) == 'l'){
                this.leftBanCount++;
            } else if(this.mode.charAt(i) == 'r'){
                this.rightBanCount++;
            }
        }


        if(this.leftBanCount > 0){
            this.leftBans = new Image[leftBanCount];
            this.leftBanSize = Math.min(350 / leftBanCount, MAX_BAN_SIZE);
        } else{
            this.leftBanSize = MAX_BAN_SIZE;
        }

        if(this.rightBanCount > 0){
            this.rightBans = new Image[rightBanCount];
            this.rightBanSize = Math.min(350 / rightBanCount, MAX_BAN_SIZE);
        } else{
            this.rightBanSize = MAX_BAN_SIZE;
        }

       
    }



    @Override
    public void actionPerformed(ActionEvent e){
        
        if(this.modeIndex < this.mode.length()){
            if(this.time>0){
                this.time -= 1;
            }else if(this.mode.charAt(this.modeIndex) == 'l' || this.mode.charAt(this.modeIndex) == 'L'){
                this.leftTime -= 1;
            }else if(this.mode.charAt(this.modeIndex) == 'r' || this.mode.charAt(this.modeIndex) == 'R'){
                this.rightTime -=1;
            }
        }

        repaint();
    }
}


