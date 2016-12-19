package com.android.scifijukebox;

/**
*  This class is responsible for keep albuns informations
*/
public class Album
{
  private String title;

  public Album(String pAlbumTitle)
  {
    this.title = pAlbumTitle;
  }

  public String getTitle()
  {
    return this.title;
  }

  public String toString()
  {
    return (this.title);
  }
}
