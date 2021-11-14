package ch.heigvd.amt.models;

import java.beans.ConstructorProperties;
import javax.annotation.Nullable;

public class Image {

  private final int id;
  private final byte[] data;

  @ConstructorProperties({"id", "data"})
  public Image(int id, @Nullable byte[] data) {
    this.id = id;
    if (data == null) {
      this.data = new byte[0];
    } else {
      this.data = data;
    }
  }

  public int getId() {
    return id;
  }

  public byte[] getData() {
    return data;
  }
}
