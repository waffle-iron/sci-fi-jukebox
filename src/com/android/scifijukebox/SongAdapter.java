package com.android.scifijukebox;

import java.util.ArrayList;
import android.content.Context;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.BaseAdapter;

public class SongAdapter extends BaseAdapter
{
  private ArrayList<Song> songs;
  private LayoutInflater songInf;

  public SongAdapter(Context pContext, ArrayList<Song> pTheSongs)
  {
    this.songs = pTheSongs;
    this.songInf = LayoutInflater.from(pContext);
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
  public long getItemId(int arg0)
  {
    return 0;
  }
 
  @Override
  public View getView(int pPosition, View pConvertView, ViewGroup pParent)
  {
    LinearLayout songLay = (LinearLayout)this.songInf.inflate(R.layout.song,
                            pParent, false);
    TextView songView = (TextView)songLay.findViewById(R.id.song_title);
    TextView artistView = (TextView)songLay.findViewById(R.id.song_artist);

    Song currSong = songs.get(pPosition);

    songView.setText(currSong.getTitle());
    artistView.setText(currSong.getArtist());

    songLay.setTag(pPosition);
    return songLay;
  }
 
}
