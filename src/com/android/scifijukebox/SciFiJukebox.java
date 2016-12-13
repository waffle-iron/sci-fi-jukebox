package com.android.scifijukebox;

import com.android.scifijukebox.MusicService.MusicBinder;

import android.app.Activity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.ListView;

import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;

import android.view.Menu;

public class SciFiJukebox extends Activity
{
    private ArrayList<Song> songList;
    private ListView songView;

    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;

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

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection()
    { 
      @Override
      public void onServiceConnected(ComponentName name, IBinder service)
      {
        MusicBinder binder = (MusicBinder)service;
        //get service
        musicSrv = binder.getService();
        //pass list
        musicSrv.setList(songList);
        musicBound = true;
      }
     
      @Override
      public void onServiceDisconnected(ComponentName name)
      {
        musicBound = false;
      }
    };

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

  @Override
  protected void onStart()
  {
    super.onStart();
    if(playIntent == null)
    {
      playIntent = new Intent(this, MusicService.class);
      bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
      startService(playIntent);
    }
  }

  public void songPicked(View view)
  {
    musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
    musicSrv.playSong();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    //menu item selected
    switch (item.getItemId())
    {
      case R.id.action_shuffle:
        //shuffle
        break;
      case R.id.action_end:
        stopService(playIntent);
        musicSrv=null;
        System.exit(0);
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onDestroy()
  {
    stopService(playIntent);
    musicSrv=null;
    super.onDestroy();
  }
}
