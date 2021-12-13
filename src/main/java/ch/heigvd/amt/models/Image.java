package ch.heigvd.amt.models;

import java.beans.ConstructorProperties;
import java.util.Objects;
import javax.annotation.Nullable;

public class Image {

  public static final int DEFAULT_IMAGE_ID = 0;
  public static final Image DEFAULT_IMAGE = new Image(DEFAULT_IMAGE_ID, null);

  private final int id;
  private final byte[] data;

  @ConstructorProperties({"id", "data"})
  public Image(int id, @Nullable byte[] data) {
    this.id = id;
    this.data = Objects.requireNonNullElseGet(data, () -> new byte[0]);
  }

  public int getId() {
    return id;
  }

  public byte[] getData() {
    return data;
  }
}
