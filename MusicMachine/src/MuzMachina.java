import java.awt.*;
import javax.swing.*;
import javax.sound.midi.*;
import java.io.*;
import java.util.*;
import java.awt.event.*;

class MusicMachine implements MetaEventListener {

    private JPanel mainPanel;
    private ArrayList<JCheckBox> checkBoxList;
    private Sequencer sequencer;
    private Sequence sequence;
    private Track track;
    private int numberOfInstruments=16;
    private int numberOfBoxes=16;
    private String[] nameOfInstruments = {"64.Low Conga", "63.Open Hi Conga","62.Mute Hi Conga",
            "35.Bass Drum", "42.Closed Hi-Hat", "46.Open Hi-Hat","44.Pedal Hi-Hat",
            "41.Low Floor Tom","45.Low Tom ","43.High Floor Tom","47.Low mid Tom","50.High Tom",
            "38.Acoustic Snare", "39.Hand Clap", "60.Hi Bongo","61.Low Bongo"};
    private int[] instruments = {64,63,62,35,42,46,44,41,45,43,47,50,38,39,60,61};

    public static void main (String[] args) { new MusicMachine().createGUI(); }

    private void createGUI() {
        JFrame mainFrame = new JFrame("MuzMachina");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel backgroundPanel = new JPanel(layout);
        backgroundPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        checkBoxList = new ArrayList<>();
        Box buttonsArea = new Box(BoxLayout.Y_AXIS);

        JButton start = new JButton("Start");
        start.addActionListener(new MyStartListener());
        buttonsArea.add(start);

        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonsArea.add(stop);

        JButton clear = new JButton("Czyść");
        clear.addActionListener(new MyClearListener());
        buttonsArea.add(clear);

        JButton save = new JButton("Zapisz");
        save.addActionListener(new MySaveListener());
        buttonsArea.add(save);

        JButton load = new JButton("Wczytaj");
        load.addActionListener(new MyLoadListener());
        buttonsArea.add(load);

        JButton tempoG = new JButton("Szybciej");
        tempoG.addActionListener(new MyTempoGListener());
        buttonsArea.add(tempoG);

        JButton tempoD = new JButton("Wolniej");
        tempoD.addActionListener(new MyTempoDListener());
        buttonsArea.add(tempoD);

        Box nameArea = new Box(BoxLayout.Y_AXIS);
        for (int i = 0; i < numberOfInstruments; i++) {
            nameArea.add(new Label(nameOfInstruments[i]));
        }

        backgroundPanel.add(BorderLayout.EAST, buttonsArea);
        backgroundPanel.add(BorderLayout.WEST, nameArea);

        mainFrame.getContentPane().add(backgroundPanel);
        //add all boxes to panel
        GridLayout gridOfCheckBoxes = new GridLayout(numberOfInstruments,numberOfBoxes);
        gridOfCheckBoxes.setVgap(1);
        gridOfCheckBoxes.setHgap(2);
        mainPanel = new JPanel(gridOfCheckBoxes);
        backgroundPanel.add(BorderLayout.CENTER, mainPanel);

        for (int i = 0; i < numberOfInstruments*numberOfBoxes; i++) {
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            checkBoxList.add(c);
            mainPanel.add(c);
        }

        configureMidi();
        mainFrame.setBounds(50,50,300,300);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    private void configureMidi() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequencer.addMetaEventListener(this);
            sequence = new Sequence(Sequence.PPQ,4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);

        } catch(Exception e) {e.printStackTrace();}
    }

    private void makeTrackAndPlay() {
        int[] listOfTracks ;
        sequence.deleteTrack(track);
        track = sequence.createTrack();
        for (int i = 0; i < numberOfInstruments; i++) {
            listOfTracks = new int[numberOfBoxes];

            int key = instruments[i];

            for (int j = 0; j < numberOfBoxes; j++ ) {
                JCheckBox jc = checkBoxList.get(j + (numberOfBoxes*i));
                if ( jc.isSelected()) {
                    listOfTracks[j] = key;
                } else {
                    listOfTracks[j] = 0;
                }
            }
            makeTrack(listOfTracks);
        }

        track.add(createEvent(192, 1,0,numberOfBoxes-1));

        try {
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
        } catch(Exception e) {e.printStackTrace();}
    }
    //read boxes status and play music
    public class MyStartListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            makeTrackAndPlay();
        }
    }
    //stop music
    public class MyStopListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            sequencer.stop();
        }
    }
    //make music faster
    public class MyTempoGListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor * 1.1));
        }
    }
    //make music slower
    public class MyTempoDListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor * .9));
        }
    }
    //clear all boxes
    public class MyClearListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            for (int i = 0; i < numberOfInstruments*numberOfBoxes; i++) {
                JCheckBox set = checkBoxList.get(i);
                set.setSelected(false);
            }
        }
    }
    //check status of boxes and save on file
    public class MySaveListener implements ActionListener {
        boolean [] fieldStates=new boolean[numberOfInstruments*numberOfBoxes];
        public void actionPerformed(ActionEvent a) {
            for (int i = 0; i < numberOfInstruments*numberOfBoxes; i++) {
                JCheckBox field =  checkBoxList.get(i);
                if (field.isSelected()){
                    fieldStates[i]=true;
                }
            }
            JFileChooser dataFile = new JFileChooser();
            dataFile.showSaveDialog(mainPanel);

            try {
                FileOutputStream streamFile = new FileOutputStream(dataFile.getSelectedFile());
                ObjectOutputStream os = new ObjectOutputStream(streamFile);
                os.writeObject(fieldStates);
            } catch(Exception ex) {
                ex.printStackTrace();
            }

        }
    }
    //load status of boxes from file
    public class MyLoadListener implements ActionListener {
        boolean [] fieldStates=null;
        public void actionPerformed(ActionEvent a) {

            JFileChooser dialogFile = new JFileChooser();
            dialogFile.showOpenDialog(mainPanel);
            try {
                FileInputStream dataFile = new FileInputStream(dialogFile.getSelectedFile());
                ObjectInputStream is = new ObjectInputStream(dataFile);
                fieldStates = (boolean[]) is.readObject();

            } catch(Exception ex) {ex.printStackTrace();}
            for (int i = 0; i < numberOfInstruments*numberOfBoxes; i++) {
                JCheckBox set =  checkBoxList.get(i);
                if (fieldStates[i]){
                    set.setSelected(true);
                }else{
                    set.setSelected(false);
                }
            }
            sequencer.stop();
            makeTrackAndPlay();
        }
    }

    private void makeTrack(int[] list) {
        for (int i = 0; i < numberOfBoxes; i++) {
            int key = list[i];

            if (key != 0) {
                track.add(createEvent(144, key, 100, i));
                track.add(createEvent(128, key, 100, i+1));
            }
        }
    }
    //this method make one sound
    private static MidiEvent createEvent(int plc, int one, int two, int tact) {
        MidiEvent createSound = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(plc, 9, one, two);
            createSound = new MidiEvent(a, tact);
        } catch(Exception e) { e.printStackTrace(); }
        return createSound;
    }

    public void meta(MetaMessage arg0) {
    }

}
