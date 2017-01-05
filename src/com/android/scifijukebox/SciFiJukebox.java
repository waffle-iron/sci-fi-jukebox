package com.android.scifijukebox;

import com.android.scifijukebox.MusicService.MusicBinder;

import android.app.Activity;
import android.os.Bundle;

import java.util.ArrayList;
import java.io.File;

import android.net.Uri;

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

  // Attributes to handling notifications
  private String songTitle = "";
  private static final int NOTIFY_ID = 1;
  private static final String SCIFI_JUKEBOX = "jukebox";

  // Attributes responsible for controlling music
  private boolean paused = false;

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

 @Override
  protected void onDestroy()
  {
    this.stopService(this.playIntent);
    this.musicService = null;
    super.onDestroy();
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
      musicHandler.setMusicBound(true);
      musicHandler.setMusicService(musicService);
    }

    @Override
    public void onServiceDisconnected(ComponentName pName)
    {
      musicHandler.setMusicBound(false);
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

  public void goHome(View pView)
  {
    this.setAlbumLayout();
  }

  private void setMusicLayout()
  {
    this.musicLayout = this.musicHandler.updateMusicLayout();
    setContentView(this.musicLayout);
  }

  private void setAlbumLayout()
  {
    this.musicHandler.resetPlayer();
    setContentView(this.albumLayout);
  }

  public void playSong(View pView)
  {
    this.musicHandler.playPauseMusic();
  }

  public void goBackward(View pView)
  {
    this.musicHandler.goBackward(pView);
  }

  public void goForward(View pView)
  {
    this.musicHandler.goForward(pView);
  }
}
