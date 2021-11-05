package ch.heigvd.amt.models;

import java.beans.ConstructorProperties;

public class Product {

  private final String name;
  private final double price;
  private final String description;
  private final int quantity;
  private final int image;

  @ConstructorProperties({"name", "price", "description", "quantity", "image"})
  public Product(String name, double price, String description, int quantity, int image) {
    this.name = name;
    this.price = price;
    this.description = description;
    this.quantity = quantity;
    this.image = image;
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

  public int getImage() {
    return image;
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
