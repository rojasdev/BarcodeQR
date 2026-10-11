package com.rhix.barcodeqr.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.rhix.barcodeqr.R;

import java.util.EnumMap;
import java.util.Map;

public class NativeBarcodeFragment extends Fragment {

    private EditText editValue;
    private ImageView imageBarcode;
    private TextView textResult;
    private Button buttonGenerate;

    public NativeBarcodeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_native_barcode,
                container,
                false
        );

        editValue = view.findViewById(R.id.edit_native_barcode_value);
        imageBarcode = view.findViewById(R.id.image_native_barcode);
        textResult = view.findViewById(R.id.text_native_barcode_result);
        buttonGenerate = view.findViewById(R.id.button_native_barcode);

        buttonGenerate.setOnClickListener(v -> generateBarcode());

        return view;
    }

    private void generateBarcode() {
        String value = editValue.getText().toString().trim();

        if (TextUtils.isEmpty(value)) {
            editValue.setError("Please enter a barcode value.");
            return;
        }

        try {
            int width = 1000;
            int height = 150;

            Map<EncodeHintType, Object> hints =
                    new EnumMap<>(EncodeHintType.class);

            hints.put(EncodeHintType.MARGIN, 10);

            BitMatrix matrix = new MultiFormatWriter().encode(
                    value,
                    BarcodeFormat.CODE_128,
                    width,
                    height,
                    hints
            );

            Bitmap bitmap = Bitmap.createBitmap(
                    width,
                    height,
                    Bitmap.Config.ARGB_8888
            );

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(
                            x,
                            y,
                            matrix.get(x, y)
                                    ? android.graphics.Color.BLACK
                                    : android.graphics.Color.WHITE
                    );
                }
            }

            imageBarcode.setImageBitmap(bitmap);

            textResult.setText(
                    "Barcode generated successfully.\n\nValue: " + value
            );

        } catch (WriterException e) {
            textResult.setText(
                    "Unable to generate barcode: " + e.getMessage()
            );
        }
    }
}
