package ch.heigvd.amt.models;

import java.beans.ConstructorProperties;

public class CartProduct {

  private final String name;
  private final Double price;
  private final Integer imageId;
  private final Integer quantity;

  @ConstructorProperties({"name", "price", "image_id", "quantity"})
  public CartProduct(String name, Double price, Integer imageId, Integer quantity) {
    this.name = name;
    this.price = price;
    this.imageId = imageId;
    this.quantity = quantity;
  }

  public String getName() {
    return name;
  }

  public Double getPrice() {
    return price;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public Integer getImageId() {
    return imageId;
  }

  public Double getTotal() {
    return quantity * price;
  }

  @Override
  public String toString() {
    return "Product{"
        + "name='"
        + name
        + '\''
        + ", price="
        + price
        + '\''
        + ", quantity="
        + quantity
        + '\''
        + ", image_id="
        + imageId
        + '}';
  }
}
