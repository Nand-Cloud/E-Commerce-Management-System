package f2;

public class Product {
    private int productId;
    private String style;
    private int stock;
    private double originalPrice;
    private double discountedPrice;

    public Product(int productId, String style, int stock, double originalPrice, double discountedPrice) {
        this.productId = productId;
        this.style = style;
        this.stock = stock;
        this.originalPrice = originalPrice;
        this.discountedPrice = discountedPrice;
    }

    public int getProductId() {
        return productId;
    }

    public String getStyle() {
        return style;
    }

    public int getStock() {
        return stock;
    }

    public double getOriginalPrice() {
        return originalPrice;
    }

    public double getDiscountedPrice() {
        return discountedPrice;
    }
}
