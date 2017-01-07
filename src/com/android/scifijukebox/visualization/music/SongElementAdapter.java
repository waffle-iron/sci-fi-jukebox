package com.android.scifijukebox;

import java.util.ArrayList;
import android.content.Context;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.BaseAdapter;

import android.util.Log;

/**
* Adapter for each music inside list
* @class SongElementAdapter
*/
public class SongElementAdapter extends BaseAdapter
{
  private ArrayList<Song> songs;
  private LayoutInflater inflater;

  private static final String SCIFI_JUKEBOX_ADAPTER = "jukebox-adapter";

  public SongElementAdapter(Context pContext, ArrayList<Song> pTheSongs)
  {
    this.songs = pTheSongs;
    this.inflater = LayoutInflater.from(pContext);
  }

  public void setSongList(ArrayList<Song> pSongs)
  {
    this.songs = pSongs;
  }

  @Override
  public int getCount()
  {
    return this.songs.size();
  }

  @Override
  public Object getItem(int pItem)
  {
    return null;
  }

  @Override
  public long getItemId(int pId)
  {
    return 0;
  }

  @Override
  public View getView(int pPosition, View pConvertView, ViewGroup pParent)
  {
    View row = pConvertView;
    SongElementWrapper wrapper = null;

    if (row == null)
    {
      row = this.inflater.inflate(R.layout.song, pParent, false);
      wrapper = new SongElementWrapper(row);
      row.setTag(wrapper);
    }
    else
    {
      wrapper = (SongElementWrapper)row.getTag();
    }

    Song currentSong = this.songs.get(pPosition);

    wrapper.getTitle().setText(currentSong.getTitle());
    wrapper.setPosition(pPosition);

    return (row);
  }
}
