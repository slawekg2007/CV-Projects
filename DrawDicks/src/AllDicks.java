import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.midi.*;
public class AllDicks {
    private JFrame mainFrame;
    private DrawPanel panelR = new DrawPanel();
    private int moveDick=0;
    private boolean dickStormFlag=false;
    private boolean animatedDickFlag=false;
    private boolean soundDicksFlag=false;
    private boolean pulseDickFlag=false;
    private boolean colorDickFlag=false;
    private int howLongWait=5;
    private Sequencer musicSequencer;

    public static void main(String[] args) {
        new AllDicks().makeWindow();
    }

    private void makeWindow() {
        mainFrame = new JFrame("Rysuj Peniski");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel backgroundPanel = new JPanel(layout);
        backgroundPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        Box buttonsArea = new Box(BoxLayout.Y_AXIS);

        JButton dickStorm = new JButton("Kutasoburza");
        dickStorm.addActionListener(new DickStormListener());
        buttonsArea.add(dickStorm);

        JButton animatedDick = new JButton("Animowany Kutas");
        animatedDick.addActionListener(new AnimatedDick());
        buttonsArea.add(animatedDick);

        JButton pulseDick = new JButton("Pulsujące Kutasy");
        pulseDick.addActionListener(new PulseDick());
        buttonsArea.add(pulseDick);

        JButton colorDick = new JButton("Koloruj Kutasa");
        colorDick.addActionListener(new ColorDick());
        buttonsArea.add(colorDick);

        JButton soundDick = new JButton("Dźwiękokutasy");
        soundDick.addActionListener(new SoundDick());
        buttonsArea.add(soundDick);

        JButton slowerDick = new JButton("Wolniej");
        slowerDick.addActionListener(new SlowerDick());
        buttonsArea.add(slowerDick);

        JButton fasterDick = new JButton("Szybciej");
        fasterDick.addActionListener(new FasterDick());
        buttonsArea.add(fasterDick);

        panelR = new DrawPanel();
        backgroundPanel.add(BorderLayout.EAST, buttonsArea);
        backgroundPanel.add(BorderLayout.CENTER, panelR);
        mainFrame.getContentPane().add(backgroundPanel);
        mainFrame.setSize(1600, 800);
        mainFrame.setVisible(true);
        moveDickForever();
    }

    private void makeAllFalse(){
        dickStormFlag=false;
        animatedDickFlag=false;
        soundDicksFlag=false;
        pulseDickFlag=false;
        colorDickFlag=false;
    }

    private void moveDickForever(){
        while (true) {                                      //moving dick forever
            if (!colorDickFlag && !soundDicksFlag) {
                for (int i = 0; i < 200; i++) {
                    moveDick += 5;
                    mainFrame.repaint();
                    if(colorDickFlag || soundDicksFlag) {
                        moveDick=0;
                        break;}
                    try {
                        Thread.sleep(howLongWait);
                    } catch (Exception ignored) {
                    }
                }
                for (int i = 0; i < 200; i++) {
                    moveDick -= 5;
                    mainFrame.repaint();
                    if(colorDickFlag || soundDicksFlag) {
                        moveDick=0;
                        break;}
                    try {
                        Thread.sleep(howLongWait);
                    } catch (Exception ignored) {
                    }
                }
            }
            try {
                Thread.sleep(20);
            } catch (Exception ignored) {
            }
        }
    }

    private void makeMusic() {
        try {
            musicSequencer = MidiSystem.getSequencer();
            musicSequencer.open();
            musicSequencer.addControllerEventListener(panelR, new int[]{127});
            Sequence musicSequence = new Sequence(Sequence.PPQ, 4);
            Track musicTrack = musicSequence.createTrack();
            musicTrack.add(event(192, 100, 0, 1));

            for (int i = 5; i < 61; i++) {
                musicTrack.add(event(144, i, 100, i));
                musicTrack.add(event(176, 127, 0, i));
                musicTrack.add(event(128, i, 100, i + 3));
            }
            for (int i = 5; i < 61; i++) {
                musicTrack.add(event(144, 70 - i, 100,  i+61));
                musicTrack.add(event(176, 127, 0,  i+61));
                musicTrack.add(event(128, 70 - i, 100,  i + 3+61));
            }
            musicSequencer.setSequence(musicSequence);
            musicSequencer.setLoopCount(musicSequencer.LOOP_CONTINUOUSLY);
            musicSequencer.setTempoInBPM(120);
            musicSequencer.start();
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }
    private static MidiEvent event(int plc, int one, int two, int bar) {
        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(plc, 1, one, two);
            event = new MidiEvent(a, bar);
        } catch (Exception ignored) {}
        return event;
    }

