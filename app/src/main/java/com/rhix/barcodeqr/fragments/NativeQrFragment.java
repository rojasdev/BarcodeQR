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

public class NativeQrFragment extends Fragment {

    private EditText editValue;
    private ImageView imageQr;
    private TextView textResult;
    private Button buttonGenerate;

    public NativeQrFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_native_qr,
                container,
                false
        );

        editValue = view.findViewById(R.id.edit_native_qr_value);
        imageQr = view.findViewById(R.id.image_native_qr);
        textResult = view.findViewById(R.id.text_native_qr_result);
        buttonGenerate = view.findViewById(R.id.button_native_qr);

        buttonGenerate.setOnClickListener(v -> generateQrCode());

        return view;
    }

    private void generateQrCode() {
        String value = editValue.getText().toString().trim();

        if (TextUtils.isEmpty(value)) {
            editValue.setError("Please enter text or a URL.");
            return;
        }

        try {
            int size = 600;

            Map<EncodeHintType, Object> hints =
                    new EnumMap<>(EncodeHintType.class);

            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix matrix = new MultiFormatWriter().encode(
                    value,
                    BarcodeFormat.QR_CODE,
                    size,
                    size,
                    hints
            );

            Bitmap bitmap = Bitmap.createBitmap(
                    size,
                    size,
                    Bitmap.Config.ARGB_8888
            );

            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(
                            x,
                            y,
                            matrix.get(x, y)
                                    ? android.graphics.Color.BLACK
                                    : android.graphics.Color.WHITE
                    );
                }
            }

            imageQr.setImageBitmap(bitmap);
            textResult.setText(
                    "QR code generated successfully.\n\nContent: " + value
            );

        } catch (WriterException e) {
            textResult.setText("Unable to generate QR code: " + e.getMessage());
        }
    }
}
