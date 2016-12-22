package com.android.scifijukebox;

import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;

/**
* Keeps the information about the object, Holder Pattern.
*/
class AlbumElementWrapper
{
  private View base;
  private TextView title = null;
  private TextView description = null;
  private ImageView albumImage = null;
  private int position = 0;

  public AlbumElementWrapper(View pBase)
  {
    this.base = pBase;
  }

  public TextView albumTitle()
  {
    if (this.title == null)
    {
      this.title = (TextView)this.base.findViewById(R.id.album_title);
    }
    return (this.title);
  }

  public TextView albumDescription()
  {
    if (this.description == null)
    {
      this.description = (TextView)this.base.findViewById(
                                                      R.id.album_description);
    }
    return (this.description);
  }


  public ImageView albumIcon()
  {
    if (this.albumImage == null)
    {
      this.albumImage = (ImageView)this.base.findViewById(R.id.album_icon);
    }
    return (this.albumImage);
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
