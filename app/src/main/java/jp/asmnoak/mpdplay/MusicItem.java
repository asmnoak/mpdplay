package jp.asmnoak.mpdplay;

public class MusicItem {
    String filename;
    String artist;
    String title;
    String album;
    Integer time;
    Integer track;
    MusicItem() {
        super();
    }
    MusicItem(String filename, String artist, String album, String title, Integer time, Integer track){
        super();
        this.filename=filename;
        this.artist = artist;
        this.album = album;
        this.title = title;
        this.time = time;
        this.track = track;

    }
}
