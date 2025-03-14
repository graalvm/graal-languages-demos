package org.example;

public interface QRCode {
    Promise toString(String data);

    Promise toDataURL(String data, Object options);
}