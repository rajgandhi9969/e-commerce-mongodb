import java.util.HashMap;
import java.util.HashSet;

public interface StoreAPI {

    void createAccount(String username, String password, String firstName, String lastName);

    void submitOrder(String orderDate, String username, String password, HashMap<Long, Integer> listOfProductsAndQuantities);

    void postReview(long reviewId, String username, String password, Long product_id, int rating, String reviewText, String date);

    boolean authorized(String username, String password);

    void addProduct(long product_id, String name, String description, float price, int initialStock);

    void updateStockLevel(int product_id, int itemCountToAdd);

    void getProductAndReviews(int product_id);

    void getAverageUserRating(String username);

    int[] getMissedProducts();

}
