package Resources;

import com.puchoInc.diya.R;
import com.puchoInc.diya.TextSpeakData.SpeakData;

public class listenData {
    public static SpeakData[] getSpeakDatas(){
        return SNAPBACKS;
    }

    public static final SpeakData Data1 = new SpeakData(R.drawable.audio1,
            9377376);
    public static final SpeakData Data2 = new SpeakData(R.drawable.audio2,
            9377377);
    public static final SpeakData Data3 = new SpeakData( R.drawable.audio3,
            9377378);
    public static final SpeakData Data4 = new SpeakData( R.drawable.audio4,
            9377379);
    public static final SpeakData Data5 = new SpeakData( R.drawable.audio5,
            9377380);
    public static final SpeakData Data6 = new SpeakData( R.drawable.audio6,9377381);

    public static final SpeakData[] SNAPBACKS = {Data1, Data2, Data3, Data4, Data5, Data6};

}
