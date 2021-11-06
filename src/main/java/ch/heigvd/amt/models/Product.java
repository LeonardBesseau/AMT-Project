package ch.heigvd.amt.models;

import java.beans.ConstructorProperties;

public class Product {

  private final String name;
  private final double price;
  private final String description;
  private final int quantity;
  private final int imageId;

  @ConstructorProperties({"name", "price", "description", "quantity", "image_id"})
  public Product(String name, double price, String description, int quantity, int imageId) {
    this.name = name;
    this.price = price;
    this.description = description;
    this.quantity = quantity;
    this.imageId = imageId;
  }

  public String getName() {
    return name;
  }

  public double getPrice() {
    return price;
  }

  public String getDescription() {
    return description;
  }

  public int getQuantity() {
    return quantity;
  }

  public int getImageId() {
    return imageId;
  }

  @Override
  public String toString() {
    return "Product{"
        + "name='"
        + name
        + '\''
        + ", price="
        + price
        + ", description='"
        + description
        + '\''
        + ", quantity="
        + quantity
        + '}';
  }
}
