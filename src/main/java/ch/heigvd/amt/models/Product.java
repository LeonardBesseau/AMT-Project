package ch.heigvd.amt.models;

public class Product {

    private String name;
    private double price;
    private String description;
    private long id;
    private String[] tags;

    public Product() {
    }

    public Product(String name, double price, String description, long id, String[] tags) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.id = id;
        this.tags = tags;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }
}
