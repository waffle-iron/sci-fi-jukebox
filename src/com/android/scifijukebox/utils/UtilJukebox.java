package com.android.scifijukebox;

import android.os.Environment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.io.File;

final class UtilJukebox
{
  public static String PATH_MUSIC = "scifi-jukebox";

  private UtilJukebox()
  {
    throw new AssertionError();
  }

  public static String getRootDirectory()
  {
    String directoryPath = new File(Environment.getExternalStorageDirectory(),
                                    "Music").toString();
    directoryPath = new File(directoryPath, PATH_MUSIC).toString();
    return directoryPath;
  }

}
