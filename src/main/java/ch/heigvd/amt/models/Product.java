package ch.heigvd.amt.models;

import java.beans.ConstructorProperties;

public class Product {

  private final String name;
  private final double price;
  private final String description;
  private final int quantity;

  @ConstructorProperties({"name", "price", "description", "quantity"})
  public Product(String name, double price, String description, int quantity) {
    this.name = name;
    this.price = price;
    this.description = description;
    this.quantity = quantity;
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

  @Override
  public String toString() {
    return "Product{" +
        "name='" + name + '\'' +
        ", price=" + price +
        ", description='" + description + '\'' +
        ", quantity=" + quantity +
        '}';
  }
}
