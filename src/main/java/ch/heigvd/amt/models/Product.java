package ch.heigvd.amt.models;

import java.beans.ConstructorProperties;

public class Product {

    private final String name;
    private final double price;
    private final String description;
    private final String refCode;
    private final String tags;

    @ConstructorProperties({"name", "price", "description", "refCode", "tags"})
    public Product(String name, double price, String description, String refCode, String tags) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.refCode = refCode;
        this.tags = tags;
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

    public String getRefCode() {
        return refCode;
    }

    public String getTags() {
        return tags;
    }

    @Override
    public String toString() {
        return "Product{" +
            "name='" + name + '\'' +
            ", price=" + price +
            ", description='" + description + '\'' +
            ", refCode='" + refCode + '\'' +
            ", tags='" + tags + '\'' +
            '}';
    }
}
