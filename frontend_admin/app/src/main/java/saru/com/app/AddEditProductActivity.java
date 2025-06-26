package saru.com.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;
import saru.com.models.Brand;
import saru.com.models.Category;
import saru.com.models.Product;
import saru.com.app.R;

public class AddEditProductActivity extends AppCompatActivity {
    private EditText edtProductName, edtProductID, edtCustomerRating, edtIngredients, edtNetContent,
            edtProductDescription, edtProductPrice, edtProductStatusID, edtWineType, edtAlcoholStrength, edtProductTaste;
    private Spinner spinnerCateID, spinnerBrandID;
    private CheckBox chkBestSelling, chkOnSale;
    private Button btnSelectImage, btnSave, btnCancel;
    private ImageView imgProduct;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Uri imageUri;
    private Product product;
    private List<Category> categories;
    private List<Brand> brands;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_product);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        initializeViews();
        setupSpinners();
        checkForEdit();
        setupEvents();
    }

    private void initializeViews() {
        edtProductName = findViewById(R.id.edtProductName);
        edtProductID = findViewById(R.id.edtProductID);
        edtCustomerRating = findViewById(R.id.edtCustomerRating);
        edtIngredients = findViewById(R.id.edtIngredients);
        edtNetContent = findViewById(R.id.edtNetContent);
        edtProductDescription = findViewById(R.id.edtProductDescription);
        edtProductPrice = findViewById(R.id.edtProductPrice);
        edtProductStatusID = findViewById(R.id.edtProductStatusID);
        edtWineType = findViewById(R.id.edtWineType);
        edtAlcoholStrength = findViewById(R.id.edtAlcoholStrength);
        edtProductTaste = findViewById(R.id.edtProductTaste);
        spinnerCateID = findViewById(R.id.spinnerCateID);
        spinnerBrandID = findViewById(R.id.spinnerBrandID);
        chkBestSelling = findViewById(R.id.chkBestSelling);
        chkOnSale = findViewById(R.id.chkOnSale);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        imgProduct = findViewById(R.id.imgProduct);
    }

    private void setupSpinners() {
        // Categories
        categories = new ArrayList<>();
        ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCateID.setAdapter(categoryAdapter);

        db.collection("productCategory").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Category category = document.toObject(Category.class);
                        category.setCateID(document.getId());
                        categories.add(category);
                    }
                    categoryAdapter.notifyDataSetChanged();
                    checkForEdit(); // Re-run to set correct category
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load categories", Toast.LENGTH_SHORT).show());

        // Brands
        brands = new ArrayList<>();
        ArrayAdapter<Brand> brandAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, brands);
        brandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBrandID.setAdapter(brandAdapter);

        db.collection("productBrand").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Brand brand = document.toObject(Brand.class);
                        brand.setBrandID(document.getId());
                        brands.add(brand);
                    }
                    brandAdapter.notifyDataSetChanged();
                    checkForEdit(); // Re-run to set correct brand
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load brands", Toast.LENGTH_SHORT).show());
    }

    private void checkForEdit() {
        product = (Product) getIntent().getSerializableExtra("PRODUCT");
        if (product != null) {
            edtProductName.setText(product.getProductName());
            edtProductID.setText(product.getProductID());
            edtCustomerRating.setText(String.valueOf(product.getCustomerRating()));
            edtIngredients.setText(product.getIngredients());
            edtNetContent.setText(product.getNetContent());
            edtProductDescription.setText(product.getProductDescription());
            edtProductPrice.setText(String.valueOf(product.getProductPrice()));
            edtProductStatusID.setText(product.getProductStatusID());
            edtWineType.setText(product.getWineType());
            edtAlcoholStrength.setText(product.getAlcoholStrength());
            edtProductTaste.setText(product.getProductTaste());
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).getCateID().equals(product.getCateID())) {
                    spinnerCateID.setSelection(i);
                    break;
                }
            }
            for (int i = 0; i < brands.size(); i++) {
                if (brands.get(i).getBrandID().equals(product.getBrandID())) {
                    spinnerBrandID.setSelection(i);
                    break;
                }
            }
            chkBestSelling.setChecked(product.isBestSelling());
            chkOnSale.setChecked(product.isOnSale());
            if (product.getImageID() != null && !product.getImageID().isEmpty()) {
                Picasso.get().load(product.getImageID()).into(imgProduct);
            } else {
                imgProduct.setImageResource(R.drawable.ic_placeholder);
            }
        }
    }

    private void setupEvents() {
        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        btnSave.setOnClickListener(v -> saveProduct());
        btnCancel.setOnClickListener(v -> finish());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imgProduct.setImageURI(imageUri);
        }
    }

    private void saveProduct() {
        String productName = edtProductName.getText().toString().trim();
        String productID = edtProductID.getText().toString().trim();
        String customerRatingStr = edtCustomerRating.getText().toString().trim();
        String ingredients = edtIngredients.getText().toString().trim();
        String netContent = edtNetContent.getText().toString().trim();
        String productDescription = edtProductDescription.getText().toString().trim();
        String productPriceStr = edtProductPrice.getText().toString().trim();
        String productStatusID = edtProductStatusID.getText().toString().trim();
        String wineType = edtWineType.getText().toString().trim();
        String alcoholStrength = edtAlcoholStrength.getText().toString().trim();
        String productTaste = edtProductTaste.getText().toString().trim();
        String cateID = spinnerCateID.getSelectedItem() != null ? ((Category) spinnerCateID.getSelectedItem()).getCateID() : "";
        String brandID = spinnerBrandID.getSelectedItem() != null ? ((Brand) spinnerBrandID.getSelectedItem()).getBrandID() : "";
        boolean isBestSelling = chkBestSelling.isChecked();
        boolean isOnSale = chkOnSale.isChecked();

        if (productName.isEmpty() || productID.isEmpty() || productPriceStr.isEmpty() || cateID.isEmpty() || brandID.isEmpty()) {
            Toast.makeText(this, "Please fill required fields (Name, ID, Price, Category, Brand)", Toast.LENGTH_SHORT).show();
            return;
        }

        double productPrice, customerRating;
        try {
            productPrice = Double.parseDouble(productPriceStr);
            customerRating = customerRatingStr.isEmpty() ? 0.0 : Double.parseDouble(customerRatingStr);
            if (customerRating < 0 || customerRating > 5) {
                Toast.makeText(this, "Rating must be between 0 and 5", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price or rating", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri != null) {
            uploadImageAndSaveProduct(productID, productName, cateID, brandID, customerRating, ingredients,
                    isBestSelling, isOnSale, netContent, productDescription, productPrice, productStatusID, wineType,
                    alcoholStrength, productTaste);
        } else {
            saveProductToFirestore(productID, productName, cateID, brandID, customerRating,
                    product != null ? product.getImageID() : null, ingredients, isBestSelling, isOnSale, netContent,
                    productDescription, productPrice, productStatusID, wineType, alcoholStrength, productTaste);
        }
    }

    private void uploadImageAndSaveProduct(String productID, String productName, String cateID, String brandID,
                                           double customerRating, String ingredients, boolean isBestSelling,
                                           boolean isOnSale, String netContent, String productDescription,
                                           double productPrice, String productStatusID, String wineType,
                                           String alcoholStrength, String productTaste) {
        StorageReference storageRef = storage.getReference().child("products/" + productID + ".jpg");
        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveProductToFirestore(productID, productName, cateID, brandID, customerRating, uri.toString(),
                            ingredients, isBestSelling, isOnSale, netContent, productDescription, productPrice,
                            productStatusID, wineType, alcoholStrength, productTaste);
                }))
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show());
    }

    private void saveProductToFirestore(String productID, String productName, String cateID, String brandID,
                                        double customerRating, String imageID, String ingredients, boolean isBestSelling,
                                        boolean isOnSale, String netContent, String productDescription,
                                        double productPrice, String productStatusID, String wineType,
                                        String alcoholStrength, String productTaste) {
        Product newProduct = new Product(
                productID, productName, cateID, brandID, customerRating, imageID, ingredients,
                isBestSelling, isOnSale, netContent, productDescription, productPrice, productStatusID,
                wineType.isEmpty() ? null : wineType, alcoholStrength.isEmpty() ? null : alcoholStrength,
                productTaste.isEmpty() ? null : productTaste
        );

        db.collection("products").document(productID).set(newProduct)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, product != null ? "Product updated" : "Product added", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save product", Toast.LENGTH_SHORT).show());
    }
}