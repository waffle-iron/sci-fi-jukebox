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
* @class AlbumElementAdapter
*/
public class AlbumElementAdapter extends BaseAdapter
{
  private ArrayList<Album> albuns;
  private LayoutInflater inflater;

  private static final String SCIFI_JUKEBOX_ADAPTER = "jukebox-adapter-album";

  public AlbumElementAdapter(Context pContext, ArrayList<Album> pTheAlbum)
  {
    this.albuns = pTheAlbum;
    this.inflater = LayoutInflater.from(pContext);
  }

  @Override
  public int getCount()
  {
    return this.albuns.size();
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
    AlbumElementWrapper wrapper = null;

    if (row == null)
    {
      row = this.inflater.inflate(R.layout.album, pParent, false);
      wrapper = new AlbumElementWrapper(row);
      row.setTag(wrapper);
    }
    else
    {
      wrapper = (AlbumElementWrapper)row.getTag();
    }

    Album currentAlbum = this.albuns.get(pPosition);

    wrapper.albumTitle().setText(currentAlbum.getTitle());
    wrapper.setPosition(pPosition);

    return (row);
  }
}
