/***
 * Author: Raj Pradeep Gandhi
 * This program fills the database. This intended to be executed when blank database is provided.
 */

import com.devskiller.jfairy.Fairy;
import com.devskiller.jfairy.producer.text.TextProducer;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;


class DatabaseFiller {

    public static int getRandomNumber(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /**
     * This method is used to clear the db before it can filled with data
     */
    public void clearDB() {
        String[] collections = {"reviews", "products", "orders", "users"};
        MongoClient mongoClient = null;
        try {
            mongoClient = getConnectionClient();
            MongoDatabase mongoDBConnect = mongoClient.getDatabase("cart");
            mongoDBConnect.drop();
            mongoDBConnect = mongoClient.getDatabase("cart");
            for (String collectionsToMake : collections) {
                mongoDBConnect.createCollection(collectionsToMake);
                MongoCollection<Document> collection = mongoDBConnect.getCollection(collectionsToMake);
                if (collectionsToMake.equals("reviews")) {
                    Document uniqueIndex = new Document("review_id", 1);
                    collection.createIndex(uniqueIndex, new IndexOptions().unique(true));
                } else if (collectionsToMake.equals("products")) {
                    Document uniqueIndex = new Document("product_id", 1);
                    collection.createIndex(uniqueIndex, new IndexOptions().unique(true));
                } else if (collectionsToMake.equals("users")) {
                    Document uniqueIndex = new Document("username", 1);
                    collection.createIndex(uniqueIndex, new IndexOptions().unique(true));
                }
            }
            System.out.println("Database is cleared, new data is being filled");
        } catch (MongoCommandException mongoCommandException) {
            System.out.println("There was an error executing mongo command, please try again later");
        } catch (MongoWriteException mongoWriteException) {
            System.out.println("There was an error writing to mongo db");
        } catch (MongoSocketReadException mongoSocketReadException) {
            System.out.println("There was an error reading from socket");
        } finally {
            mongoClient.close();
        }
    }

    /***
     * Method for filling random users
     * @param totalEntries: Number of users to be filled
     */
    public void fillUsers(int totalEntries) {

        StoreAPI storeOperations = new StoreAPIMongoImpl();
        for (int i = 0; i < totalEntries; i++) {
            storeOperations.createAccount("user_" + i, "password_" + i, Fairy.create().person().getFirstName(), Fairy.create().person().getLastName());
        }
    }

    /***
     * This method is used to fill random products to prdducts table
     * @param totalEntries: number of products to be filled
     */
    public void fillProducts(int totalEntries) {
        Fairy fairy = Fairy.create();
        TextProducer text = fairy.textProducer();
        StoreAPI storeOperations = new StoreAPIMongoImpl();
        for (int i = 0; i < totalEntries; i++) {
            storeOperations.addProduct(i, text.sentence(getRandomNumber(1, 10)), text.paragraph(getRandomNumber(1, 50)), (float) getRandomNumber(1, 3000), getRandomNumber(1, 5000));
        }
    }

    /**
     * Method for random orders to be placed
     *
     * @param totalEntries: Total number of orders to filled
     */
    public void fillOrders(int totalEntries) {
        StoreAPI storeOperations = new StoreAPIMongoImpl();
        for (int i = 0; i < totalEntries; i++) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            int getRandomUserId = getRandomNumber(0, 999);
            storeOperations.submitOrder(dateFormat.format(date), "user_" + getRandomUserId, "password_" + getRandomUserId, getProductsToBeOrdered(getRandomNumber(1, 10)));
        }
    }

    /***
     * This method map of products and quantities to be ordered for a particular order
     * @param totalEntries: number of products in a order
     * @return: Map of products and their quantities that are to be required.
     */
    public HashMap<Long, Integer> getProductsToBeOrdered(int totalEntries) {
        HashMap<Long, Integer> listOfProducts = new HashMap<>();
        for (int i = 0; i < totalEntries; i++) {
            listOfProducts.put((long) getRandomNumber(0, 9999), getRandomNumber(1, 10));
        }
        return listOfProducts;
    }

    /***
     * This method is used to fill random reviews in the reviews table
     * @param totalEntries: number reviews to be filled.
     */
    public void fillReviews(int totalEntries) {
        StoreAPI storeOperations = new StoreAPIMongoImpl();
        for (int i = 0; i < totalEntries; i++) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            int getRandomUserId = getRandomNumber(0, 999);
            Fairy fairy = Fairy.create();
            TextProducer text = fairy.textProducer();
            storeOperations.postReview(i, "user_" + getRandomUserId, "password_" + getRandomUserId, (long) getRandomNumber(0, 9999), getRandomNumber(1, 5), text.paragraph(getRandomNumber(1, 5)), dateFormat.format(date));
        }
    }

    public MongoClient getConnectionClient() {
        Properties dbConnectionProp = new Properties();
        FileInputStream dbPropFile = null;
        MongoClient mongo = null;
        try {
            dbPropFile = new FileInputStream("properties/db.properties");
            dbConnectionProp.load(dbPropFile);
            dbPropFile.close();
            mongo = new MongoClient(dbConnectionProp.getProperty("host"), Integer.parseInt(dbConnectionProp.getProperty("port")));
            // Creating Credentials
            MongoCredential credential;
            credential = MongoCredential.createCredential(dbConnectionProp.getProperty("username"), "cart",
                    dbConnectionProp.getProperty("password").toCharArray());

        } catch (FileNotFoundException e) {
            System.out.println("Database connection config file not found");
        } catch (IOException ie) {
            System.out.println("There was I/O error while reading db config file, please check the config file and try again");
        } catch (NumberFormatException e) {
            System.out.println("There was an error in getting the port no for the db. Please check config file");
        }
        return mongo;
    }

}
