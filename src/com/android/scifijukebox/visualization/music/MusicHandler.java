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

  private SongElementAdapter adapter;

  private int currentMusic = 0;

  private static final String SCIFI_JUKEBOX_PLAYER = "jukebox-player";

  public MusicHandler(Context pContext)
  {
    this.base = pContext;
    this.adapter = new SongElementAdapter(this.base, this.songList);
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

    this.setTransitionDirection(true);

    ImageButton backward = (ImageButton)this.musicLayout.findViewById(R.id.backward);
    ImageButton play = (ImageButton)this.musicLayout.findViewById(R.id.play);
    ImageButton forward = (ImageButton)this.musicLayout.findViewById(R.id.forward);

    for (Song song : this.songList)
    {
      Button btn = new Button(this.base);
      View musicInfo = LayoutInflater.from(this.base).inflate(R.layout.music_info, null);
      TextView title = (TextView)musicInfo.findViewById(R.id.title);
      TextView artist = (TextView)musicInfo.findViewById(R.id.artist);
      TextView year = (TextView)musicInfo.findViewById(R.id.year);

      title.setText("Music: " + song.getTitle());
      artist.setText("Artist: " + song.getArtist());
      year.setText("Year: Unknown");

      this.flipper.addView(musicInfo, new ViewGroup.LayoutParams(
                                      ViewGroup.LayoutParams.FILL_PARENT,
                                      ViewGroup.LayoutParams.FILL_PARENT));
    }
    this.buildMusicList();
    return (this.musicLayout);
  }

  private ListView buildMusicList()
  {
    ListView musicList = (ListView)this.musicLayout.findViewById(
                                                      R.id.music_list);
    this.adapter.setSongList(this.songList);

    musicList.setAdapter(adapter);
    return musicList;
  }

  public void resetPlayer()
  {
    this.pauseMusic();
    this.musicService.resetPlayer();
    this.resetControlAttributes();
  }

  /**
  * Every time that someone uses player menu, this method update music view
  */
  public void playFromButton()
  {
    this.currentMusic = this.flipper.getDisplayedChild();
    this.playPauseMusic();
  }

  /**
  * Every time that someone try to play music from list
  */
  public void playFromList(int pToPlay)
  {
    this.currentMusic = (pToPlay < 0) ? 0 : pToPlay;
    this.resetPlayer();
    this.setTransitionDirection(true);
    this.flipper.setDisplayedChild(pToPlay);
    this.playPauseMusic();
  }

  /**
  * Handling play and pause music. Basically we can find here the logic to
  * handle play and pause in the same button.
  */
  private void playPauseMusic()
  {
    //int displayedChild = this.flipper.getDisplayedChild();
    this.musicService.setSong(this.currentMusic);
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
    ImageButton pauseButton = (ImageButton)this.musicLayout.findViewById(
                                                                    R.id.play);
    pauseButton.setImageResource(R.drawable.play);
    this.playbackPaused = false;
    this.playbackPosition = this.getCurrentPosition();
    this.musicService.pausePlayer();
  }

  private void resumeMusic()
  {
    ImageButton playButton = (ImageButton)this.musicLayout.findViewById(
                                                                    R.id.play);
    playButton.setImageResource(R.drawable.pause);

    this.playbackPaused = true;
    this.musicService.seek(this.playbackPosition);
    this.musicService.go();
  }

  public void goBackward(View pView)
  {
    this.setTransitionDirection(false);

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
    this.setTransitionDirection(true);
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

  private void setTransitionDirection(boolean pRightToLeft)
  {
    int in = R.anim.push_left_in;
    int out = R.anim.push_left_out;
    in = pRightToLeft ? R.anim.push_left_in : R.anim.push_right_in;
    out = pRightToLeft ? R.anim.push_left_out : R.anim.push_right_out;

    this.flipper.setInAnimation(AnimationUtils.loadAnimation(this.base, in));
    this.flipper.setOutAnimation(AnimationUtils.loadAnimation(this.base, out));
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
