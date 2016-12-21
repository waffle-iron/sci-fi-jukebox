package com.android.scifijukebox;

/**
*  This class is responsible for keep albuns informations
*/
public class Album
{
  private String title;
  private String description;
  private String albumPath;
  private String imagePath;

  public Album(String pAlbumTitle)
  {
    this.setTitle(pAlbumTitle);
  }

  public Album(String pAlbumTitle, String pAlbumPath)
  {
    this.setTitle(pAlbumTitle);
    this.setAlbumPath(pAlbumPath);
  }

  public void setTitle(String pTitle)
  {
    this.title = verifyString(pTitle);
  }

  public String getTitle()
  {
    return this.title;
  }

  public void setDescription(String pDescription)
  {
    this.description = verifyString(pDescription);
  }

  public void setAlbumPath(String pPath)
  {
    //TODO: Verify if path is valid
    this.albumPath = pPath;
  }

  public String getAlbumPath()
  {
    return (this.albumPath);
  }

  public String toString()
  {
    return (this.title + " : " + this.description);
  }

  private String verifyString(String pString)
  {
    if (pString == null || pString.isEmpty())
    {
      pString = "Unknown";
    }
    return pString;
  }
}
