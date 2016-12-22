package com.android.scifijukebox;

import android.view.View;
import android.view.LayoutInflater;

import android.content.Context;

import android.widget.ListView;

import java.util.ArrayList;
import java.io.File;


class AlbumHandler
{
  private ListView albumView;
  private ArrayList<Album> albuns;
  private Context base;
  private View albumLayout;

  public AlbumHandler(Context pBase)
  {
    this.base = pBase;
  }

  public Album getAlbumById(int pId)
  {
    //TODO: Make a verification in pId
    return (this.albuns.get(pId));
  }

  public View getAlbumViewLayout()
  {
    LayoutInflater inflater = LayoutInflater.from(this.base);

    this.albumLayout = inflater.inflate(R.layout.main, null);
    return (this.albumLayout);
  }

  public void initAlbumList()
  {
    this.albumView = (ListView)this.albumLayout.findViewById(R.id.album_list);
    this.albuns = new ArrayList<Album>();
    this.getAlbuns();
    this.setAlbumLayout();
  }

  /**
  *  Initialize elements related to music list
  */
  private void getAlbuns()
  {
    File[] files = new File(UtilJukebox.getRootDirectory()).listFiles();

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

  private void setAlbumLayout()
  {
    AlbumElementAdapter adapter = new AlbumElementAdapter(this.base, this.albuns);
    this.albumView.setAdapter(adapter);
  }
}
