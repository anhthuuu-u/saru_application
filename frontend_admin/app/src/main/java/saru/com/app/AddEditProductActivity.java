package saru.com.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import saru.com.models.Brand;
import saru.com.models.Category;
import saru.com.models.Product;
import saru.com.models.ProductStatus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AddEditProductActivity extends AppCompatActivity {
    private static final String TAG = "AddEditProductActivity";
    private EditText edtProductID, edtProductName, edtCustomerRating, edtIngredients, edtNetContent,
            edtProductDescription, edtProductPrice, edtWineType, edtAlcoholStrength, edtProductTaste;
    private CheckBox chkBestSelling, chkOnSale;
    private Spinner spinnerCateID, spinnerBrandID, spinnerProductStatus;
    private Button btnSelectImage, btnSave, btnCancel;
    private ImageView imgCover;
    private ProgressBar progressBar;
    private Product product;
    private FirebaseFirestore db;
    private StorageReference storageReference;
    private List<Category> categories;
    private List<Brand> brands;
    private List<ProductStatus> productStatuses;
    private String selectedCategoryId, selectedBrandId, selectedStatusId;
    private List<Uri> selectedImageUris = new ArrayList<>();
    private static final int MAX_IMAGES = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_product);
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("product_images");
        initializeViews();
        setupToolbar();
        setupEvents();
        loadInitialData();
    }

    private void initializeViews() {
        edtProductID = findViewById(R.id.edtProductID);
        edtProductName = findViewById(R.id.edtProductName);
        edtCustomerRating = findViewById(R.id.edtCustomerRating);
        edtIngredients = findViewById(R.id.edtIngredients);
        edtNetContent = findViewById(R.id.edtNetContent);
        edtProductDescription = findViewById(R.id.edtProductDescription);
        edtProductPrice = findViewById(R.id.edtProductPrice);
        edtWineType = findViewById(R.id.edtWineType);
        edtAlcoholStrength = findViewById(R.id.edtAlcoholStrength);
        edtProductTaste = findViewById(R.id.edtProductTaste);
        chkBestSelling = findViewById(R.id.chkBestSelling);
        chkOnSale = findViewById(R.id.chkOnSale);
        spinnerCateID = findViewById(R.id.spinnerCateID);
        spinnerBrandID = findViewById(R.id.spinnerBrandID);
        spinnerProductStatus = findViewById(R.id.spinnerProductStatus);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        imgCover = findViewById(R.id.imgCover);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(product == null ? "Add Product" : "Edit Product");
        }
    }

    private void setupEvents() {
        btnSelectImage.setOnClickListener(v -> {
            if (selectedImageUris.size() >= MAX_IMAGES) {
                showToast("You can only select up to " + MAX_IMAGES + " images.");
                return;
            }
            selectImageLauncher.launch("image/*");
        });
        btnSave.setOnClickListener(v -> saveProduct());
        btnCancel.setOnClickListener(v -> finish());
    }

    private final ActivityResultLauncher<String> selectImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetMultipleContents(),
            uris -> {
                if (uris != null && !uris.isEmpty()) {
                    int remainingSlots = MAX_IMAGES - selectedImageUris.size();
                    int imagesToAdd = Math.min(uris.size(), remainingSlots);
                    for (int i = 0; i < imagesToAdd; i++) {
                        selectedImageUris.add(uris.get(i));
                    }
                    if (!selectedImageUris.isEmpty()) {
                        Glide.with(this).load(selectedImageUris.get(0)).into(imgCover);
                    }
                    showToast("Selected " + selectedImageUris.size() + " image(s).");
                }
            });

    private void loadInitialData() {
        categories = new ArrayList<>();
        brands = new ArrayList<>();
        productStatuses = new ArrayList<>();
        loadCategories();
    }

    private void loadCategories() {
        db.collection("productCategory").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Category category = document.toObject(Category.class);
                            category.setCateID(document.getId());
                            categories.add(category);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing category: " + document.getId(), e);
                        }
                    }
                    setupCategorySpinner();
                    loadBrands();
                })
                .addOnFailureListener(e -> showToast("Failed to load categories: " + e.getMessage()));
    }

    private void loadBrands() {
        db.collection("productBrand").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Brand brand = document.toObject(Brand.class);
                            brand.setBrandID(document.getId());
                            brands.add(brand);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing brand: " + document.getId(), e);
                        }
                    }
                    setupBrandSpinner();
                    loadProductStatuses();
                })
                .addOnFailureListener(e -> showToast("Failed to load brands: " + e.getMessage()));
    }

    private void loadProductStatuses() {
        db.collection("productStatus").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            ProductStatus status = document.toObject(ProductStatus.class);
                            status.setProductStatusID(document.getId());
                            productStatuses.add(status);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing status: " + document.getId(), e);
                        }
                    }
                    setupStatusSpinner();
                    displayProduct();
                })
                .addOnFailureListener(e -> showToast("Failed to load product statuses: " + e.getMessage()));
    }

    private void setupCategorySpinner() {
        List<String> categoryNames = new ArrayList<>();
        for (Category category : categories) {
            categoryNames.add(category.getCateName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCateID.setAdapter(adapter);
        spinnerCateID.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedCategoryId = categories.get(position).getCateID();
                Log.d(TAG, "Selected Category ID: " + selectedCategoryId);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedCategoryId = null;
            }
        });
    }

    private void setupBrandSpinner() {
        List<String> brandNames = new ArrayList<>();
        for (Brand brand : brands) {
            brandNames.add(brand.getBrandName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, brandNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBrandID.setAdapter(adapter);
        spinnerBrandID.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedBrandId = brands.get(position).getBrandID();
                Log.d(TAG, "Selected Brand ID: " + selectedBrandId);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedBrandId = null;
            }
        });
    }

    private void setupStatusSpinner() {
        ArrayAdapter<ProductStatus> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, productStatuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProductStatus.setAdapter(adapter);
        spinnerProductStatus.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedStatusId = productStatuses.get(position).getProductStatusID();
                Log.d(TAG, "Selected Status ID: " + selectedStatusId);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedStatusId = null;
            }
        });
    }

    private void displayProduct() {
        product = (Product) getIntent().getSerializableExtra("SELECTED_PRODUCT");
        if (product != null) {
            edtProductID.setText(product.getProductID());
            edtProductName.setText(product.getProductName());
            edtCustomerRating.setText(String.valueOf(product.getCustomerRating()));
            edtIngredients.setText(product.getIngredients());
            edtNetContent.setText(product.getNetContent());
            edtProductDescription.setText(product.getProductDescription());
            edtProductPrice.setText(String.valueOf(product.getProductPrice()));
            edtWineType.setText(product.getWineType());
            edtAlcoholStrength.setText(product.getAlcoholStrength());
            edtProductTaste.setText(product.getProductTaste());
            chkBestSelling.setChecked(product.isBestSelling());
            chkOnSale.setChecked(product.isOnSale());
            if (product.getProductImageCover() != null && !product.getProductImageCover().isEmpty()) {
                Glide.with(this).load(product.getProductImageCover()).into(imgCover);
            }
            if (product.getCateID() != null) {
                for (int i = 0; i < categories.size(); i++) {
                    if (categories.get(i).getCateID().equals(product.getCateID())) {
                        spinnerCateID.setSelection(i);
                        selectedCategoryId = categories.get(i).getCateID();
                        break;
                    }
                }
            }
            if (product.getBrandID() != null) {
                for (int i = 0; i < brands.size(); i++) {
                    if (brands.get(i).getBrandID().equals(product.getBrandID())) {
                        spinnerBrandID.setSelection(i);
                        selectedBrandId = brands.get(i).getBrandID();
                        break;
                    }
                }
            }
            if (product.getProductStatusID() != null) {
                for (int i = 0; i < productStatuses.size(); i++) {
                    if (productStatuses.get(i).getProductStatusID().equals(product.getProductStatusID())) {
                        spinnerProductStatus.setSelection(i);
                        selectedStatusId = productStatuses.get(i).getProductStatusID();
                        break;
                    }
                }
            }
        }
    }

    private void saveProduct() {
        String productID = edtProductID.getText().toString().trim();
        String productName = edtProductName.getText().toString().trim();
        String customerRatingStr = edtCustomerRating.getText().toString().trim();
        String ingredients = edtIngredients.getText().toString().trim();
        String netContent = edtNetContent.getText().toString().trim();
        String productDescription = edtProductDescription.getText().toString().trim();
        String productPriceStr = edtProductPrice.getText().toString().trim();
        String wineType = edtWineType.getText().toString().trim();
        String alcoholStrength = edtAlcoholStrength.getText().toString().trim();
        String productTaste = edtProductTaste.getText().toString().trim();
        boolean isBestSelling = chkBestSelling.isChecked();
        boolean isOnSale = chkOnSale.isChecked();

        if (productName.isEmpty()) {
            showToast("Please enter Product Name");
            return;
        }
        if (productDescription.isEmpty()) {
            showToast("Please enter Product Description");
            return;
        }
        if (productPriceStr.isEmpty()) {
            showToast("Please enter Product Price");
            return;
        }
        if (selectedCategoryId == null) {
            showToast("Please select a Category");
            return;
        }
        if (selectedBrandId == null) {
            showToast("Please select a Brand");
            return;
        }
        if (selectedStatusId == null && !productStatuses.isEmpty()) {
            selectedStatusId = productStatuses.get(0).getProductStatusID();
        }
        if (selectedImageUris.isEmpty() && (product == null || product.getProductImageCover() == null)) {
            showToast("Please select at least one image for the product");
            return;
        }

        double customerRating;
        try {
            customerRating = customerRatingStr.isEmpty() ? 0.0 : Double.parseDouble(customerRatingStr);
        } catch (NumberFormatException e) {
            showToast("Invalid Customer Rating format");
            return;
        }

        double productPrice;
        try {
            productPrice = Double.parseDouble(productPriceStr);
        } catch (NumberFormatException e) {
            showToast("Invalid Product Price format");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        if (!selectedImageUris.isEmpty()) {
            uploadImagesAndSaveProduct(productID, productName, customerRating, ingredients, netContent,
                    productDescription, productPrice, wineType, alcoholStrength, productTaste,
                    isBestSelling, isOnSale);
        } else {
            String cover = product != null ? product.getProductImageCover() : null;
            String sub1 = product != null ? product.getProductImageSub1() : null;
            String sub2 = product != null ? product.getProductImageSub2() : null;
            String imageId = product != null ? product.getImageID() : null;
            saveProductData(productID, productName, customerRating, ingredients, netContent,
                    productDescription, productPrice, wineType, alcoholStrength, productTaste,
                    isBestSelling, isOnSale, cover, sub1, sub2, imageId);
        }
    }

    private void uploadImagesAndSaveProduct(String productID, String productName, double customerRating,
                                            String ingredients, String netContent, String productDescription,
                                            double productPrice, String wineType, String alcoholStrength,
                                            String productTaste, boolean isBestSelling, boolean isOnSale) {
        List<Task<Uri>> uploadTasks = new ArrayList<>();
        List<String> imageUrls = new ArrayList<>();

        for (Uri imageUri : selectedImageUris) {
            final StorageReference fileRef = storageReference.child(UUID.randomUUID().toString() + ".jpg");
            UploadTask uploadTask = fileRef.putFile(imageUri);
            Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileRef.getDownloadUrl();
            });
            uploadTasks.add(urlTask);
        }

        Tasks.whenAllSuccess(uploadTasks).addOnSuccessListener(results -> {
            for (Object result : results) {
                imageUrls.add(result.toString());
            }

            String coverUrl = imageUrls.size() > 0 ? imageUrls.get(0) : (product != null ? product.getProductImageCover() : null);
            String sub1Url = imageUrls.size() > 1 ? imageUrls.get(1) : (product != null ? product.getProductImageSub1() : null);
            String sub2Url = imageUrls.size() > 2 ? imageUrls.get(2) : (product != null ? product.getProductImageSub2() : null);

            saveImageDocument(coverUrl, sub1Url, sub2Url, productID, productName, customerRating, ingredients,
                    netContent, productDescription, productPrice, wineType, alcoholStrength, productTaste,
                    isBestSelling, isOnSale);
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            showToast("Failed to upload images: " + e.getMessage());
        });
    }

    private void saveImageDocument(String coverUrl, String sub1Url, String sub2Url, String productID,
                                   String productName, double customerRating, String ingredients,
                                   String netContent, String productDescription, double productPrice,
                                   String wineType, String alcoholStrength, String productTaste,
                                   boolean isBestSelling, boolean isOnSale) {
        Map<String, Object> imageData = new HashMap<>();
        imageData.put("ProductImageCover", coverUrl);
        imageData.put("ProductImageSub1", sub1Url);
        imageData.put("ProductImageSub2", sub2Url);

        String newImageId = (product != null && product.getImageID() != null) ? product.getImageID() : db.collection("image").document().getId();

        db.collection("image").document(newImageId).set(imageData)
                .addOnSuccessListener(aVoid -> saveProductData(productID, productName, customerRating, ingredients,
                        netContent, productDescription, productPrice, wineType, alcoholStrength, productTaste,
                        isBestSelling, isOnSale, coverUrl, sub1Url, sub2Url, newImageId))
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    showToast("Failed to save image data: " + e.getMessage());
                });
    }

    private void saveProductData(String productID, String productName, double customerRating, String ingredients,
                                 String netContent, String productDescription, double productPrice,
                                 String wineType, String alcoholStrength, String productTaste,
                                 boolean isBestSelling, boolean isOnSale, String coverUrl, String sub1Url,
                                 String sub2Url, String imageId) {
        if (productID.isEmpty()) {
            productID = db.collection("products").document().getId();
        }

        Product productToSave = new Product();
        productToSave.setProductID(productID);
        productToSave.setProductName(productName);
        productToSave.setCateID(selectedCategoryId);
        productToSave.setBrandID(selectedBrandId);
        productToSave.setCustomerRating(customerRating);
        productToSave.setProductImageCover(coverUrl);
        productToSave.setIngredients(ingredients);
        productToSave.setBestSelling(isBestSelling);
        productToSave.setOnSale(isOnSale);
        productToSave.setNetContent(netContent);
        productToSave.setProductDescription(productDescription);
        productToSave.setProductPrice(productPrice);
        productToSave.setProductStatusID(selectedStatusId);
        productToSave.setWineType(wineType);
        productToSave.setAlcoholStrength(alcoholStrength);
        productToSave.setProductTaste(productTaste);
        productToSave.setImageID(imageId);
        productToSave.setProductImageSub1(sub1Url);
        productToSave.setProductImageSub2(sub2Url);

        db.collection("products").document(productID).set(productToSave)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    showToast("Product saved successfully!");
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    showToast("Failed to save product: " + e.getMessage());
                });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}