package com.android.scifijukebox;

import android.view.View;
import android.widget.TextView;


/**
* Keeps the information about the object, Holder Pattern.
*/
class SongElementWrapper
{
  private View base;
  private TextView title = null;
  private TextView artist = null;
  private int position = 0;

  public SongElementWrapper(View pBase)
  {
    this.base = pBase;
  }

  public TextView getTitle()
  {
    if (this.title == null)
    {
      this.title = (TextView)base.findViewById(R.id.song_title);
    }
    return (this.title);
  }

  public TextView getArtist()
  {
    if (this.artist == null)
    {
      this.artist = (TextView)base.findViewById(R.id.song_artist);
    }
    return (this.artist);
  }

  public int getPosition()
  {
    return this.position;
  }

  public void setPosition(int pPosition)
  {
    if (pPosition < 0)
    {
      pPosition = 0;
    }
    this.position = pPosition;
  }
}
