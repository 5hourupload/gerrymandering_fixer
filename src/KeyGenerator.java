import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class KeyGenerator {

    public static void main(String[] args) {
        KeyGenerator keyGenerator = new KeyGenerator();

    }
    public KeyGenerator(){

        PrintWriter writer = null;
        try {
            writer = new PrintWriter("district_key.txt", "UTF-8");
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Image image = new Image(getClass().getResourceAsStream("/texas_district_key.png"));
        for (int i = 1; i <=14; i++)
        {
            PixelReader pixelReader = image.getPixelReader();
            Color color = pixelReader.getColor(15, -25 + 42 * i);
            writer.println(i + " " + color + " " + (int)(color.getRed() * 255) + " " + (int)(color.getGreen() * 255)  + " " +  (int)(color.getBlue() * 255));
        }
        for (int i = 15; i <=28; i++)
        {
            PixelReader pixelReader = image.getPixelReader();
            Color color = pixelReader.getColor(175, -25 + 42 * (i-14));
            writer.println(i + " " + color + " " + (int)(color.getRed() * 255) + " " + (int)(color.getGreen() * 255)  + " " +  (int)(color.getBlue() * 255));
        }
        for (int i = 29; i <= 36; i++)
        {
            PixelReader pixelReader = image.getPixelReader();
            Color color = pixelReader.getColor(325, -25 + 42 * (i-28));
            writer.println(i + " " + color + " " + (int)(color.getRed() * 255) + " " + (int)(color.getGreen() * 255)  + " " +  (int)(color.getBlue() * 255));
        }
        writer.close();


    }

}
//175 325