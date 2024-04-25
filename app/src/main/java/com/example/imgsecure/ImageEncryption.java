package com.example.imgsecure;
import android.graphics.Bitmap;
import android.graphics.Color;

public class ImageEncryption {

    public static Bitmap encryptText(Bitmap image, String text) {
        // Convert the text to binary
        String binaryText = stringToBinary(text);

        // Check if the image can accommodate the text and its length
        int imageCapacity = image.getWidth() * image.getHeight() * 3; // 3 bytes per pixel (RGB)
        int textLength = binaryText.length();

        // Calculate the maximum length that can be stored in the image
        int maxTextLength = imageCapacity - 32; // Reserve 32 bits (4 bytes) to store the text length

        if (textLength > maxTextLength) {
            throw new IllegalArgumentException("Text is too long to be encrypted within the image.");
        }

        // Create a mutable copy of the image
        Bitmap encryptedImage = image.copy(Bitmap.Config.ARGB_8888, true);

        // Convert the text length to binary
        String binaryLength = Integer.toBinaryString(textLength);
        binaryLength = String.format("%32s", binaryLength).replace(' ', '0'); // Pad with leading zeros to ensure 32 bits

        // Hide the text length in the image
        int lengthIndex = 0;
        for (int i = 0; i < 32; i++) { // 32 bits for the text length
            int pixel = encryptedImage.getPixel(i, 0);
            int alpha = Color.alpha(pixel);
            int red = Color.red(pixel);
            int green = Color.green(pixel);
            int blue = Color.blue(pixel);

            // Modify the LSB of each color channel with the text length bit
            if (lengthIndex < 32) {
                char bit = binaryLength.charAt(lengthIndex);
                red = modifyLSB(red, bit);
                lengthIndex++;
            }

            // Update the pixel color with the modified LSB
            pixel = Color.argb(alpha, red, green, blue);
            encryptedImage.setPixel(i, 0, pixel);
        }


        // Hide the text within the image
        int textIndex = 0;
        for (int y = 1; y < encryptedImage.getHeight(); y++) { // Start from the second row
            for (int x = 0; x < encryptedImage.getWidth(); x++) {
                // Get the pixel color
                int pixel = encryptedImage.getPixel(x, y);

                // Extract the color channels
                int red = Color.red(pixel);
                int green = Color.green(pixel);
                int blue = Color.blue(pixel);

                // Modify the LSB of each color channel with the text bit
                if (textIndex < textLength) {
                    // Get the next bit from the binary text
                    char bit = binaryText.charAt(textIndex);

                    // Modify the LSB of each color channel
                    red = modifyLSB(red, bit);
                    green = modifyLSB(green, bit);
                    blue = modifyLSB(blue, bit);

                    // Update the pixel color
                    pixel = Color.rgb(red, green, blue);

                    // Move to the next bit
                    textIndex++;
                }

                // Set the modified pixel color in the image
                encryptedImage.setPixel(x, y, pixel);
            }
        }

        return encryptedImage;
    }

    public static String decryptText(Bitmap image) {
        // Retrieve the length of the text from the image
        StringBuilder binaryLength = new StringBuilder();
        for (int i = 0; i < 32; i++) { // 4 bytes for the text length
            int pixel = image.getPixel(i, 0);
            int red = Color.red(pixel);

            // Extract the LSB of the red channel
            int bit = getLSB(red);
            binaryLength.append(bit);
        }

        // Convert the binary length to integer
        int textLength = Integer.parseInt(binaryLength.toString(), 2);

        // Check if the image contains enough data to retrieve the text
        int imageCapacity = (image.getWidth() * image.getHeight() - 1) * 3; // Exclude the first row used for text length (RGB)
        int maxTextLength = imageCapacity / 8;

        if (textLength > maxTextLength) {
            throw new IllegalArgumentException("Image does not contain enough encrypted text.");
        }

        // Retrieve the hidden text from the image
        StringBuilder binaryText = new StringBuilder();
        int textIndex = 0;
        for (int y = 1; y < image.getHeight(); y++) { // Start from the second row
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getPixel(x, y);

                // Extract the color channels
                int red = Color.red(pixel);

                // Extract the LSB of each color channel
                int bitRed = getLSB(red);

                // Append the extracted bits to the binary text
                binaryText.append(bitRed);

                // Break the loop if we have retrieved the entire text
                textIndex += 1;
                if (textIndex >= textLength) {
                    break;
                }
            }

            // Break the loop if we have retrieved the entire text
            if (textIndex >= textLength) {
                break;
            }
        }

        // Convert the binary text to string
        String decryptedText = binaryToString(binaryText.toString());
        return decryptedText;
    }


    // Utility function to convert a string to binary
    private static String stringToBinary(String text) {
        StringBuilder binaryText = new StringBuilder();
        for (char c : text.toCharArray()) {
            String binaryChar = Integer.toBinaryString(c);
            binaryText.append(String.format("%8s", binaryChar).replace(' ', '0'));
        }
        return binaryText.toString();
    }

    // Utility function to convert binary to a string
    private static String binaryToString(String binaryText) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < binaryText.length(); i += 8) {
            String binaryChar = binaryText.substring(i, i + 8);
            try {
                int charCode = Integer.parseInt(binaryChar, 2);
                text.append((char) charCode);
            } catch (NumberFormatException e) {
                //e.printStackTrace();
            }
        }
        return text.toString();
    }

    // Utility function to modify the least significant bit of a value
    private static int  modifyLSB(int value, char bit) {
        // Clear the least significant bit
        value = value & 0xFE;
        // Set the least significant bit to the desired value
        value = value | (bit - '0');
        return value;
    }

    // Utility function to get the least significant bit of a value
    private static int getLSB(int value) {
        return  (value & 0x01);
    }
}
