package com.android.scifijukebox;

import java.util.ArrayList;

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

/**
*  This class is a local service responsible for handling music elements.
*/
public class MusicService extends Service
                          implements MediaPlayer.OnPreparedListener,
                                     MediaPlayer.OnErrorListener,
                                     MediaPlayer.OnCompletionListener
{
  // Play music
  private MediaPlayer player;
  private ArrayList<Song> songList;
  private int songPosition;
  private String songTitle = "";

  private static final int NOTIFY_ID = 1;

  private final IBinder musicBind = new MusicBinder();

  private static final String SCIFI_JUKEBOX_SERVICE = "jukebox-service";

  // Service methods
  @Override
  public void onCreate()
  {
    super.onCreate();
    this.songPosition = 0;
    this.player = new MediaPlayer();
    this.initMusicPlayer();
  }

  @Override
  public void onDestroy()
  {
    stopForeground(true);
    Log.i(SCIFI_JUKEBOX_SERVICE, "Stopped jukebox service");
  }

  @Override
  public IBinder onBind(Intent pBinder)
  {
    return this.musicBind;
  }

  @Override
  public boolean onUnbind(Intent pIntent)
  {
    this.player.stop();
    this.player.release();
    return false;
  }

  @Override
  public void onPrepared(MediaPlayer pMediaPlayer)
  {
    //start playback
    pMediaPlayer.start();
    //Notification
    Intent notIntent = new Intent(this, SciFiJukebox.class);
    notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent,
        PendingIntent.FLAG_UPDATE_CURRENT);

    Notification.Builder builder = new Notification.Builder(this);
    builder.setContentIntent(pendInt)
            .setSmallIcon(R.drawable.play)
            .setTicker(songTitle)
            .setOngoing(true)
            .setContentTitle("Playing")
            .setContentText(songTitle);
    Notification not = builder.build();
    startForeground(NOTIFY_ID, not);
  }

  @Override
  public void onCompletion(MediaPlayer pMediaPlayer)
  {
    if (this.player.getCurrentPosition() > 0)
    {
      pMediaPlayer.reset();
      playNext();
    }
  }

  @Override
  public boolean onError(MediaPlayer pMediaPlayer, int pErrorType, int pExtra)
  {
    Log.v(SCIFI_JUKEBOX_SERVICE, "Playback Error");
    pMediaPlayer.reset();
    return false;
  }

  private void initMusicPlayer()
  {
    this.player.setWakeMode(getApplicationContext(),
                            PowerManager.PARTIAL_WAKE_LOCK);
    this.player.setAudioStreamType(AudioManager.STREAM_MUSIC);
    this.player.setOnPreparedListener(this);
    this.player.setOnCompletionListener(this);
    this.player.setOnErrorListener(this);

    Log.i(SCIFI_JUKEBOX_SERVICE, "MusicPlayer initialized successfully");
  }

  public void setList(ArrayList<Song> pTheSongs)
  {
    this.songList = pTheSongs;
  }

  // Capture service interface
  public class MusicBinder extends Binder
  {
    MusicService getService()
    {
      return MusicService.this;
    }
  }

  public void resetPlayer()
  {
    this.player.reset();
  }

  public void playSong()
  {
    this.player.reset();
    //get song
    Song playSong = songList.get(this.songPosition);
    songTitle = playSong.getTitle();
    //get id
    long currSong = playSong.getID();
    //set uri
    Uri trackUri = ContentUris.withAppendedId(
      android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
      currSong);

    try
    {
      this.player.setDataSource(getApplicationContext(), trackUri);
    }
    catch(Exception e)
    {
      Log.e(SCIFI_JUKEBOX_SERVICE, "Error setting data source", e);
    }

    this.player.prepareAsync();
  }

  public int getPosition()
  {
    return this.player.getCurrentPosition();
  }

  public int getDuration()
  {
    return this.player.getDuration();
  }

  public boolean isPlaying()
  {
    return this.player.isPlaying();
  }

  public void pausePlayer()
  {
    this.player.pause();
  }

  public void seek(int pPositionNumber)
  {
    this.player.seekTo(pPositionNumber);
  }

  public void go()
  {
    this.player.start();
  }

  public void playPrevious()
  {
    this.songPosition--;
    if (this.songPosition < 0)
    {
      this.songPosition = this.songList.size() - 1;
    }
    playSong();
  }

  public void playNext()
  {
    this.songPosition++;
    if (this.songPosition >= this.songList.size())
    {
      this.songPosition = 0;
    }
    playSong();
  }

  public void setSong(int pSongIndex)
  {
    this.songPosition = pSongIndex;
  }
}
