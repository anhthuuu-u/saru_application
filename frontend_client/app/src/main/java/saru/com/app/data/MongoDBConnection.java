package saru.com.app.data;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
public class MongoDBConnection {
    private static final String CONNECTION_STRING = "mongodb+srv://user1:ry3l1jwj1IJlM1fT@database.nkts1.mongodb.net/?retryWrites=true&w=majority&appName=Database";

    public static MongoClient getMongoClient() {
        return MongoClients.create(CONNECTION_STRING);
    }
}
