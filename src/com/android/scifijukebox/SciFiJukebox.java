package com.android.scifijukebox;

import com.android.scifijukebox.MusicService.MusicBinder;

import android.app.Activity;
import android.os.Bundle;

import java.util.ArrayList;
import java.io.File;

import android.net.Uri;
import android.widget.MediaController.MediaPlayerControl;

import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.View;

import android.view.ViewGroup;

import android.util.Log;

/**
*  This class consists of main activity of the project. This class can be
*  considered the main class of the project. This class works directly with
*  MusicService class.
*  @see MusicService
*/
public class SciFiJukebox extends Activity
{
  // Musics
  private MusicHandler musicHandler;

  // Album
  private AlbumHandler albumHandler;

  // Attributes for handling the communication between Activity and Service
  private MusicService musicService;
  private Intent playIntent;
  private boolean musicBound = false;

  // Attributes to handling notifications
  private String songTitle = "";
  private static final int NOTIFY_ID = 1;
  private static final String SCIFI_JUKEBOX = "jukebox";

  // Attributes responsible for controlling music
  private boolean paused = false;
  private boolean playbackPaused = false;

  //Layouts
  private View albumLayout;
  private View musicLayout;

  @Override
  public void onCreate(Bundle pSavedInstanceState)
  {
    super.onCreate(pSavedInstanceState);
    this.albumHandler = new AlbumHandler(this);
    this.albumLayout = this.albumHandler.getAlbumViewLayout();

    this.musicHandler = new MusicHandler(this);
    this.musicLayout = this.musicHandler.getMusicViewLayout();

    setContentView(this.albumLayout);

    this.initDefaultDirectory();

    this.musicHandler.initMusicList();
    this.albumHandler.initAlbumList();

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
      this.paused = false;
    }
  }

  @Override
  protected void onStop()
  {
    super.onStop();
  }

  @Override
  protected void onStart()
  {
    super.onStart();
    if (this.playIntent == null)
    {
      this.playIntent = new Intent(this, MusicService.class);
      bindService(this.playIntent, this.musicConnection,
                  Context.BIND_AUTO_CREATE);
      startService(this.playIntent);
    }
  }

  // Connect activity to Service
  private ServiceConnection musicConnection = new ServiceConnection()
  {
    @Override
    public void onServiceConnected(ComponentName pName, IBinder pService)
    {
      MusicBinder binder = (MusicBinder)pService;
      //get service
      musicService = binder.getService();
      //pass list
      musicService.setList(musicHandler.getSongList());
      musicBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName pName)
    {
      musicBound = false;
    }
  };

  private void initDefaultDirectory()
  {
    boolean success = true;
    File folder = new File(UtilJukebox.getRootDirectory());

    if (!folder.exists())
    {
      success = folder.mkdir();
      Log.i(SCIFI_JUKEBOX, "Created default directory in: " +
            UtilJukebox.getRootDirectory());
    }
    else
    {
      Log.i(SCIFI_JUKEBOX, "Default directory already created");
    }
  }

  public void albumPicked(View pView)
  {
    int albumToGo = ((AlbumElementWrapper)pView.getTag()).getPosition();
    Album albumTarget = this.albumHandler.getAlbumById(albumToGo);
    Uri uri = Uri.parse(albumTarget.getTitle());
    String path = uri.getPath();
    String idString = path.substring(path.lastIndexOf('/') + 1);

    // Get a list of musics related with album
    this.musicHandler.getSongList(idString);
    this.setMusicLayout();
  }

  private void setMusicLayout()
  {
    this.musicLayout = this.musicHandler.updateMusicLayout();
    setContentView(this.musicLayout);
  }

  public void playSong(View pView)
  {
    int displayedChild = this.musicHandler.getCurrentMusic();
    this.musicService.setSong(displayedChild);
    if (this.isPlaying())
    {
      pause();
    }
    else
    {
      this.musicService.playSong();
    }
   // if (this.playbackPaused)
   // {
   //   this.playbackPaused = false;
   // }
  }

  public void goBackward(View pView)
  {
    this.musicHandler.goBackward(pView);
  }

  public void goForward(View pView)
  {
    this.musicHandler.goForward(pView);
  }

  /**
  *  Go to next music and play it
  */
  private void playNext()
  {
    this.musicService.playNext();
    if (this.playbackPaused)
    {
      this.playbackPaused = false;
    }
  }

  /**
  *  Go to previous music and play it
  */
  private void playPrev()
  {
    this.musicService.playPrevious();
    if (this.playbackPaused)
    {
      this.playbackPaused = false;
    }
  }

  protected void onDestroy()
  {
    this.stopService(this.playIntent);
    this.musicService = null;
    super.onDestroy();
  }

  public boolean canPause()
  {
    return true;
  }

  public boolean canSeekBackward()
  {
    return true;
  }

  public boolean canSeekForward()
  {
    return true;
  }

  public int getAudioSessionId()
  {
    return 0;
  }

  public int getBufferPercentage()
  {
    return 0;
  }

  /**
  *  Get current position of music, notice that is required to make some
  *  verifications before return this value
  *  @return Return a integer representing the current position of music
  */
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

  /**
  *  Get music duration
  */
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

  /**
  *  Verify if music is playing
  */
  public boolean isPlaying()
  {
    if (this.musicService != null && this.musicBound)
    {
      return this.musicService.isPlaying();
    }
    return false;
  }

  /**
  *  Pause music
  */
  public void pause()
  {
    this.playbackPaused = true;
    this.musicService.pausePlayer();
  }

  /**
  *  Get an id and seek this position in music
  *  @param pPosition integer presenting a position in a music
  */
  public void seekTo(int pPosition)
  {
    this.musicService.seek(pPosition);
  }

  /**
  *  Start playing music
  */
  public void start()
  {
    this.musicService.go();
  }
}
