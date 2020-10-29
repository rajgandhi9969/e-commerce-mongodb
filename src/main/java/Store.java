import com.devskiller.jfairy.Fairy;
import com.devskiller.jfairy.producer.person.Person;
import com.devskiller.jfairy.producer.text.TextProducer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

/***
 *
 * This is the main driver program which will use threads to use
 */

public class Store {
    // to track number of operations
    static int numberOfOperations = 0;

    public static void main(String[] args) {
        // flag to refresh with new data, passed via command line
        boolean refreshData = false;
        // number of threads to be executed. Passed via command line
        int threadPool = 5;
        boolean hasUserSpecifiedThreads = false;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("refreshdata")) {
                refreshData = true;
            } else {
                try {
                    threadPool = Integer.parseInt(args[i]);
                    hasUserSpecifiedThreads = true;
                } catch (Exception e) {
                }
            }
        }
        if (!hasUserSpecifiedThreads) {
            System.out.println("Number of threads not found in command line arguments, by default 5 threads will be running");
        } else {
            System.out.println("Number of threads to execute: " + threadPool);
        }
        // if data needs to be refreshed
        if (refreshData) {
            DatabaseFiller filler = new DatabaseFiller();
            filler.clearDB();
            filler.fillUsers(1000);
            filler.fillProducts(10000);
            filler.fillOrders(10000);
            filler.fillReviews(20000);
        }

        // Below callable methods are used in executor service. This calls methods from StoreAPI as per requirement
        Callable<Object> createAccount = () -> {
            StoreAPI storeOperations = new StoreAPIMongoImpl();
            Fairy fairy = Fairy.create();
            Person person = fairy.person();
            storeOperations.createAccount(person.getUsername(), person.getPassword(), person.getFirstName(), person.getLastName());
            numberOfOperations += 1;
            return null;
        };

        Callable<Object> addProduct = () -> {
            StoreAPI storeOperations = new StoreAPIMongoImpl();
            Fairy fairy = Fairy.create();
            TextProducer text = fairy.textProducer();
            storeOperations.addProduct(-1, text.sentence(getRandomNumber(1, 10)), text.paragraph(getRandomNumber(1, 50)), (float) getRandomNumber(1, 3000), getRandomNumber(1, 5000));
            numberOfOperations += 1;
            return null;
        };

        Callable<Object> updateStockLevel = () -> {
            StoreAPI storeOperations = new StoreAPIMongoImpl();
            storeOperations.updateStockLevel(getRandomNumber(0, 9999), getRandomNumber(0, 500));
            numberOfOperations += 1;
            return null;
        };

        Callable<Object> submitOrder = () -> {
            StoreAPI storeOperations = new StoreAPIMongoImpl();
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            int getRandomUserId = getRandomNumber(0, 999);
            storeOperations.submitOrder(dateFormat.format(date), "user_" + getRandomUserId, "password_" + getRandomUserId, getProductsToBeOrdered(getRandomNumber(1, 10)));
            numberOfOperations += 1;
            return null;
        };

        Callable<Object> postReviews = () -> {
            StoreAPI storeOperations = new StoreAPIMongoImpl();
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            int getRandomUserId = getRandomNumber(0, 999);
            Fairy fairy = Fairy.create();
            TextProducer text = fairy.textProducer();
            storeOperations.postReview(-1, "user_" + getRandomUserId, "password_" + getRandomUserId, (long) getRandomNumber(0, 9999), getRandomNumber(1, 5), text.paragraph(getRandomNumber(1, 5)), dateFormat.format(date));
            numberOfOperations += 1;
            return null;
        };

        Callable<Object> getProductandReviews = () -> {
            StoreAPI storeOperations = new StoreAPIMongoImpl();
            storeOperations.getProductAndReviews(getRandomNumber(0, 9998));
            numberOfOperations += 1;
            return null;
        };

        Callable<Object> getUserAverageReviews = () -> {
            StoreAPI storeOperations = new StoreAPIMongoImpl();
            storeOperations.getAverageUserRating("user_" + getRandomNumber(0, 9999));
            numberOfOperations += 1;
            return null;
        };


        ExecutorService executorService = Executors.newFixedThreadPool(threadPool);
        // minutes to run the service.
        long minToRun = 3;
        long start = System.currentTimeMillis();
        long end = start + (minToRun * 60 * 1000);
        // running for given number of minutes. Random API method is called based on provided probabilities
        while (System.currentTimeMillis() <= end) {
            int randomTask = getRandomNumber(1, 100);
            if (randomTask <= 65) {
                executorService.submit(getProductandReviews);
            } else if (randomTask > 65 && randomTask <= 75) {
                executorService.submit(updateStockLevel);
            } else if (randomTask > 75 && randomTask <= 85) {
                executorService.submit(submitOrder);
            } else if (randomTask > 85 && randomTask <= 90) {
                executorService.submit(postReviews);
            } else if (randomTask > 90 && randomTask <= 95) {
                executorService.submit(getUserAverageReviews);
            } else if (randomTask > 95 && randomTask <= 98) {
                executorService.submit(createAccount);
            } else {
                executorService.submit(addProduct);
            }
        }
        // shut the service are given number of mins are passed
        executorService.shutdownNow();
        while (!executorService.isTerminated()) {
            // wait for all the threads to terminate
        }
        // results
        StoreAPI storeOperations = new StoreAPIMongoImpl();
        System.out.println("************** Operations on all threads are completed. Below are the results **************");
        System.out.println("Total Operations: " + numberOfOperations);
        int missedData[]=storeOperations.getMissedProducts();
        System.out.println("Total products missed " + missedData[0]+" out of total products "+missedData[1]);
    }

    public static int getRandomNumber(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    // helper function which provides list of products and its quantities to be ordered
    public static HashMap<Long, Integer> getProductsToBeOrdered(int totalEntries) {
        HashMap<Long, Integer> listOfProducts = new HashMap<>();
        for (int i = 0; i < totalEntries; i++) {
            listOfProducts.put((long) getRandomNumber(0, 9999), getRandomNumber(1, 10));
        }
        return listOfProducts;
    }

}
