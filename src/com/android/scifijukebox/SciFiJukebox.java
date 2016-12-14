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

import android.util.Log;

public class SciFiJukebox extends Activity implements MediaPlayerControl
{
  private ArrayList<Song> songList;
  private ListView songView;

  private MusicService musicService;
  private Intent playIntent;
  private boolean musicBound = false;
  private MusicController controller;

  private String songTitle = "";
  private static final int NOTIFY_ID = 1;
  private static final String SCIFI_JUKEBOX = "jukebox";

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
    Log.i(SCIFI_JUKEBOX, "Activity initialized successfully");
  }

  @Override
  protected void onPause()
  {
    super.onPause();
    this.paused = true;
  }

  @Override
  protected void onResume()
  {
    super.onResume();
    if (this.paused)
    {
      setController();
      this.paused = false;
    }
  }

  @Override
  protected void onStop()
  {
    this.controller.hide();
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
      musicService = binder.getService();
      //pass list
      musicService.setList(songList);
      musicBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName pName)
    {
      musicBound = false;
    }
  };

  @Override
  public boolean onCreateOptionsMenu(Menu pMenu)
  {
    getMenuInflater().inflate(R.menu.main, pMenu);
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
      String title = android.provider.MediaStore.Audio.Media.TITLE;
      String id = android.provider.MediaStore.Audio.Media._ID;
      String artist = android.provider.MediaStore.Audio.Media.ARTIST;

      int titleColumn = musicCursor.getColumnIndex(title);
      int idColumn = musicCursor.getColumnIndex(id);
      int artistColumn = musicCursor.getColumnIndex(artist);
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
    this.controller = new MusicController(this);

    View.OnClickListener nextMusic = new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        playNext();
      }
    };
    View.OnClickListener previousMusic = new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        playPrev();
      }
    };

    this.controller.setPrevNextListeners(nextMusic, previousMusic);
    this.controller.setMediaPlayer(this);
    this.controller.setAnchorView(findViewById(R.id.song_list));
    this.controller.setEnabled(true);
  }

  @Override
  protected void onStart()
  {
    super.onStart();
    if (this.playIntent == null)
    {
      this.playIntent = new Intent(this, MusicService.class);
      bindService(this.playIntent, musicConnection, Context.BIND_AUTO_CREATE);
      startService(this.playIntent);
    }
  }

  public void songPicked(View pView)
  {
    this.musicService.setSong(Integer.parseInt(pView.getTag().toString()));
    this.musicService.playSong();
    if (this.playbackPaused)
    {
      setController();
      this.playbackPaused = false;
    }
    this.controller.show(0);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem pItem)
  {
    //menu item selected
    switch (pItem.getItemId())
    {
      case R.id.action_shuffle:
        this.musicService.setShuffle();
        break;
      case R.id.action_end:
        stopService(this.playIntent);
        this.musicService = null;
        System.exit(0);
        break;
    }
    return super.onOptionsItemSelected(pItem);
  }

  private void playNext()
  {
    this.musicService.playNext();
    if (this.playbackPaused)
    {
      setController();
      this.playbackPaused = false;
    }
    this.controller.show(0);
  }

  private void playPrev()
  {
    this.musicService.playPrevious();
    if (playbackPaused)
    {
      setController();
      this.playbackPaused = false;
    }
    this.controller.show(0);
  }

  @Override
  protected void onDestroy()
  {
    stopService(playIntent);
    this.musicService = null;
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
    if (this.musicService != null && this.musicBound
        && this.musicService.isPlaying())
    {
      return this.musicService.getPosition();
    }
    else
    {
      return 0;
    }
  }

  @Override
  public int getDuration()
  {
    if (this.musicService != null && this.musicBound
        && this.musicService.isPlaying())
    {
      return this.musicService.getDuration();
    }
    else
    {
      return 0;
    }
  }

  @Override
  public boolean isPlaying()
  {
    if (this.musicService!=null && this.musicBound)
    {
      return this.musicService.isPlaying();
    }
    return false;
  }

  @Override
  public void pause()
  {
    this.playbackPaused = true;
    this.musicService.pausePlayer();
  }

  @Override
  public void seekTo(int pPosition)
  {
    this.musicService.seek(pPosition);
  }

  @Override
  public void start()
  {
    this.musicService.go();
  }
}
