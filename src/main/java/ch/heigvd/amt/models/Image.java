package ch.heigvd.amt.models;

import java.beans.ConstructorProperties;

public class Image {
  private final int id;
  private final byte[] data;

  @ConstructorProperties({"id", "data"})
  public Image(int id, byte[] data) {
    this.id = id;
    this.data = data;
  }

  public int getId() {
    return id;
  }

  public byte[] getData() {
    return data;
  }
}
