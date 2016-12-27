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

public class MusicHandler
{
  // List of musics
  private ArrayList<Song> songList;
  private ListView songView;
  private ViewFlipper flipper;
  private Button button;
  private Context base;
  private View musicLayout;

  public MusicHandler(Context pContext)
  {
    this.base = pContext;
  }

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

  public View updateMusicLayout()
  {
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

  public void goBackward(View pView)
  {
    this.flipper.showPrevious();
  }

  public void goForward(View pView)
  {
    this.flipper.showNext();
  }

  public int getCurrentMusic()
  {
    return (this.flipper.getDisplayedChild());
  }

  public ArrayList<Song> getSongList()
  {
    return (this.songList);
  }

  public void getSongList(String pTargetPath)
  {
    //Retrieve song info
    ContentResolver musicResolver = this.base.getContentResolver();
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
