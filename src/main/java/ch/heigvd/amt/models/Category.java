package ch.heigvd.amt.models;

import java.beans.ConstructorProperties;

public class Category {

  private final String name;

  @ConstructorProperties({"name"})
  public Category(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "Category{" + "name='" + name + '\'' + '}';
  }
}
