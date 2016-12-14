package com.android.scifijukebox;

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
                          implements MediaPlayer.OnPreparedListener,
                                     MediaPlayer.OnErrorListener,
                                     MediaPlayer.OnCompletionListener
{
  private MediaPlayer player;
  private ArrayList<Song> songList;
  private int songPosition;
  private String songTitle="";
  private static final int NOTIFY_ID = 1;

  private final IBinder musicBind = new MusicBinder();

  private boolean shuffle = false;
  private Random rand;

  // Service methods
  @Override
  public void onCreate()
  {
    super.onCreate();
    this.songPosition = 0;
    this.rand = new Random();
    this.player = new MediaPlayer();
    initMusicPlayer();
  }

  @Override
  public void onDestroy()
  {
    stopForeground(true);
  }

  @Override
  public IBinder onBind(Intent binder)
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

  // MediaPlayer interface
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
    Log.v("MUSIC PLAYER", "Playback Error");
    pMediaPlayer.reset();
    return false;
  }

  //TODO: Make it private
  public void initMusicPlayer()
  {
    this.player.setWakeMode(getApplicationContext(),
                            PowerManager.PARTIAL_WAKE_LOCK);
    this.player.setAudioStreamType(AudioManager.STREAM_MUSIC);
    this.player.setOnPreparedListener(this);
    this.player.setOnCompletionListener(this);
    this.player.setOnErrorListener(this);
  }

  public void setList(ArrayList<Song> theSongs)
  {
    this.songList = theSongs;
  }

  // Capture service interface
  public class MusicBinder extends Binder
  {
    MusicService getService()
    {
      return MusicService.this;
    }
  }

  public void setShuffle()
  {
    if (this.shuffle)
    {
      this.shuffle = false;
    }
    else
    {
      this.shuffle = true;
    }
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
      Log.e("MUSIC SERVICE", "Error setting data source", e);
    }

    this.player.prepareAsync();
  }

  public int getPosn()
  {
    return this.player.getCurrentPosition();
  }

  public int getDur()
  {
    return this.player.getDuration();
  }

  public boolean isPng()
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

  public void playPrev()
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
    if (this.shuffle)
    {
      int newSong = this.songPosition;
      while (newSong == this.songPosition)
      {
        newSong = rand.nextInt(this.songList.size());
      }
      songPosition = newSong;
    }
    else
    {
      this.songPosition++;
      if (this.songPosition >= this.songList.size())
      {
        this.songPosition = 0;
      }
    }
    playSong();
  }

  public void setSong(int pSongIndex)
  {
    this.songPosition = pSongIndex;
  }
}
