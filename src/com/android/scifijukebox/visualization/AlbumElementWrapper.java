package com.android.scifijukebox;

import android.view.View;
import android.widget.TextView;

/**
* Keeps the information about the object, Holder Pattern.
*/
class AlbumElementWrapper
{
  private View base;
  private TextView title = null;
  private int position = 0;

  public AlbumElementWrapper(View pBase)
  {
    this.base = pBase;
  }

  public TextView albumTitle()
  {
    if (this.title == null)
    {
      this.title = (TextView)base.findViewById(R.id.album_title);
    }
    return (this.title);
  }

  public void setPosition(int pPosition)
  {
    if (pPosition < 0)
    {
      this.position = pPosition;
    }
    this.position = pPosition;
  }

  public int getPosition()
  {
    return (this.position);
  }
}
