package com.android.scifijukebox;

import com.android.scifijukebox.MusicService.MusicBinder;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.io.File;

import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.ListView;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.Button;
import android.widget.ViewFlipper;
import android.widget.TextView;
import android.widget.ImageButton;

import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;

import android.view.animation.AnimationUtils;
import android.view.ViewGroup;

import android.view.Menu;

import android.util.Log;

/**
*  This class consists of main activity of the project. This class can be
*  considered the main class of the project. This class works directly with
*  MusicService class.
*  @see MusicService
*/
public class SciFiJukebox extends Activity implements MediaPlayerControl
{
  // List of musics
  private ArrayList<Song> songList;
  private ListView songView;
  private ListView albumView;
  private String pathMusic = "scifi-jukebox";
  private ArrayList<Album> albuns;
  private ViewFlipper flipper;
  private Button button;

  // Attributes for handling the communication between Activity and Service
  private MusicService musicService;
  private Intent playIntent;
  private boolean musicBound = false;
  private MusicController controller;

  // Attributes to handling notifications
  private String songTitle = "";
  private static final int NOTIFY_ID = 1;
  private static final String SCIFI_JUKEBOX = "jukebox";

  // Attributes responsible for controlling music
  private boolean paused = false;
  private boolean playbackPaused = false;

  private View albumLayout;
  private View musicLayout;