    class DickStormListener implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            makeAllFalse();
            dickStormFlag=true;
            musicSequencer.stop();
        }
    }

    class AnimatedDick implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            makeAllFalse();
            animatedDickFlag=true;
            musicSequencer.stop();
        }
    }

    class PulseDick implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            makeAllFalse();
            pulseDickFlag=true;
            musicSequencer.stop();
        }
    }

    class ColorDick implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            makeAllFalse();
            colorDickFlag=true;
            mainFrame.repaint();
            musicSequencer.stop();
        }
    }

    class SoundDick implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            makeMusic();
            makeAllFalse();
            soundDicksFlag=true;
        }
    }

    class SlowerDick implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            howLongWait+=5;
        }
    }

    class FasterDick implements ActionListener{
        public void actionPerformed(ActionEvent e) { 
            
            if (howLongWait>5){ howLongWait-=5; }
            else {howLongWait=1; }
        }
    }

    class DrawPanel extends JPanel implements ControllerEventListener{
        boolean message = false;

        public void controlChange(ShortMessage event) { //draw when music play
            message = true;
            repaint();
        }
        public void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            if (!soundDicksFlag) {
                g2d.setPaint(Color.black);
                g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
            }
            int red = (int) (Math.random() * 256);                     //make random color for gradient
            int green = (int) (Math.random() * 256);
            int blue = (int) (Math.random() * 256);
            g.setColor(new Color(red, green, blue));
            Color startColor = new Color(red,green,blue);
            red = (red+128)%256;
            green = (green+128)%256;
            blue = (blue+128)%256;
            Color endColor = new Color(red,green,blue);
            GradientPaint gradient = new GradientPaint(0,0,startColor, 500,1000, endColor);
            g2d.setPaint(gradient);

            if (dickStormFlag) {
                for (int i = 0; i <5 ; i++) {
                     red = (int) (Math.random() * 256);                     //make random color for gradient
                     green = (int) (Math.random() * 256);
                     blue = (int) (Math.random() * 256);
                    g.setColor(new Color(red, green, blue));
                    int x = (int) (Math.random() * 1100);
                    int y = (int) (Math.random() * 400);
                    g2d.fillOval(x + 20, y + 160, 60, 60);
                    g2d.fillOval(x + 110, y + 160, 60, 60);
                    g2d.fillOval(x + 65, y + 10, 60, 60);
                    g2d.fillRect(x + 65, y + 40, 60, 180);
                }

            }
            if(animatedDickFlag){
                g2d.fillOval(moveDick,500,100,100);
                g2d.fillOval(moveDick+150,500,100,100);
                g2d.fillOval(moveDick+75,250,100,100);
                g2d.fillRect(moveDick+75,300,100,300);
            }
            if(pulseDickFlag){
                for (int j = 0; j <2 ; j++) {
                    for (int i = 0; i < 4; i++) {
                        g2d.fillOval((moveDick / 19) + i * 300, (moveDick / 4) + 50+ j * 300, (moveDick / 10) + 20, (moveDick / 10) + 20);
                        g2d.fillOval((moveDick / 7) + 30 + i * 300, (moveDick / 4) + 50+ j * 300, (moveDick / 10) + 20, (moveDick / 10) + 20);
                        g2d.fillOval((moveDick / 10) + 15 + i * 300, j*300, (moveDick / 10) + 20, (moveDick / 10) + 20);
                        g2d.fillRect((moveDick / 10) + 15 + i * 300, (moveDick / 19) + 10+ j * 300, (moveDick / 10) + 20, (3 * moveDick / 10) + 60);
                    }
                }

            }
            if(colorDickFlag){
                g2d.fillOval(375,450,150,150);
                g2d.fillOval(575,450,150,150);
                g2d.fillOval(475,50,150,150);
                g2d.fillRect(475,125,150,475);
            }
            if (soundDicksFlag) {

                if (message) {
                    int x = (int) (Math.random() * 1100);
                    int y = (int) (Math.random() * 400);
                    g2d.fillOval(x + 20, y + 60, 20, 20);
                    g2d.fillOval(x + 50, y + 60, 20, 20);
                    g2d.fillOval(x + 35, y + 10, 20, 20);
                    g2d.fillRect(x + 35, y + 20, 20, 60);
                    message = false;
                }
            }
        }
    }
}
