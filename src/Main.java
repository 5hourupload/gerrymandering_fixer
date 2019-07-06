import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Main extends Application {
    GraphicsContext gc;
    List<Point> pointList = new LinkedList<>();
    List<Centroid> centroidList = new LinkedList<>();
    int DRAW_WIDTH = 800;
    int DRAW_HEIGHT = 800;
    double AVERAGE_POPULATION = 797273;
    int iterations = 0;
    boolean weightsEnabled = false;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        // Create the Canvas
        Canvas canvas = new Canvas(DRAW_WIDTH, DRAW_HEIGHT);
        gc = canvas.getGraphicsContext2D();

        List<List<String>> countyPopulationsSpreadsheet = new LinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("texas_county_populations.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                LinkedList<String> temp = new LinkedList<>(Arrays.asList(values));
                countyPopulationsSpreadsheet.add(temp);
            }
        }

        Map<String, Integer> countyPopulations = new HashMap<>();

        for (List<String> list : countyPopulationsSpreadsheet) {
            if (!list.get(2).contains("County")) continue;
            String name = list.get(2);
            name = name.substring(1,name.indexOf(" County"));
            name = name.replace(" " , "");
            int population = Integer.parseInt(list.get(14));
            countyPopulations.put(name,population);
        }

        System.out.println(countyPopulations);
        List<List<String>> records = new LinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("Texas_Counties_Centroid_Map.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                LinkedList<String> temp = new LinkedList<>(Arrays.asList(values));
                records.add(temp);
            }
        }
        for (int i = 1; i < records.size(); i++) {
            records.get(i).add(1, "-".concat(records.get(i).get(1)));
            records.get(i).remove(2);
        }
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -9999;
        double maxY = -9999;
        for (int i = 1; i < records.size(); i++) {
            if (Double.parseDouble(records.get(i).get(0)) < minX) {
                minX = Double.parseDouble(records.get(i).get(0));
            }
            if (Double.parseDouble(records.get(i).get(0)) > maxX) {
                maxX = Double.parseDouble(records.get(i).get(0));
            }

            if (Double.parseDouble(records.get(i).get(1)) < minY) {
                minY = Double.parseDouble(records.get(i).get(1));
            }
            if (Double.parseDouble(records.get(i).get(1)) > maxY) {
                maxY = Double.parseDouble(records.get(i).get(1));
            }
        }

        double xRange = maxX - minX;
        double yRange = maxY - minY;

        for (int i = 1; i < records.size(); i++) {
            double x = Double.parseDouble(records.get(i).get(0));
            double y = Double.parseDouble(records.get(i).get(1));

            x -= minX;
            x *= ((DRAW_WIDTH - 100) / xRange);
            y -= minY;
            y *= ((DRAW_WIDTH - 100) / yRange);
            x += 50;
            y += 50;
            String name = records.get(i).get(2).replace(" ","");
            int population = countyPopulations.get(name);
            for (int j = 0; j < population / 1000;j++)
            {
                double angle = Math.random() * 360;
                double radius = Math.random() * 25 * Math.random();
                Point point = new Point(x + Math.cos(Math.toRadians(angle)) * radius, y + Math.sin(Math.toRadians(angle)) * radius,1000);
                pointList.add(point);

            }
        }
        generateRandomCentroids(36);
//        assignPoints();
        drawPoints();

        Button update = new Button();
        update.setText("update");
        update.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                assignPoints();
                update();
                drawPoints();
                iterations++;
            }
        });

        Button printPopulations = new Button();
        printPopulations.setText("Print Populations");
        printPopulations.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                printPopulations();
            }
        });

        Button updateWeights = new Button();
        updateWeights.setText("update Weights");
        updateWeights.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                updateWeights();
                System.out.println("weights updated");
            }
        });

        VBox vBox = new VBox();
        vBox.getChildren().addAll(canvas, update,updateWeights);
        Scene scene = new Scene(vBox);
        stage.setScene(scene);
        stage.setTitle("Creation of a Canvas");
        stage.show();
    }

//    public void generateRandomPoints() {
//        for (int i = 0; i < 11000; i++) {
//            Point point = new Point((int) (Math.random() * DRAW_WIDTH),
//                    (int) (Math.random() * DRAW_HEIGHT));
//            pointList.add(point);
//        }
//    }

    public void generateRandomCentroids(int number) {
        List<Color> colors = new LinkedList<>();
        for (int i = 0; i < number; i++)
            colors.add(Color.hsb(((double) i / number) * 360, 1, 1));

        for (int i = 0; i < number; i++) {
            Centroid centroid = new Centroid((int) (Math.random() * 150) + DRAW_WIDTH * .5 - 75,
                    (int) (Math.random() * 150) + DRAW_HEIGHT * .5 - 75, colors.get(i));
            centroidList.add(centroid);
        }
    }

    private void assignPoints() {
        for (Centroid c : centroidList)
        {
            c.setOldPopulation(c.getPopulation());
            c.setPopulation(0);
//            c.setWeight(1);
        }
        for (Point p : pointList) {
            double shortestDistance = Integer.MAX_VALUE;
            Centroid closestCentroid = null;
            for (Centroid c : centroidList) {
                double distance = Math.sqrt(Math.pow((c.getX() - p.getX()), 2) + Math.pow((c.getY() - p.getY()), 2)) * c.getWeight();
                if (distance < shortestDistance) {
                    shortestDistance = distance;
                    p.setColor(c.getColor());
                    closestCentroid = c;
                }
            }
            if (closestCentroid != null) {
                closestCentroid.setPopulation(closestCentroid.getPopulation() + p.getPopulation());

            }
        }
        updateWeights();
        System.out.println();
        printPopulations();
    }

    private void drawPoints() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, DRAW_WIDTH, DRAW_HEIGHT);

        if (centroidList.size() > 0) {

            shade(0, 0, DRAW_WIDTH / 2);
            shade(DRAW_WIDTH / 2, 0, DRAW_WIDTH / 2);
            shade(0, DRAW_WIDTH / 2, DRAW_WIDTH / 2);
            shade(DRAW_WIDTH / 2, DRAW_WIDTH / 2, DRAW_WIDTH / 2);
        }

        for (Point point : pointList) {
            int population = point.getPopulation();
            double popMult = population/100000;
            gc.setFill(point.getColor());
            gc.setFill(Color.BLACK);
//            gc.fillOval(point.getX() - (5 + popMult/2), point.getY() - (5 + popMult/2), 10 + popMult, 10 + popMult);
            gc.fillOval(point.getX() - 1, point.getY() - 1, 2, 2);
        }
        for (Centroid centroid : centroidList) {
            Color c = centroid.getColor();
            gc.setFill(Color.rgb((int)(c.getRed() * 255),(int)(c.getGreen() * 255),(int)(c.getBlue() * 255),.5));
            gc.fillOval(centroid.getX() - 20, centroid.getY() - 20, 40, 40);
        }
