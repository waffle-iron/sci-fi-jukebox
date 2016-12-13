package com.android.player.SciFiJukeBox;

import java.util.ArrayList;
import java.util.Random;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;


public class MusicService extends Service
  implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener
{
  //media player
  private MediaPlayer player;
  //song list
  private ArrayList<Song> songs;
  //current position
  private int songPosn;
  //title of current song
  private String songTitle="";

  private final IBinder musicBind = new MusicBinder();

  public void onCreate()
  {
    super.onCreate();
    songPosn = 0;
    player = new MediaPlayer();
    initMusicPlayer();
  }

  public void initMusicPlayer()
  {
    player.setWakeMode(getApplicationContext(),
                        PowerManager.PARTIAL_WAKE_LOCK);
    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
    player.setOnPreparedListener(this);
    player.setOnCompletionListener(this);
    player.setOnErrorListener(this);
  }

  public void setList(ArrayList<Song> theSongs)
  {
    songs=theSongs;
  }

  public class MusicBinder extends Binder
  {
    MusicService getService()
    {
      return MusicService.this;
    }
  }

  @Override
  public IBinder onBind(Intent arg0)
  {
      return musicBind;
  }

  @Override
  public boolean onUnbind(Intent intent)
  {
    player.stop();
    player.release();
    return false;
  }

  public void playSong()
  {
    player.reset();
    //get song
    Song playSong = songs.get(songPosn);
    songTitle = playSong.getTitle();
    //get id
    long currSong = playSong.getID();
    //set uri
    Uri trackUri = ContentUris.withAppendedId(
      android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
      currSong);

    try
    {
      player.setDataSource(getApplicationContext(), trackUri);
    }
    catch(Exception e)
    {
      Log.e("MUSIC SERVICE", "Error setting data source", e);
    }

    player.prepareAsync();
  }

  @Override
  public void onPrepared(MediaPlayer mp)
  {
    //start playback
    mp.start();
  }

  public void setSong(int songIndex)
  {
    songPosn = songIndex;
  }

  @Override
  public boolean onError(MediaPlayer mp, int what, int extra)
  {
    Log.v("MUSIC PLAYER", "Playback Error");
    mp.reset();
    return false;
  }

  @Override
  public void onCompletion(MediaPlayer mp)
  {
  }

}
