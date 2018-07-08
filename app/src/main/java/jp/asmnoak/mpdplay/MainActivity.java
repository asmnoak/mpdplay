package jp.asmnoak.mpdplay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
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
    public static String command ;
    public static String rcvdata;
    public static String currsong;
    public static ArrayList<MusicItem> musiclist;
    public static Integer playing = 0;
    public static int mpdport = 6600;
    public static String ipaddr = "192.168.0.4";

    public class Item {
        boolean checked;
        Drawable ItemDrawable;
        String ItemString;
        Item(Drawable drawable, String t, boolean b){
            ItemDrawable = drawable;
            ItemString = t;
            checked = b;
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

    public class ItemsListAdapter extends BaseAdapter {

        private Context context;
        private List<Item> list;
        private LayoutInflater inflater;   ////

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
                //LayoutInflater inflater = ((Activity) context).getLayoutInflater();
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
            //if(position==1){   ////
                // 背景色を変える
             //   rowView.setBackgroundColor(Color.rgb(255, 220, 127));
            //}

            final String itemStr = list.get(position).ItemString;
            viewHolder.text.setText(itemStr);

            viewHolder.checkBox.setTag(position);

            /*
            viewHolder.checkBox.setOnCheckedChangeListener(
                    new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    list.get(position).checked = b;

                    Toast.makeText(getApplicationContext(),
                            itemStr + "onCheckedChanged\nchecked: " + b,
                            Toast.LENGTH_LONG).show();
                }
            });
            */

            viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean newState = !list.get(position).isChecked();
                    list.get(position).checked = newState;
                    ////
                     Toast.makeText(getApplicationContext(),
                            itemStr + "setOnClickListener\nchecked: " + newState,
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
        switch (item.getItemId()) {
            case R.id.server:
                Intent intent = new Intent(this,Server.class);
                startActivity(intent);
                return  true;
            case R.id.album:
                return  true;
            case R.id.artist:
                ArrayList<MusicItem> ml = new ArrayList<>();
                MusicItem mi=null;
                for (MusicItem m:musiclist) {
                    if(mi==null || !mi.artist.equals(m.artist)) {
                        ml.add(m);
                        mi=m;
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
        setContentView(R.layout.activity_main);
        text = (TextView)findViewById(R.id.text);
        listView = (ListView)findViewById(R.id.listview);
        btnLookup = (Button)findViewById(R.id.lookup);
        pauseButton = (ImageButton)findViewById(R.id.pauseButton);
        playButton = (ImageButton)findViewById(R.id.playButton);
        doCommand("search filename mp3");
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
                doCommand("pause");
                doCommand("stop");

                for (int i=0; i<items.size(); i++){
                    myItemsListAdapter.setChecked(i,false);
                }
                myItemsListAdapter.notifyDataSetChanged();
                //View rowview = myItemsListAdapter.getView(0,null,null);
                //ViewHolder viewHolder = new ViewHolder();
                //viewHolder.checkBox = (CheckBox) rowview.findViewById(R.id.rowCheckBox);
                //viewHolder.checkBox.setChecked(false);
            }
        });
        playButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String str;
                String[] sar;
                doCommand("clear");
                for (int i=0; i<items.size(); i++){
                    if (items.get(i).isChecked()){
                        str = items.get(i).ItemString;
                        sar = str.split(":");
                        doCommand("findadd title " + sar[2]);
                    }
                }
                doCommand("play");
                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        playing = 1;
                        while (true) {
                            doCommand("currentsong");
                            if (rcvdata==null || rcvdata.equals("")) {
                                // no playing song
                                playing = 0;
                            } else {
                                String[] ml = rcvdata.split("file:");
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
                                Thread.sleep(2000);
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

                /*
                int cnt = myItemsListAdapter.getCount();
                for (int i=0; i<cnt; i++){
                    if(myItemsListAdapter.isChecked(i)){
                        str += i + "\n";
                    }
                }
                */

                Toast.makeText(MainActivity.this,
                        str,
                        Toast.LENGTH_LONG).show();

            }
        });
    }

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

   synchronized  private void doCommand(String cm){
        ComThread th = new ComThread();
        command = cm;
        rcvdata = null;
        th.start();
        if (rcvdata==null) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
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

        //TypedArray arrayDrawable = getResources().obtainTypedArray(R.array.resicon);
        //TypedArray arrayText = getResources().obtainTypedArray(R.array.restext);

        for(int i=0; i<musiclist.size(); i++){
            Drawable drw;
            drw = getResources().getDrawable(R.drawable.icon_onpu_64);
            String s = musiclist.get(i).artist + ":" +musiclist.get(i).album + ":" + musiclist.get(i).title;
            boolean b = false;
            Item item = new Item(drw, s, b);
            items.add(item);
        }

        //arrayDrawable.recycle();
        //arrayText.recycle();
    }
}

