package org.svuonline.f18sales.salesmen.management;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import org.svuonline.f18sales.R;
import org.svuonline.f18sales.data.DatabaseHelper;
import org.svuonline.f18sales.model.Region;
import org.svuonline.f18sales.model.Salesman;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import static android.text.TextUtils.isEmpty;

public class ModifySalesmanFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private static final String TAG = ModifySalesmanFragment.class.getName();
    private static final int GALLERY_REQUEST_CODE = 406;
    private static final String IMAGE_PREFIX = "image_";
    private DatabaseHelper dbHelper;

    private Spinner spinnerSalesmen;
    private ImageView imageView;
    private FloatingActionButton fabUploadImage;
    private EditText editTextSalesmanId;
    private EditText editTextFullName;
    private Spinner spinnerRegions;
    private EditText editTextHiringDate;
    private Button buttonModifySalesman;

    public ModifySalesmanFragment() {
    }

    public static ModifySalesmanFragment newInstance() {
        return new ModifySalesmanFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_modify_salesman, container, false);

        dbHelper = new DatabaseHelper(getContext());

        initElements(v);
        setImageUploadOnClickListener(v);
        fillRegionsSpinnerWithData();
        fillSalesmenSpinnerWithData();

        spinnerSalesmen.setOnItemSelectedListener(this);
        setModifySalesmanButtonOnClickListener();
        return v;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            refreshPageContent();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result code is RESULT_OK only if the user selects an Image
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY_REQUEST_CODE) {
                //data.getData returns the content URI for the selected Image
                Uri selectedImage = data.getData();
                imageView.setImageURI(selectedImage);
            }
        }
    }

    private void initElements(View v) {
        spinnerSalesmen = v.findViewById(R.id.spinner_modify_salesmen);
        imageView = v.findViewById(R.id.image);
        fabUploadImage = v.findViewById(R.id.fab_upload_image);
        editTextSalesmanId = v.findViewById(R.id.editText_salesman_id);
        editTextFullName = v.findViewById(R.id.editText_full_name);
        spinnerRegions = v.findViewById(R.id.spinner_region);
        editTextHiringDate = v.findViewById(R.id.editText_hiring_date);
        buttonModifySalesman = v.findViewById(R.id.button_modify_salesman);
    }

    private void setImageUploadOnClickListener(View v) {
        fabUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // selectImageFromGallery
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                String[] mimeTypes = {"image/jpeg", "image/png"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                startActivityForResult(intent, GALLERY_REQUEST_CODE);
            }
        });
    }

    private void fillRegionsSpinnerWithData() {
        ArrayList<Region> regionsList = dbHelper.getAllRegions();
        RegionSpinnerArrayAdapter regionsAdapter = new RegionSpinnerArrayAdapter(getContext(), regionsList);
        spinnerRegions.setAdapter(regionsAdapter);
    }

    private void fillSalesmenSpinnerWithData() {
        SalesmenSpinnerArrayAdapter salesmenAdapter = new SalesmenSpinnerArrayAdapter(getContext(), dbHelper.getAllSalesmen());
        spinnerSalesmen.setAdapter(salesmenAdapter);
    }

    private void setModifySalesmanButtonOnClickListener() {
        buttonModifySalesman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValid()) {
                    boolean modified = modifySalesman();
                    if (modified) {
                        showMessage("Success", "Salesman was successfully modified in the database.");
                        refreshPageContent();
                    } else {
                        showMessage("Failure", "Failed modifying salesman in the database.");
                    }
                } else {
                    Toast.makeText(getContext(), "Please fill all fields correctly including the image!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean isValid() {
        // regions has a default value (always valid)
        // Hiring date editText is disabled --> its value will not change (always valid)
        // editTextSalesmanId has android:inputType="number" --> no need to do an extra check for an integer
        return spinnerSalesmen.getCount() > 0 &&
                imageView.getDrawable() != null &&
                !isEmpty(editTextSalesmanId.getText().toString()) &&
                !isEmpty(editTextFullName.getText().toString());
    }

    private boolean modifySalesman() {
        try {
            // `id` and `newId` are needed in the sql UPDATE command
            Integer id = ((Salesman) spinnerSalesmen.getSelectedItem()).getId();
            Integer newId = Integer.valueOf(editTextSalesmanId.getText().toString());
            String fullName = editTextFullName.getText().toString();
            Region region = (Region) spinnerRegions.getSelectedItem();
            String hiringDate = editTextHiringDate.getText().toString();
            String imagePath = saveImageInFilesDirectory(fullName);
            if (imagePath != null) {
                Salesman salesman = new Salesman(id, fullName, region.getId(), hiringDate, imagePath, newId);
                return dbHelper.updateSalesman(salesman) != -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private String saveImageInFilesDirectory(String username) {
        try {
            Bitmap imageBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            // save image
            File imageFile = new File(getContext().getFilesDir(), IMAGE_PREFIX + username);
            FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 70, fileOutputStream);
            // return image path if we success to save the image
            return imageFile.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void refreshPageContent() {
        resetPage();
        fillRegionsSpinnerWithData();
        fillSalesmenSpinnerWithData();
    }

    private void resetPage() {
        imageView.setImageDrawable(null);
        editTextSalesmanId.setText("");
        editTextFullName.setText("");
        editTextHiringDate.setText("hiring date");
    }

    private void showMessage(String title, String Message) {
        new AlertDialog.Builder(getContext())
                .setCancelable(true)
                .setTitle(title)
                .setMessage(Message)
                .show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Salesman salesman = (Salesman) parent.getItemAtPosition(position);
        editTextFullName.setText(salesman.getFullName());
        editTextHiringDate.setText(salesman.getHiringDate());
        editTextSalesmanId.setText(salesman.getId().toString());
        // read image and show it on the UI
        File imgFile = new File(salesman.getImagePath());
        if (imgFile.exists()) {
            Bitmap image = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imageView.setImageBitmap(image);
        }
        spinnerRegions.setSelection(salesman.getRegionId() - 1);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // do nothing
    }
}
