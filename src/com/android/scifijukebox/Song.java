package com.android.scifijukebox;

public class Song
{
  private long id;
  private String title;
  private String artist;

  public Song(long pSongID, String pSongTitle, String pSongArtist)
  {
    this.id = pSongID;
    this.title = pSongTitle;
    this.artist = pSongArtist;
  }

  public long getID()
  {
    return this.id;
  }

  public String getTitle()
  {
    return this.title;
  }

  public String getArtist()
  {
    return this.artist;
  }

  public String toString()
  {
    return (this.title + " : " + this.artist);
  }
}
