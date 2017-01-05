package com.android.scifijukebox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;

import android.view.View;
import android.view.LayoutInflater;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;

import android.widget.ViewFlipper;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ImageButton;
import android.widget.TextView;

import android.content.Context;

import android.util.Log;

/**
* Class responsible to handling player actions
*
*/
public class MusicHandler
{
  // List of musics
  private ArrayList<Song> songList;
  private ListView songView;
  private ViewFlipper flipper;
  private Button button;
  private Context base;
  private View musicLayout;

  // Service
  private MusicService musicService;

  // Attributes responsible for controlling music
  private boolean musicBound = false;
  private boolean playbackPaused = false;
  private boolean firstTime = true;
  private int playbackPosition = 0;

  private static final String SCIFI_JUKEBOX_PLAYER = "jukebox-player";

  public MusicHandler(Context pContext)
  {
    this.base = pContext;
  }

  /**
  * Build player layout.
  */
  public View getMusicViewLayout()
  {
    LayoutInflater inflater = LayoutInflater.from(this.base);

    this.musicLayout = inflater.inflate(R.layout.player, null);
    return (this.musicLayout);
  }

  public void initMusicList()
  {
    this.songList = new ArrayList<Song>();
    this.getSongList(UtilJukebox.PATH_MUSIC);
    Collections.sort(this.songList, new Comparator<Song>()
    {
      public int compare(Song a, Song b)
      {
        return a.getTitle().compareTo(b.getTitle());
      }
    });
  }

  /**
  * Update from one music to another
  */
  public View updateMusicLayout()
  {
    this.musicService.resetPlayer();
    LayoutInflater inflater = LayoutInflater.from(this.base);
    this.musicLayout = inflater.inflate(R.layout.player, null);

    this.flipper = (ViewFlipper)this.musicLayout.findViewById(R.id.details);

    flipper.setInAnimation(AnimationUtils.loadAnimation(this.base,
                                                       R.anim.push_left_in));
    flipper.setOutAnimation(AnimationUtils.loadAnimation(this.base,
                                                        R.anim.push_left_out));

    ImageButton backward = (ImageButton)this.musicLayout.findViewById(R.id.backward);
    ImageButton play = (ImageButton)this.musicLayout.findViewById(R.id.play);
    ImageButton forward = (ImageButton)this.musicLayout.findViewById(R.id.forward);

    for (Song song : this.songList)
    {
      Button btn = new Button(this.base);
      btn.setText(song.getTitle());
      this.flipper.addView(btn, new ViewGroup.LayoutParams(
                                      ViewGroup.LayoutParams.FILL_PARENT,
                                      ViewGroup.LayoutParams.FILL_PARENT));
    }
    return (this.musicLayout);
  }

  public void resetPlayer()
  {
    this.pauseMusic();
    this.musicService.resetPlayer();
    this.resetControlAttributes();
  }

  /**
  * Handling play and pause music. Basically we can find here the logic to
  * handle play and pause in the same button.
  */
  public void playPauseMusic()
  {
    int displayedChild = this.flipper.getDisplayedChild();
    this.musicService.setSong(displayedChild);
    if (this.playbackPaused)
    {
      this.pauseMusic();
    }
    else
    {
      this.resumeMusic();
    }

    if (this.firstTime)
    {
      this.playbackPaused = true;
      this.firstTime = false;
      this.musicService.playSong();
    }
  }

  private void pauseMusic()
  {
    ImageButton pauseButton = (ImageButton)this.musicLayout.findViewById(R.id.play);
    pauseButton.setImageResource(R.drawable.play);
    this.playbackPaused = false;
    this.playbackPosition = this.getCurrentPosition();
    this.musicService.pausePlayer();
  }

  private void resumeMusic()
  {
    ImageButton playButton = (ImageButton)this.musicLayout.findViewById(R.id.play);
    playButton.setImageResource(R.drawable.pause);

    this.playbackPaused = true;
    this.musicService.seek(this.playbackPosition);
    this.musicService.go();
  }

  public void goBackward(View pView)
  {
    this.flipper.showPrevious();
    if (this.isPlaying())
    {
      this.musicService.playPrevious();
    }
    else
    {
      this.resetControlAttributes();
    }
  }

  public void goForward(View pView)
  {
    this.flipper.showNext();
    if (this.isPlaying())
    {
      this.musicService.playNext();
    }
    else
    {
      this.resetControlAttributes();
    }
  }

  private void resetControlAttributes()
  {
    this.playbackPosition = 0;
    this.firstTime = true;
    this.playbackPaused = false;
  }

  /**
  *  Get current position of music, notice that is required to make some
  *  verifications before return this value
  *  @return Return a integer representing the current position of music
  */
  private int getCurrentPosition()
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

  private boolean isPlaying()
  {
    if (this.musicService != null && this.musicBound)
    {
      return this.musicService.isPlaying();
    }
    return false;
  }

  public void setMusicService(MusicService pMusicService)
  {
    this.musicService = pMusicService;
  }

  public void setMusicBound(boolean pStatus)
  {
    this.musicBound = pStatus;
  }

  public ArrayList<Song> getSongList()
  {
    return (this.songList);
  }

  public void getSongList(String pTargetPath)
  {
    //Retrieve song info
    ContentResolver musicResolver = this.base.getContentResolver();
    //FIXME: This not work as expected
    String[] filterBy = {"%" + pTargetPath + "%"};
    Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    String data = android.provider.MediaStore.Audio.Media.DATA;
    Cursor musicCursor = musicResolver.query(musicUri, null, data + " like ? ",
                                             filterBy, null);

    this.songList.clear();

    if (musicCursor != null && musicCursor.moveToFirst())
    {
      // Get columns names
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
}
