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
import android.widget.MediaController.MediaPlayerControl;

import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;

import android.view.Menu;

public class SciFiJukebox extends Activity implements MediaPlayerControl
{
    private ArrayList<Song> songList;
    private ListView songView;

    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;
    private MusicController controller;

    private String songTitle = "";
    private static final int NOTIFY_ID = 1;

    private boolean paused = false;
    private boolean playbackPaused = false;

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
      setController();
    }

    @Override
    protected void onPause()
    {
      super.onPause();
      paused = true;
    }

    @Override
    protected void onResume()
    {
      super.onResume();
      if (paused)
      {
        setController();
        paused = false;
      }
    }

    @Override
    protected void onStop()
    {
      controller.hide();
      super.onStop();
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

  private void setController()
  {
    controller = new MusicController(this);

    //XXX: THIS IS TERRIBLE! FIX IT
    controller.setPrevNextListeners(new View.OnClickListener()
      {
        @Override
        public void onClick(View v)
        {
          playNext();
        }
      }, new View.OnClickListener()
         {
          @Override
          public void onClick(View v)
          {
            playPrev();
          }
       });
    controller.setMediaPlayer(this);
    controller.setAnchorView(findViewById(R.id.song_list));
    controller.setEnabled(true);
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
    if (playbackPaused)
    {
      setController();
      playbackPaused = false;
    }
    controller.show(0);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    //menu item selected
    switch (item.getItemId())
    {
      case R.id.action_shuffle:
        musicSrv.setShuffle();
        break;
      case R.id.action_end:
        stopService(playIntent);
        musicSrv=null;
        System.exit(0);
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  private void playNext()
  {
    musicSrv.playNext();
    if (playbackPaused)
    {
      setController();
      playbackPaused = false;
    }
    controller.show(0);
  }

  private void playPrev()
  {
    musicSrv.playPrev();
    if (playbackPaused)
    {
      setController();
      playbackPaused = false;
    }
    controller.show(0);
  }

  @Override
  protected void onDestroy()
  {
    stopService(playIntent);
    musicSrv=null;
    super.onDestroy();
  }

  @Override
  public boolean canPause()
  {
    return true;
  }

  @Override
  public boolean canSeekBackward()
  {
    return true;
  }

  @Override
  public boolean canSeekForward()
  {
    return true;
  }

    @Override
  public int getAudioSessionId()
  {
    return 0;
  }

  @Override
  public int getBufferPercentage()
  {
    return 0;
  }

  @Override
  public int getCurrentPosition()
  {
    if (musicSrv != null && musicBound && musicSrv.isPng())
    {
      return musicSrv.getPosn();
    }
    else
    {
      return 0;
    }
  }

  @Override
  public int getDuration()
  {
    if (musicSrv != null && musicBound && musicSrv.isPng())
    {
      return musicSrv.getDur();
    }
    else
    {
      return 0;
    }
  }

  @Override
  public boolean isPlaying()
  {
    if(musicSrv!=null && musicBound)
      return musicSrv.isPng();
    return false;
  }

  @Override
  public void pause()
  {
    playbackPaused = true;
    musicSrv.pausePlayer();
  }

  @Override
  public void seekTo(int pos)
  {
    musicSrv.seek(pos);
  }

  @Override
  public void start()
  {
    musicSrv.go();
  }
}
