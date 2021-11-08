package ch.heigvd.amt.models;

import java.beans.ConstructorProperties;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

public class Category {
  private final String name;

  @ConstructorProperties({"name"})
  public Category(@ColumnName("category_name") String name) {
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
