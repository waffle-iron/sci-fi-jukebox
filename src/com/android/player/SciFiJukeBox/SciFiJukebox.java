package com.android.player.SciFiJukeBox;

import android.app.Activity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.ListView;

import android.view.Menu;

public class SciFiJukebox extends Activity
{
    private ArrayList<Song> songList;
    private ListView songView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);

      this.songView = (ListView)findViewById(R.id.song_list);
      this.songList = new ArrayList<Song>();

      getSongList();
      Collections.sort(this.songList, new Comparator<Song>()
        {
          public int compare(Song a, Song b)
          {
            return a.getTitle().compareTo(b.getTitle());
          }
        });

      SongAdapter songAdt = new SongAdapter(this, this.songList);
      songView.setAdapter(songAdt);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
      getMenuInflater().inflate(R.menu.main, menu);
      return true;
    }

    public void getSongList()
    {
      //Retrieve song info
      ContentResolver musicResolver = getContentResolver();
      Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
      Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

      if (musicCursor != null && musicCursor.moveToFirst())
      {
        int titleColumn = musicCursor.getColumnIndex(
              android.provider.MediaStore.Audio.Media.TITLE);
        int idColumn = musicCursor.getColumnIndex(
              android.provider.MediaStore.Audio.Media._ID);
        int artistColumn = musicCursor.getColumnIndex(
              android.provider.MediaStore.Audio.Media.ARTIST);
        //Add songs to list
        do
        {
          long thisId = musicCursor.getLong(idColumn);
          String thisTitle = musicCursor.getString(titleColumn);
          String thisArtist = musicCursor.getString(artistColumn);
          this.songList.add(new Song(thisId, thisTitle, thisArtist));
        } while(musicCursor.moveToNext());
      }
    }
}
