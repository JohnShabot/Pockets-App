package yonatan.shabot.pocketsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import android.media.AudioAttributes;
import android.media.SoundPool;
import android.widget.Spinner;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity{
    Timer timer;
    TimerTask timerTask;
    Button btnPlay, btnPause, btnStop;
    CheckBox KCheckBox, SHCheckBox;
    int bpm;
    Switch mSwitch, botSwitch;
    boolean isMetronomeActive, isKMuted, isSHMuted;
    ImageView pocketView, barView;
    SoundPool soundp;
    private int bID;
    private int hhID;
    private int ohID;
    private int sID;

    private int sqID;
    private int clickID;

    String currentPocket = "";
    String currentSHPocket = "0";
    String currentKPocket = "0";

    int beat = 0;
    Boolean isPlaying = false;
    Boolean pause = false;
    Boolean isTrinary = false;
    HashMap<String, String> pocketList = new HashMap<>();

    Spinner SHSpinner, KSpinner, BPMSpinner;
    SpinnerAdapter SHadapterItems, KadapterItems, TSHadapterItems, TKadapterItems;
    ArrayAdapter<String> BPMAdapterItems;

    List<String> Kitems, SHitems, TSHitems, TKitems;
    String[] BPMitems;

    public Handler mHandler = new Handler(Looper.getMainLooper());
    ConstraintSet reference = new ConstraintSet();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timer = new Timer();

        addPockets(pocketList);

        // Initializing references to on screen Objects
        pocketView = (ImageView) findViewById(R.id.pocket_view);
        barView = (ImageView) findViewById(R.id.barView);
        btnPlay = (Button) findViewById(R.id.play_button);
        btnPause = (Button) findViewById(R.id.pause_button);
        btnStop = (Button) findViewById(R.id.stop_button);
        mSwitch = (Switch)findViewById(R.id.mSwitch);
        botSwitch = (Switch)findViewById(R.id.BoTSwitch);
        SHSpinner = findViewById(R.id.SHSpinner);
        KSpinner = findViewById(R.id.KSpinner);
        BPMSpinner = findViewById(R.id.BPMSpinner);
        reference.clone(this, R.layout.activity_main);
        KCheckBox = (CheckBox)findViewById(R.id.KMuteBox);
        SHCheckBox = (CheckBox)findViewById(R.id.SHMuteBox);

        // Initializing the itemLists for the Ternary and Binary Pockets, and the bpm selector
        SHitems = Arrays.asList(getResources().getStringArray(R.array.SH_Items));
        Kitems = Arrays.asList(getResources().getStringArray(R.array.K_Items));
        TSHitems = Arrays.asList(getResources().getStringArray(R.array.TSH_Items));
        TKitems = Arrays.asList(getResources().getStringArray(R.array.TK_Items));
        BPMitems = getResources().getStringArray(R.array.BPM_Items);


        // Metronome Switch Checker
        // true = Metronome playing every Quarter Note, False = metronome doesn't play
        mSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> isMetronomeActive = isChecked);

        // Mute Tracks Checker
        // true = track playing, false = track isn't playing
        KCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> isKMuted = isChecked);
        SHCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> isSHMuted = isChecked);

        // Binary/Ternary Switch
        // Ternary Mode = allows the user to select the Ternary Pockets (i.e swing, shuffle)
        // Binary Mode = allows the user to select the Binary Pockets (i.e regular 4/4 pockets)
        botSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                isTrinary = true;
                botSwitch.setText("Ternary");
                SHSpinner.setAdapter(TSHadapterItems);
                KSpinner.setAdapter(TKadapterItems);
                currentPocket = "00";
                currentKPocket = "0";
                currentSHPocket = "0";
                if(isPlaying){
                    isPlaying = false;
                    timerTask.cancel();
                    beat = 0;
                    barView.setVisibility(View.INVISIBLE);
                    barView.setImageResource(R.color.white);
                    mHandler.post(moveBar);
                }
            }
            else{
                isTrinary = false;
                botSwitch.setText("Binary");
                SHSpinner.setAdapter(SHadapterItems);
                KSpinner.setAdapter(KadapterItems);
                currentPocket = "00";
                currentKPocket = "0";
                currentSHPocket = "0";
                if(isPlaying){
                    isPlaying = false;
                    timerTask.cancel();
                    beat = 0;
                    barView.setVisibility(View.INVISIBLE);
                    barView.setImageResource(R.color.white);
                    mHandler.post(moveBar);
                }
            }
        });

        // Setting up the Snare / HiHat dropdown menu
        // When a pocket is selected, the pocket variable and on screen image will update
        // according to the other pocket selected (if selected)
        SHadapterItems = new SpinnerAdapter(this, SHitems, false);
        SHSpinner.setAdapter(SHadapterItems);
        SHSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSHPocket = SHadapterItems.getItem(position).toLowerCase();
                currentPocket = currentSHPocket + currentKPocket;
                String temp = "";
                if(isTrinary) temp = "t";
                if(currentKPocket.equals("") || currentKPocket.equals("0")){
                    temp +=currentSHPocket;
                    Resources res = getResources();
                    int resID = res.getIdentifier("beat_"+temp +"xl", "drawable", getPackageName());
                    pocketView.setImageResource(resID);
                } else if (currentSHPocket.equals("0")) {
                    temp += currentKPocket;
                    Resources res = getResources();
                    int resID = res.getIdentifier("beat_"+temp +"xl", "drawable", getPackageName());
                    pocketView.setImageResource(resID);
                } else if(!currentPocket.equals("00")){
                    try{
                        temp+=currentPocket;
                        Resources res = getResources();
                        int resID = res.getIdentifier(temp, "drawable", getPackageName());
                        pocketView.setImageResource(resID);
                    }
                    catch(Exception exc){
                        pocketView.setImageResource(R.drawable.empty);
                        Log.d("Debugging", "Pocket is not found / Exception: " + exc);

                    }
                }
                else{
                    pocketView.setImageResource(R.drawable.empty);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                pocketView.setImageResource(R.drawable.empty);
            }
        });

        // Setting up the Kick Drum dropdown menu
        // When a pocket is selected, the pocket variable and on screen image will update
        // according to the other pocket selected (if selected)
        KadapterItems = new SpinnerAdapter(this, Kitems, false);
        KSpinner.setAdapter(KadapterItems);
        KSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentKPocket = KadapterItems.getItem(position);
                currentPocket = currentSHPocket + currentKPocket;
                String temp = "";
                if(isTrinary) temp = "t";
                if(currentPocket.equals("00")){
                    pocketView.setImageResource(R.drawable.empty);
                } else if(currentSHPocket.equals("") || currentSHPocket.equals("0")){
                    Resources res = getResources();
                    temp +=currentKPocket;
                    int resID = res.getIdentifier("beat_"+temp +"xl", "drawable", getPackageName());
                    pocketView.setImageResource(resID);
                } else if (currentKPocket.equals("0")) {
                    temp += currentSHPocket;
                    Resources res = getResources();
                    int resID = res.getIdentifier("beat_" + temp + "xl", "drawable", getPackageName());
                    pocketView.setImageResource(resID);
                } else{
                    try{
                        Resources res = getResources();
                        temp+=currentPocket;
                        int resID = res.getIdentifier(temp, "drawable", getPackageName());
                        pocketView.setImageResource(resID);
                    }
                    catch(Exception exc){
                        pocketView.setImageResource(R.drawable.empty);
                        Log.d("Debugging", "Pocket is not found / Exception: " + exc);

                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                pocketView.setImageResource(R.drawable.empty);
            }
        });

        // Setting up the BPM dropdown menu
        // When an item is selected the bpm variable will be updated to the new value
        BPMAdapterItems= new ArrayAdapter<String>(this, R.layout.list_item, BPMitems);
        BPMSpinner.setAdapter(BPMAdapterItems);
        BPMSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                bpm = Integer.parseInt(BPMitems[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                pocketView.setImageResource(R.drawable.empty);
            }
        });

        // Initializing the Ternary SpinnerAdapters
        TSHadapterItems = new SpinnerAdapter(this, TSHitems, true);
        TKadapterItems = new SpinnerAdapter(this, TKitems, true);

        // Initializing the SoundPool
        SoundPool.Builder soundpbuilder = new SoundPool.Builder().setMaxStreams(16);
        soundpbuilder.setAudioAttributes(new AudioAttributes.Builder().setFlags(AudioAttributes.USAGE_GAME).build());
        soundp = soundpbuilder.build();

        // Loading all the sounds into the SoundPool
        bID = soundp.load(getApplicationContext(),R.raw.bd,1);
        sID = soundp.load(getApplicationContext(),R.raw.ss,1);
        sqID = soundp.load(getApplicationContext(),R.raw.ssquiet,1);
        hhID = soundp.load(getApplicationContext(),R.raw.hh,1);
        ohID = soundp.load(getApplicationContext(),R.raw.oh,1);
        clickID = soundp.load(getApplicationContext(),R.raw.click,1);

        // Check for loaded sounds, if so activate buttons
        soundp.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                btnPause.setEnabled(true);
                btnPlay.setEnabled(true);
                btnStop.setEnabled(true);
            }
        });
        // Play Beat Button Listener
        // Calls the PlayBeat() function and sets up the bar to be seen
        btnPlay.setOnClickListener(v -> {
            if (!isPlaying) {
                PlayBeat();
                isPlaying = true;
                barView.setImageResource(R.drawable.bar);
                barView.setVisibility(View.VISIBLE);
            }
        });

        // Stop Beat Button Listener
        // Disables the Play Beat TimerTask function and sets up the bar to be invisible again
        btnStop.setOnClickListener(v -> {
            if(isPlaying){

                isPlaying = false;
                timerTask.cancel();
                beat = 0;
                barView.setVisibility(View.INVISIBLE);
                barView.setImageResource(R.color.white);
            }
        });

        // Pause/Resume Beat Button Listener
        // Stops playing the pocket but keeps the bar visible / resumes the playing of the pocket
        btnPause.setOnClickListener(v -> {
            if(!pause) {
                pause = true;
                btnPause.setText("Resume");
            }
            else {
                pause = false;
                btnPause.setText("Pause");
            }
        });

    }
    // Sets Up the TimerTask that will play the next beat
    // according to the patterns the user has selected
    private void PlayBeat(){
        beat = 0;
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (!pause) {
                    int numBeats = 16;
                    String KcurrentNote;
                    String SHcurrentNote;

                    //saves the current beat in the pattern to play later
                    if(isTrinary){
                        numBeats = 12;
                        KcurrentNote = pocketList.get("T"+currentKPocket).substring((beat%numBeats)*2, (beat%numBeats)*2 + 2);
                        SHcurrentNote = pocketList.get("T"+currentSHPocket).substring((beat%numBeats)*2, (beat%numBeats)*2 + 2);
                    }
                    else{
                        KcurrentNote = pocketList.get(currentKPocket).substring((beat%numBeats)*2, (beat%numBeats)*2 + 2);
                        SHcurrentNote = pocketList.get(currentSHPocket).substring((beat%numBeats)*2, (beat%numBeats)*2 + 2);
                    }

                    if(beat%(numBeats/4)==0 && isMetronomeActive){
                        if(beat%numBeats==0) soundp.play(clickID,0.9f,0.9f,0,0,1f);
                        else soundp.play(clickID,0.5f,0.5f,0,0,1f);
                    }
                    if (Objects.equals(KcurrentNote, "KK") && !isKMuted) { //plays Kick Drum if it should according to Kick Drum patterns
                        soundp.play(bID, 1.3f, 1.3f, 0, 0, 1f);
                    }
                    if(!isSHMuted){
                        switch (SHcurrentNote) { //checks which sounds it needs to play out of the Snare/HiHat patterns
                            case "HH":
                                soundp.play(hhID, 1.0f, 1.0f, 0, 0, 1f);
                                break;
                            case "HS":
                                soundp.play(hhID, 1.0f, 1.0f, 0, 0, 1f);
                                soundp.play(sID, 1.0f, 1.0f, 0, 0, 1f);
                                break;
                            case "Hs":
                                soundp.play(hhID, 1.0f, 1.0f, 0, 0, 1f);
                                soundp.play(sqID, 0.7f, 0.7f, 0, 0, 1f);
                                break;
                            case "SS":
                                soundp.play(sID, 1.0f, 1.0f, 0, 0, 1f);
                                break;
                            case "ss":
                                soundp.play(sqID, 0.7f, 0.7f, 0, 0, 1f);
                                break;
                            case "OH":
                                soundp.play(ohID, 0.5f, 0.5f, 0, 0, 1f);
                                break;
                        }
                    }
                    mHandler.postDelayed(moveBar, 1); //animating the bar
                    beat++;
                }

            }
        };
        //schedules the task to every 16th note of the bpm selected
        if(isTrinary) timer.scheduleAtFixedRate(timerTask,0,20000/bpm);
        else timer.scheduleAtFixedRate(timerTask,0,15000/bpm);
    }

    // Initializes the pocketList with all the Binary and Ternary Pockets
    // and their associated String form. MM = no note to play, HH = play only HiHat,
    // HS = play both HiHat and Snare, SS = play only Snare, OH = play an Open Hat,
    // ss = play a Quiet Snare, Hs = play a Quiet Snare and a HiHat, KK = play a Kick Drum
    private void addPockets(HashMap<String, String> list){
        list.put("a", "HHMMHHMMHSMMHHMMHHMMHHMMHSMMHHMM");
        list.put("b", "HHHHHHHHHSHHHHHHHHHHHHHHHSHHHHHH");
        list.put("c", "HHMMMMMMHSMMMMMMHHMMMMMMHSMMMMMM");
        list.put("d", "MMMMHHMMSSMMHHMMMMMMHHMMSSMMHHMM");
        list.put("e", "HHHHHHHHSSHHHHHHHHHHHHHHSSHHHHHH");
        list.put("f", "HHHHHHMMHSHHHHMMHHHHHHMMHSHHHHMM");
        list.put("g", "HHMMHHHHHSMMHHHHHHMMHHHHHSMMHHHH");
        list.put("h", "HHssHHHHSSHHssssHHssHHHHSSHHssss");
        list.put("i", "HHssHHHHSSHHHHssHHssHHHHSSHHHHss");
        list.put("j", "HHssHsMMHSssHHssHHssHsMMHSssHHss");
        list.put("k", "HHssssMMHSssMMssHHssssMMHSssMMss");
        list.put("l", "HHssHsHHHSssHHHsHHssHsHHHSssHHHs");
        list.put("m", "HHMMOHMMHSMMOHMMHHMMOHMMHSMMOHMM");

        list.put("0", "MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM");

        list.put("1", "KKMMMMMMMMMMMMMMKKMMMMMMMMMMMMMM");
        list.put("2", "KKMMKKMMMMMMMMMMKKMMKKMMMMMMMMMM");
        list.put("3", "KKMMMMMMMMMMKKMMKKMMMMMMMMMMMMMM");
        list.put("4", "KKMMMMMMMMMMKKMMMMMMKKMMMMMMKKMM");
        list.put("5", "KKMMMMKKMMMMMMMMKKMMMMKKMMMMMMMM");
        list.put("6", "KKMMMMKKMMMMKKMMKKMMMMKKMMMMKKMM");
        list.put("7", "KKMMMMMMMMMMMMKKMMMMKKMMMMMMMMMM");
        list.put("8", "KKMMMMMMMMKKMMMMKKMMMMMMMMKKMMMM");
        list.put("9", "KKMMMMMMMMMMMMKKMMMMKKMMMMKKMMMM");
        list.put("10", "KKMMMMKKMMMMMMKKMMMMKKMMMMKKMMMM");
        list.put("11", "KKMMMMMMMMMMMMKKKKMMMMMMMMMMMMKK");
        list.put("12", "KKMMKKMMMMMMMMKKKKMMKKMMMMMMMMKK");
        list.put("13", "KKMMMMMMMMKKMMKKKKMMMMMMMMKKMMKK");
        list.put("14", "KKKKMMMMMMMMMMMMKKKKMMMMMMMMMMMM");
        list.put("15", "KKKKMMKKMMMMMMMMKKKKMMKKMMMMMMMM");
        list.put("16", "KKMMMMMMMMMMKKKKMMMMKKKKMMMMMMMM");
        list.put("17", "KKMMMMMMMMMMKKKKMMKKKKMMMMMMMMMM");
        list.put("18", "KKKKMMKKMMMMKKKKMMKKKKMMMMMMKKMM");

        list.put("T0", "MMMMMMMMMMMMMMMMMMMMMMMM");
        list.put("Ta", "HHHHHHHSHHHHHHHHHHHSHHHH");
        list.put("Tb", "HHHHHHSSHHHHHHHHHHSSHHHH");
        list.put("Tc", "HHMMHHHSMMHHHHMMHHHSMMHH");
        list.put("Td", "HHssHHHSssHHHHssHHHSssHH");
        list.put("Te", "HHssHHHHssHHSHssHHHHssHH");
        list.put("Tf", "HHMMMMHSMMHHHHMMMMHSMMHH");
        list.put("Tg", "HHMMMMHHMMHHHSMMMMHHMMHH");

        list.put("T1", "KKMMMMMMMMMMKKMMMMMMMMMM");
        list.put("T2", "KKMMMMMMMMKKKKMMMMMMMMMM");
        list.put("T3", "KKMMMMMMMMKKMMMMMMMMMMMM");
        list.put("T4", "KKMMMMMMMMKKMMMMKKMMMMKK");
        list.put("T5", "KKMMKKMMMMMMKKMMKKMMMMMM");
        list.put("T6", "KKMMKKMMMMKKKKMMKKMMMMKK");
        list.put("T7", "KKMMKKMMKKMMKKMMKKMMKKMM");
        list.put("T8", "MMMMKKMMMMKKMMMMKKMMMMKK");
        list.put("T9", "KKMMMMKKMMMMKKMMMMKKMMMM");
    }

    // Moves the Bar that follows the playing notes along the pocket,
    // will only move if the pocket is playing
    Runnable moveBar = () -> {
        try {
            ConstraintLayout constraintLayout = findViewById(R.id.parent);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(reference);
            if (isTrinary) constraintSet.connect(R.id.barView,ConstraintSet.LEFT,R.id.pocket_view,ConstraintSet.LEFT,pocketView.getMeasuredWidth()/13*(1+beat%12));
            else constraintSet.connect(R.id.barView,ConstraintSet.LEFT,R.id.pocket_view,ConstraintSet.LEFT,pocketView.getMeasuredWidth()/17*(1+beat%16));
            constraintSet.connect(R.id.barView,ConstraintSet.TOP,R.id.pocket_view,ConstraintSet.TOP,0);
            constraintSet.applyTo(constraintLayout);
        }
        catch (Exception exc){
            Log.d("Exception!", "exc is " + exc);
        }
    };

}