  @Override
  public void onCreate(Bundle pSavedInstanceState)
  {
    super.onCreate(pSavedInstanceState);
    this.albumLayout = getLayoutInflater().inflate(R.layout.main, null);
    this.musicLayout = getLayoutInflater().inflate(R.layout.player, null);
    setContentView(this.albumLayout);

    this.initDefaultDirectory();
    this.initMusicList();
    this.initAlbumList();
    this.setAlbumLayout();

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
      this.setController();
      this.paused = false;
    }
  }

  @Override
  protected void onStop()
  {
    this.controller.hide();
    super.onStop();
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
      musicService.setList(songList);
      musicBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName pName)
    {
      musicBound = false;
    }
  };

  /**
  *  Initialize elements related to music list
  */
  private void getAlbuns()
  {
    File[] files = new File(this.getRootDirectory()).listFiles();

    for (File file : files)
    {
      if (file.isDirectory())
      {
        String albumPath = file.toString();
        String albumName = albumPath.substring(albumPath.lastIndexOf('/') + 1);

        Album newAlbum = new Album(albumName, albumPath);
        this.albuns.add(newAlbum);
      }
    }
  }

  private void initMusicList()
  {
    this.songView = (ListView)findViewById(R.id.album_list);
    this.songList = new ArrayList<Song>();
    getSongList(this.pathMusic);
    Collections.sort(this.songList, new Comparator<Song>()
      {
        public int compare(Song a, Song b)
        {
          return a.getTitle().compareTo(b.getTitle());
        }
      });
  }

  private void initAlbumList()
  {
    this.albumView = (ListView)findViewById(R.id.album_list);
    this.albuns = new ArrayList<Album>();
    this.getAlbuns();
  }

  private void setAlbumLayout()
  {
    AlbumElementAdapter adapter = new AlbumElementAdapter(this, this.albuns);
    this.albumView.setAdapter(adapter);
  }

  private void setMusicLayout()
  {
    setContentView(this.musicLayout);
    this.flipper = (ViewFlipper)findViewById(R.id.details);

    flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_in));
    flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_out));

    ImageButton backward = (ImageButton)findViewById(R.id.backward);
    ImageButton play = (ImageButton)findViewById(R.id.play);
    ImageButton forward = (ImageButton)findViewById(R.id.forward);

    for (Song song : this.songList)
    {
      Button btn = new Button(this);
      btn.setText(song.getTitle());
      this.flipper.addView(btn, new ViewGroup.LayoutParams(
                                      ViewGroup.LayoutParams.FILL_PARENT,
                                      ViewGroup.LayoutParams.FILL_PARENT));
    }
  }

  public void goBackward(View pView)
  {
    this.flipper.showPrevious();
  }

  public void goForward(View pView)
  {
    this.flipper.showNext();
  }

  public void playSong(View pView)
  {
    int displayedChild = this.flipper.getDisplayedChild();

    this.musicService.setSong(displayedChild);
    this.musicService.playSong();
    if (this.playbackPaused)
    {
      this.playbackPaused = false;
    }
  }

 // private void setMusicLayout()
 // {
 //   SongElementAdapter adapter = new SongElementAdapter(this, this.songList);
 //   this.songView.setAdapter(adapter);
 //   this.setController();
 // }

  private void initDefaultDirectory()
  {
    boolean success = true;

    File folder = new File(this.getRootDirectory());

    if (!folder.exists())
    {
      success = folder.mkdir();
      Log.i(SCIFI_JUKEBOX, "Created default directory in: " +
            this.getRootDirectory());
    }
    else
    {
      Log.i(SCIFI_JUKEBOX, "Default directory already created");
    }
  }

  private String getRootDirectory()
  {
    String directoryPath = new File(Environment.getExternalStorageDirectory(),
                                    "Music").toString();
    directoryPath = new File(directoryPath, this.pathMusic).toString();
    return directoryPath;
  }

  /**
  *  Get music information from device. This method scan all directories to
  *  find musics.
  */
  private void getSongList(String pTargetPath)
  {
    //Retrieve song info
    ContentResolver musicResolver = getContentResolver();
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

  /**
  *  Set music controller. Basically, this method is responsible for setup
  *  playNext and playPrevious.
  */
  private void setController()
  {
    this.controller = new MusicController(this);

    View.OnClickListener nextMusic = new View.OnClickListener()
    {
      @Override
      public void onClick(View pView)
      {
        playNext();
      }
    };

    View.OnClickListener previousMusic = new View.OnClickListener()
    {
      @Override
      public void onClick(View pView)
      {
        playPrev();
      }
    };

    this.controller.setPrevNextListeners(nextMusic, previousMusic);
    this.controller.setMediaPlayer(this);
    this.controller.setAnchorView(findViewById(R.id.album_list));
    this.controller.setEnabled(true);
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

  /**
  *  Each row of the list is tied up with this method. Basically, it takes the
  *  position number clicked by the user and retrieve the information about it
  *  to finally play the music.
  */
  public void songPicked(View pView)
  {
    int musicToPlay = ((SongElementWrapper)pView.getTag()).getPosition();
    this.musicService.setSong(musicToPlay);
    this.musicService.playSong();
    if (this.playbackPaused)
    {
      this.setController();
      this.playbackPaused = false;
    }
    this.controller.show(0);
  }

  public void albumPicked(View pView)
  {
    int albumToGo = ((AlbumElementWrapper)pView.getTag()).getPosition();
    Uri uri = Uri.parse(albuns.get(albumToGo).getTitle());
    String path = uri.getPath();
    String idString = path.substring(path.lastIndexOf('/') + 1);

    // Get a list of musics realted with album
    this.getSongList(idString);
    this.setMusicLayout();
  }

  /**
  *  Go to next music and play it
  */
  private void playNext()
  {
    this.musicService.playNext();
    if (this.playbackPaused)
    {
      this.setController();
      this.playbackPaused = false;
    }
    this.controller.show(0);
  }

  /**
  *  Go to previous music and play it
  */
  private void playPrev()
  {
    this.musicService.playPrevious();
    if (this.playbackPaused)
    {
      this.setController();
      this.playbackPaused = false;
    }
    this.controller.show(0);
  }

  @Override
  protected void onDestroy()
  {
    this.stopService(this.playIntent);
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

  /**
  *  Get current position of music, notice that is required to make some
  *  verifications before return this value
  *  @return Return a integer representing the current position of music
  */
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

  /**
  *  Get music duration
  */
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

  /**
  *  Verify if music is playing
  */
  @Override
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
  @Override
  public void pause()
  {
    this.playbackPaused = true;
    this.musicService.pausePlayer();
  }

  /**
  *  Get an id and seek this position in music
  *  @param pPosition integer presenting a position in a music
  */
  @Override
  public void seekTo(int pPosition)
  {
    this.musicService.seek(pPosition);
  }

  /**
  *  Start playing music
  */
  @Override
  public void start()
  {
    this.musicService.go();
  }
}
