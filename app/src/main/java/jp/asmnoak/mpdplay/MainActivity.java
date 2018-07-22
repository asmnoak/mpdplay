package jp.asmnoak.mpdplay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static String command ;                   // current mpd command
    public static String rcvdata;                    // received data
    public static String rcvdata1;                    // alternate received data
    public static String currsong;                   // info of current song
    public static  List<Item> lastlist;              // saved last list of listview
    public static  List<Item> lastlist_al;           // saved last list(album) of listview
    public static ArrayList<MusicItem> musiclist;    // checked music data which is candidate for playlist
    public static Integer listdmode = 0;             // display mode of listview. 0:all, 1:artist list, 2:album list, 3:an album
    public static Integer playing = 0;               // if 1 , playing music now
    public static int mpdport = 6600;               // mpd server
    public static String ipaddr = "192.168.0.3";  // mpd server

    /**
     *   item of listview
     */
    public class Item {
        boolean checked;
        Drawable ItemDrawable;
        String ItemString;
        Item(Drawable drawable, String t, boolean b){
            ItemDrawable = drawable;
            ItemString = t;
            checked = b;
        }
        void setChecked(boolean checked) {
            this.checked = checked;
        }
        public boolean isChecked(){
            return checked;
        }
    }

    static class ViewHolder {
        CheckBox checkBox;
        ImageView icon;
        TextView text;
    }

    /**
     *  Custom ItemsListAdapter
     */
    public class ItemsListAdapter extends BaseAdapter {

        private Context context;
        private List<Item> list;
        private LayoutInflater inflater;   // field

        ItemsListAdapter(Context c, List<Item> l) {
            context = c;
            list = l;
        }
        public void setChecked(int position, boolean b) {
            list.get(position).checked = b;
        }
        public void setList(List<Item> list){
            this.list = list;
        }
        public List<Item> getList(){
            return this.list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public boolean isChecked(int position) {
            return list.get(position).checked;
        }

        @Override
        public View getView(final int position, final View convertView, ViewGroup parent) {
            View rowView = convertView;

            // reuse views
            ViewHolder viewHolder = new ViewHolder();
            if (rowView == null) {
                inflater = ((Activity) context).getLayoutInflater();
                rowView = inflater.inflate(R.layout.row, null);

                viewHolder.checkBox = (CheckBox) rowView.findViewById(R.id.rowCheckBox);
                viewHolder.icon = (ImageView) rowView.findViewById(R.id.rowImageView);
                viewHolder.text = (TextView) rowView.findViewById(R.id.rowTextView);
                rowView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) rowView.getTag();
            }

            viewHolder.icon.setImageDrawable(list.get(position).ItemDrawable);
            viewHolder.checkBox.setChecked(list.get(position).checked);

            final String itemStr = list.get(position).ItemString;
            viewHolder.text.setText(itemStr);

            viewHolder.checkBox.setTag(position);

            viewHolder.text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ////
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            ArrayList<MusicItem> ml = new ArrayList<>();
                            String[] sar = itemStr.split(":");
                            if (listdmode==2) {
                                String str = sar[1];
                               for (MusicItem m : musiclist) {
                                    if (str.equals(m.album)) {
                                        ml.add(m);
                                    }
                                }
                            } else if (listdmode==1) {
                                String str = sar[0];
                                for (MusicItem m : musiclist) {
                                    if (str.equals(m.artist)) {
                                        ml.add(m);
                                    }
                                }
                            }
                            if (listdmode==2 || listdmode==1) {
                                listdmode = 3;
                                initItems(ml);
                                lastlist_al = items;  //save
                                myItemsListAdapter.setList(items);
                                myItemsListAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                    Toast.makeText(getApplicationContext(),
                            itemStr + "：アルバム",
                            Toast.LENGTH_LONG).show();
                }
            });

            viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean newState = !list.get(position).isChecked();
                    list.get(position).checked = newState;
                    // save
                    lastlist = list;
                     Toast.makeText(getApplicationContext(),
                            itemStr + "：チェック",
                            Toast.LENGTH_LONG).show();
                }
            });

            viewHolder.checkBox.setChecked(isChecked(position));

            return rowView;
        }
    }

    Button btnLookup;
    TextView text;
    ImageButton pauseButton;
    ImageButton playButton;
    List<Item> items;
    ListView listView;
    ItemsListAdapter myItemsListAdapter;
    private Handler handler = new Handler();
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main,menu);
        return true;

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ArrayList<MusicItem> ml;
        MusicItem mi=null;
        switch (item.getItemId()) {
            case R.id.top:
                listdmode = 0;
                initItems(musiclist);
                if (lastlist!=null) {
                    if (items.size() == lastlist.size()) {
                        for (int i = 0; i < items.size(); i++) {
                            items.get(i).setChecked(lastlist.get(i).checked);
                        }
                    }
                }
                myItemsListAdapter.setList(items);;
                myItemsListAdapter.notifyDataSetChanged();
                return true;
            case R.id.server:
                Intent intent = new Intent(this,Server.class);
                startActivity(intent);
                return  true;
            case R.id.album:
                listdmode = 2;
                ml = new ArrayList<>();
                for (MusicItem m:musiclist) {
                    if(mi==null || !mi.album.equals(m.album)) {
                        ml.add(m);
                        mi=m;
                    }
                }
                initItems(ml);
                myItemsListAdapter.setList(items);;
                myItemsListAdapter.notifyDataSetChanged();
                return  true;
            case R.id.artist:
                listdmode = 1;
                ml = new ArrayList<>();
                for (MusicItem m:musiclist) {
                    MusicItem mii = null;
                    if(mi==null) {
                        ml.add(m);
                        mi=m;
                    } else {
                        int i;
                        for (i=0;i<ml.size();i++) {
                            if(ml.get(i).artist.equals(m.artist)) {
                                break;
                            };
                        }
                        if (i>=ml.size()) {
                            ml.add(m);
                        }
                    }
                }
                initItems(ml);
                myItemsListAdapter.setList(items);;
                myItemsListAdapter.notifyDataSetChanged();
                return  true;
        }
        return  super.onOptionsItemSelected(item);
    }

    /**
     * Main onCreate
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        text = (TextView)findViewById(R.id.text);
        listView = (ListView)findViewById(R.id.listview);
        btnLookup = (Button)findViewById(R.id.lookup);
        pauseButton = (ImageButton)findViewById(R.id.pauseButton);
        playButton = (ImageButton)findViewById(R.id.playButton);
        SharedPreferences pref = getSharedPreferences("user_data", MODE_PRIVATE);
        if (pref!=null) {
            ipaddr = pref.getString("ipaddr","192.168.0.3");
            mpdport = pref.getInt("port",6600);
        }
        doCommand("search filename mp3",0);
        if (rcvdata==null || rcvdata.equals("") ) {
            Toast.makeText(MainActivity.this,
                    "サーバーとの通信に失敗しています",
                    Toast.LENGTH_LONG).show();
            musiclist = new ArrayList<>();
            MusicItem mi = new MusicItem("nofile.mp3","noname","noname","noname",0,0);
            musiclist.add(mi);
            initItems(musiclist);
        } else {
            initItems(initMusicList(rcvdata));
        }
        myItemsListAdapter = new ItemsListAdapter(this, items);
        listView.setAdapter(myItemsListAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(MainActivity.this,
                        ((Item)(parent.getItemAtPosition(position))).ItemString,
                        Toast.LENGTH_LONG).show();
        }});
        pauseButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                playing = 0;
                doCommand("pause",0);
                doCommand("stop",0);

                for (int i=0; i<items.size(); i++){
                    myItemsListAdapter.setChecked(i,false);
                }
                lastlist = myItemsListAdapter.getList();
                myItemsListAdapter.notifyDataSetChanged();
            }
        });
        playButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String str;
                String[] sar;
                doCommand("clear",0);
                for (int i=0; i<items.size(); i++){
                    if (items.get(i).isChecked()){
                        str = items.get(i).ItemString;
                        sar = str.split(":");
                        doCommand("findadd title " + sar[2],0);
                    }
                }
                doCommand("play",0);
                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        playing = 1;    // indicate now playing
                        int delay = 0;  // for reply delay
                        while (true) {
                            doCommand("currentsong",1);
                            // check receive and pause
                            while ((rcvdata1==null || rcvdata1.equals("")) && playing==1) {
                                if (rcvdata1!=null) {
                                    if (delay == 1 && rcvdata1.equals(""))
                                        break;
                                }
                                doCommand("currentsong",1); // one more
                                try {
                                   Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            if(rcvdata1.split("file:")==null || rcvdata1.equals("") || playing==0) {
                                // no playing song info
                                playing = 0;
                            } else {
                                String[] ml = rcvdata1.split("file:");
                                String[] ml2 = ml[1].split(";");
                                currsong = ml2[0];
                                String[] ml3;
                                String art;
                                String al;
                                for (int j=0;j<ml2.length;j++){
                                    if (ml2[j].contains("Artist:")) {
                                        ml3 = ml2[j].split(":");
                                        art = ml3[1];
                                    } else if (ml2[j].contains("Album:")) {
                                        ml3 = ml2[j].split(":");
                                        al = ml3[1];
                                    } else if (ml2[j].contains("Title:")) {
                                        ml3 = ml2[j].split(":");
                                        currsong = ml3[1];
                                    }
                                }

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        text.setText("タイトル：" + currsong);
                                    }
                                });
                            }
                            try {
                                Thread.sleep(4000);
                                delay = 1;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (playing==0) break;
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                text.setText("Music List" );
                            }
                        });
                    }
                });
                thread.start();
            }
        });

        btnLookup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str = "Check items:\n";

                for (int i=0; i<items.size(); i++){
                    if (items.get(i).isChecked()){
                        str += i + "\n";
                    }
                }

                 Toast.makeText(MainActivity.this,
                        str,
                        Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override
    protected void onResume(){
        super.onResume();
        if (listdmode==3 && lastlist_al!=null ) { // last is album mode
            items=lastlist_al;
        } else {
            //List<Item> lastlist = myItemsListAdapter.getList();
            doCommand("search filename mp3", 0);
            if (rcvdata == null || rcvdata.equals("")) {
                Toast.makeText(MainActivity.this,
                        "サーバーとの通信に失敗しています",
                        Toast.LENGTH_LONG).show();
                musiclist = new ArrayList<>();
                MusicItem mi = new MusicItem("nofile.mp3", "noname", "noname", "noname", 0, 0);
                musiclist.add(mi);
                initItems(musiclist);
            } else {
                initItems(initMusicList(rcvdata));
            }
            if (lastlist != null) {
                if (items.size() == lastlist.size()) {
                    for (int i = 0; i < items.size(); i++) {
                        items.get(i).setChecked(lastlist.get(i).checked);
                    }
                }
            }
            listdmode = 0;
        }
        myItemsListAdapter.setList(items);;
        myItemsListAdapter.notifyDataSetChanged();
    }

    /**
     *  make Misic list
     * @param rcvdata : mp3 song list on MPD server
     * @return  List of Music item
     */
    private ArrayList<MusicItem> initMusicList(String rcvdata) {
        String fn = "";
        String art = "";
        String tl = "";
        String al  = "";
        Integer tr = 0;
        Integer t = 0;
        String[] ml = rcvdata.split("file:");
        musiclist = new ArrayList<>();
        for (int i=1;i<ml.length;i++) {
            String[] ml2 = ml[i].split(";");
            fn = ml2[0];
            String[] ml3;
            for (int j=0;j<ml2.length;j++){
                if (ml2[j].contains("Artist:")) {
                    ml3 = ml2[j].split(":");
                    art = ml3[1];
                } else if (ml2[j].contains("Album:")) {
                    ml3 = ml2[j].split(":");
                    al = ml3[1];
                } else if (ml2[j].contains("Title:")) {
                    ml3 = ml2[j].split(":");
                    tl = ml3[1];

                }
            }
            MusicItem mi = new MusicItem(fn,art,al,tl,t,tr);
            musiclist.add(mi);
        }
        return musiclist;
    }

    /**
     * send MPD command
     * @param cm command
     * @param rsw received data buf switch
     */
   synchronized  private void doCommand(String cm , int rsw){
        ComThread th = new ComThread(rsw);
        command = cm;
        if (rsw==0) {
            rcvdata = null;
        } else {
            rcvdata1 = null;
        }
        th.start();
        if (rsw==0) {
            if (rcvdata == null) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (rcvdata1 == null) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /**
     * To construct items(input for myItemsListAdapter) of musiclist.
     *
     * @param musiclist   music info
     *
     */
    private void initItems(ArrayList<MusicItem> musiclist){
        items = new ArrayList<Item>();

        for(int i=0; i<musiclist.size(); i++){
            Drawable drw;
            drw = getResources().getDrawable(R.drawable.icon_onpu_64);
            String s = musiclist.get(i).artist + ":" +musiclist.get(i).album + ":" + musiclist.get(i).title;
            boolean b = false;
            Item item = new Item(drw, s, b);
            items.add(item);
        }
    }
}