//        Image image = getBackgroundImage();
//        gc.drawImage(getBackgroundImage(),0,0,800,800);
    }

    private void shade(int x, int y, int sideLength) {
        Color startPaint = getClosestPaint(x, y);
        if (startPaint == getClosestPaint(x, y + sideLength - 1) &&
                startPaint == getClosestPaint(x + sideLength - 1, y) &&
                startPaint == getClosestPaint(x + sideLength - 1, y + sideLength - 1)) {

            Color tint = startPaint;
            tint = Color.rgb((int) (tint.getRed() * 255), (int) (tint.getGreen() * 255),
                    (int) tint.getBlue() * 255, .25);

            gc.setFill(Color.WHITE);
            gc.fillRect(x, y, x + sideLength, y + sideLength);
            gc.setFill(tint);
            gc.fillRect(x, y, x + sideLength, y + sideLength);

        }
        else {
            int newLength = sideLength / 2;
            if (sideLength % 2 == 1 && sideLength > 2) newLength++;
            shade(x, y, newLength);
            shade(x + newLength, y, newLength);
            shade(x, y + newLength, newLength);
            shade(x + newLength, y + newLength, newLength);
        }
    }

    private Color getClosestPaint(int x, int y) {
        Color local = null;
        double shortestDistance = Integer.MAX_VALUE;

        for (Centroid c : centroidList) {
            double distance = Math.sqrt(Math.pow((c.getX() - x), 2) + Math.pow((c.getY() - y), 2)) * c.getWeight();
            if (distance < shortestDistance) {
                shortestDistance = distance;
                local = c.getColor();
            }
        }
        return local;
    }

    public void update() {
        Map<Color, List<Point>> avgLocations = new HashMap<>();
        for (Centroid c : centroidList) {
            List<Point> temp = new LinkedList<>();
            avgLocations.put(c.getColor(), temp);
        }
        for (Point p : pointList) {
            List<Point> temp = avgLocations.get(p.getColor());
            temp.add(p);
            avgLocations.put(p.getColor(), temp);
        }
        for (Centroid c : centroidList) {
            List<Point> points = avgLocations.get(c.getColor());
            double meanX = 0;
            double meanY = 0;
            for (Point p : points) {
                meanX += p.getX();
                meanY += p.getY();
            }
            meanX /= points.size();
            meanY /= points.size();

            double vector[] = {meanX - c.getX(),meanY-c.getY()};
            if (points.size() != 0) {
//                c.setX(c.getX() + vector[0] * c.getWeight());
//                c.setY(c.getY() + vector[1] * c.getWeight());
                c.setX(meanX);
                c.setY(meanY);
            }
        }
    }
    private void printPopulations()
    {
        double total = 0;
        LinkedList<Integer> populations = new LinkedList<>();
        int min = Integer.MAX_VALUE;
        int max = 0;
        for (Centroid c : centroidList)
        {
            total+=c.getPopulation();
            populations.add(c.getPopulation());
            min = min < c.getPopulation() ? min : c.getPopulation();
            max = max > c.getPopulation() ? max : c.getPopulation();
        }
//        System.out.println("average: " + total/centroidList.size());
        System.out.println(populations);
        System.out.println("min: " + min + " max: " + max);
    }
    private void updateWeights()
    {
        for (Centroid c : centroidList)
        {
//                if (Math.abs(c.getPopulation() - AVERAGE_POPULATION) < 50_000)
//                    continue;
                if (c.getPopulation() > AVERAGE_POPULATION)
                    if (c.getWeight() > 4) continue;

                    c.setWeight(c.getWeight()+.05);
//                    c.setWeight(c.getWeight()*.99);
//                    c.setWeight(c.getWeight()-(.01 * (Math.abs(c.getPopulation()-AVERAGE_POPULATION)/AVERAGE_POPULATION)));
                if (c.getPopulation() < AVERAGE_POPULATION) {
                    if (c.getWeight() < .6) continue;
                    c.setWeight(c.getWeight() - .05);
                }

//                    c.setWeight(c.getWeight()*1.01);
//                    c.setWeight(c.getWeight()+(.01 * (Math.abs(c.getPopulation()-AVERAGE_POPULATION)/AVERAGE_POPULATION)));


            System.out.print(c.getWeight() + ":" + (Math.abs(c.getPopulation()-AVERAGE_POPULATION)/AVERAGE_POPULATION) + " ");
        }
    }
    private Image getBackgroundImage() {
        return new Image(getClass().getResourceAsStream("/texas_district_map_only.png"));

    }
}