import java.io.*;
public class TextDraft {
    

    TextDraft(){
        String in;
        String homeMenu = "choose an option:\n1. start a new draft\n0. exit";
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));


        do{
            System.out.println(homeMenu);
            try {
                in = br.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if(in.charAt(0) == '1'){
                // format of draft format code: L = left pick, R = right pick, l = left ban, r = right ban.
                // ex standard draft would be - lrlrLRRLLrlRRLLR
                System.out.println("Enter draft format: ");

                String format = null;
                try {
                    format = br.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                int leftPicks = 0;
                int rightPicks = 0;

                for(int i = 0; i < format.length(); i++){
                    if(format.charAt(i) == 'L'){
                        leftPicks++;
                    }else if(format.charAt(i) == 'R'){
                        rightPicks++;
                    }
                }

                if(leftPicks == 5 && rightPicks == 5){


                    MyFrame frame = new MyFrame(format);

                    String hero;

                    System.out.println("press enter to start");
                    try {
                        hero = br.readLine();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    frame.panel.timer.start();


                    for(int i = 0; i<frame.draftMode.length(); i++){
                        try {
                            hero = br.readLine();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        switch(frame.draftMode.charAt(i)){
                            case 'L':
                                frame.pickHero(0, hero);
                                break;
                            case 'R':
                                frame.pickHero(1, hero);
                                break;
                            case 'l':
                                frame.banHero(0, hero);
                                break;
                            case 'r':
                                frame.banHero(1, hero);
                                break;
                        }
                    }
                    try {
                        hero = br.readLine();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    frame.dispose();
                } else{
                    System.out.println("Invalid format");
                }
            } 
        } while(in.charAt(0) != '0');
    }
}

