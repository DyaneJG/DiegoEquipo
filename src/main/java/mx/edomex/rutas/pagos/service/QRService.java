package mx.edomex.rutas.pagos.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

/*
 * Genera imágenes QR.
 */
@Service
public class QRService {

    /*
     * Devuelve un QR en Base64.
     */
    public String generarQR(String contenido) {

        try {

            QRCodeWriter writer = new QRCodeWriter();

            var matrix = writer.encode(
                    contenido,
                    BarcodeFormat.QR_CODE,
                    350,
                    350
            );

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            MatrixToImageWriter.writeToStream(
                    matrix,
                    "PNG",
                    output
            );

            return Base64.getEncoder()
                    .encodeToString(output.toByteArray());

        } catch (Exception e) {

            throw new RuntimeException(
                    "No se pudo generar el QR."
            );

        }

    }

}