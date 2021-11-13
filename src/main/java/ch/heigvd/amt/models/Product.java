package ch.heigvd.amt.models;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import org.jdbi.v3.core.mapper.Nested;

public class Product {

  private final String name;
  private final double price;
  private final String description;
  private final int quantity;
  private final Image image;
  private final List<Category> categories;

  @ConstructorProperties({"name", "price", "description", "quantity", "image", "category_name"})
  public Product(
      String name,
      double price,
      String description,
      int quantity,
      @Nested("image") Image image,
      @Nullable List<Category> categories) {
    this.name = name;
    this.price = price;
    this.description = description;
    this.quantity = quantity;
    this.image = image;
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

  public Image getImage() {
    return image;
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
