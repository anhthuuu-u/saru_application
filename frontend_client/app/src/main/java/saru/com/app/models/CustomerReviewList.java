package saru.com.app.models;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class CustomerReviewList {
    private final MutableLiveData<List<CustomerReviews>> reviewsLiveData;
    private final FirebaseFirestore db;

    public CustomerReviewList() {
        reviewsLiveData = new MutableLiveData<>();
        db = FirebaseFirestore.getInstance();
        loadReviewsFromFirestore();
    }

    private void loadReviewsFromFirestore() {
        db.collection("customerReview")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<CustomerReviews> reviews = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            CustomerReviews review = new CustomerReviews(
                                    document.getString("customerName"),
                                    document.getString("reviewContent"),
                                    document.getString("purchasedProduct"),
                                    document.getString("customerImage")
                            );
                            reviews.add(review);
                        }
                        reviewsLiveData.setValue(reviews);
                        Log.d("CustomerReviewList", "Loaded " + reviews.size() + " reviews from Firestore");
                    } else {
                        Log.e("CustomerReviewList", "Error loading reviews: ", task.getException());
                        reviewsLiveData.setValue(new ArrayList<>());
                    }
                });
    }

    public MutableLiveData<List<CustomerReviews>> getReviews() {
        return reviewsLiveData;
    }
}