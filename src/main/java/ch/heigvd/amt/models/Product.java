package ch.heigvd.amt.models;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

public class Product {

  private final String name;
  private final double price;
  private final String description;
  private final int quantity;
  private final int imageId;
  private final List<Category> categories;

  @ConstructorProperties({"name", "price", "description", "quantity", "image_id", "category_name"})
  public Product(
      String name,
      double price,
      String description,
      int quantity,
      int imageId,
      @Nullable List<Category> categories) {
    this.name = name;
    this.price = price;
    this.description = description;
    this.quantity = quantity;
    this.imageId = imageId;
    this.categories = Objects.requireNonNullElseGet(categories, ArrayList::new);
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

  public List<Category> getCategories() {
    return categories;
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
