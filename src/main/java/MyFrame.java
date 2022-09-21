import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class MyFrame extends JFrame{
    

    
    MyPanel panel;
    String draftMode;
    String scPath;

    
    MyFrame(String mode){
        this.draftMode = mode;
        this.scPath = "src/main/resources/.sc/sc.png";
        panel = new MyPanel(this.draftMode);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(panel);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible((true));
        this.setResizable(false);
    }


    //https://stackoverflow.com/questions/5853879/swing-obtain-image-of-jframe
    public void getScreenShot(String fileName) {
        Component component = this.getContentPane();
        BufferedImage image = new BufferedImage(
                component.getWidth(),
                component.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );
        // call the Component's paint method, using
        // the Graphics object of the image.
        component.paint( image.getGraphics() ); // alternately use .printAll(..)

        try{
            ImageIO.write(image, "png", new File(fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void pickHero(int team, String hero){
        Image img = new ImageIcon("src/main/resources/" + hero +".png").getImage();
        if(team == 0){
            for(int i = 0; i<this.panel.leftPicks.length; i++){
                if(this.panel.leftPicks[i] == null){
                    this.panel.leftPicks[i] = img; 
                    break;
                }
            }
        } else if(team == 1){
            for(int i = 0; i<this.panel.rightPicks.length; i++){
                if(this.panel.rightPicks[i] == null){
                    this.panel.rightPicks[i] = img; 
                    break;
                }
            }
        }
        this.panel.modeIndex++;
        this.panel.time = this.panel.START_TIME;
        this.panel.repaint();
        this.getScreenShot(this.scPath);

    }

    public void banHero(int team, String hero){
        Image img = new ImageIcon("src/main/resources/" + hero +".png").getImage();
        if(team == 0){
            Image scaledImage = img.getScaledInstance(this.panel.leftBanSize, this.panel.leftBanSize, Image.SCALE_SMOOTH);
            for(int i = 0; i<this.panel.leftBans.length; i++){
                if(this.panel.leftBans[i] == null){
                    this.panel.leftBans[i] = new ImageIcon(scaledImage).getImage();
                    break;
                }
            }
        } else if(team == 1){
            Image scaledImage = img.getScaledInstance(this.panel.rightBanSize, this.panel.rightBanSize, Image.SCALE_SMOOTH);
            for(int i = 0; i<this.panel.rightBans.length; i++){
                if(this.panel.rightBans[i] == null){
                    this.panel.rightBans[i] = new ImageIcon(scaledImage).getImage();
                    break;
                }
            }
        }
        this.panel.modeIndex++;
        this.panel.time = this.panel.START_TIME;
        this.panel.repaint();
        this.getScreenShot(this.scPath);
    }
}
