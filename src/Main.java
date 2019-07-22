import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {
    GraphicsContext gc;
    List<Point> pointList = new LinkedList<>();
    List<Centroid> centroidList = new LinkedList<>();
    List<Centroid> currentCentroidList = new LinkedList<>();
    int DRAW_WIDTH = 2048;
    int DRAW_HEIGHT = 2048;
    double AVERAGE_POPULATION = 797273;
    int iterations = 0;

    int highestMin = 0;
    int lowestMax = Integer.MAX_VALUE;
    List<Centroid> bestCentroids = new LinkedList<>();
    boolean showingBestCentroids = false;




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
        Map<String, String> countyVotingPatterns = new HashMap<>();

        for (List<String> list : countyPopulationsSpreadsheet) {
            if (!list.get(2).contains("County")) continue;
            String name = list.get(2);
            name = name.substring(1,name.indexOf(" County"));
            name = name.replace(" " , "");
            int population = Integer.parseInt(list.get(14));
            countyPopulations.put(name,population);
            countyVotingPatterns.put(name,list.get(15));
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
            String name = records.get(i).get(2).replace(" ", "");
            int population = countyPopulations.get(name);
            String voting = countyVotingPatterns.get(name);
            Map<String, Double> votingPattern = new HashMap<>();
            if (voting.equals("REP"))
            {
                votingPattern.put("REP",1.0);
                votingPattern.put("DEM",0.0);
            }
            else
            {
                votingPattern.put("REP",0.0);
                votingPattern.put("DEM",1.0);
            }
            for (int j = 0; j < population / 1000;j++)
            {
                double angle = Math.random() * 360;
                double radius = Math.random() * 25 * Math.random();
                Point point = new Point(x + Math.cos(Math.toRadians(angle)) * radius, y + Math.sin(Math.toRadians(angle)) * radius,1000);
                point.setVotingPattern(votingPattern);
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
                showingBestCentroids = false;
                currentCentroidList = centroidList;
                drawPoints();
                iterations++;
                System.out.println(iterations);
            }
        });

        Button updateMult = new Button();
        updateMult.setText("update multiple times");
        updateMult.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                for (int i = 0; i < 1000; i++)
                {
                    assignPoints();
                    update();
                    iterations++;
                    System.out.println(iterations);
                }

                showingBestCentroids = false;
                currentCentroidList = centroidList;
                drawPoints();
            }
        });
        Button switchCentroidView = new Button();
        switchCentroidView.setText("switch centroid views");
        switchCentroidView.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                showingBestCentroids = !showingBestCentroids;
                currentCentroidList = showingBestCentroids ? bestCentroids : centroidList;
                drawPoints();
            }
        });

        Button elect = new Button();
        elect.setText("simulate election");
        elect.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {

                simulateElection();
            }
        });

        Button snapShotBtn = new Button("Take a Snapshot");
        snapShotBtn.setOnAction((ActionEvent t) -> {
            try {
                SnapshotParameters parameters = new SnapshotParameters();
                WritableImage wi = new WritableImage(DRAW_WIDTH, DRAW_HEIGHT);
                WritableImage snapshot = canvas.snapshot(new SnapshotParameters(), wi);

                File output = new File("snapshot" + new Date().getTime() + ".png");
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", output);
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        VBox vBox = new VBox();
        vBox.getChildren().addAll(update, updateMult, elect,switchCentroidView, snapShotBtn, canvas);
        Scene scene = new Scene(vBox);
        stage.setScene(scene);
        stage.setTitle("gerrymanderr");
        stage.show();

    }

    private void simulateElection() {
        Map<Color, Map<String, Double>> results = new HashMap<>();
        for (Centroid centroid : currentCentroidList)
        {
            Map<String, Double> temp = new HashMap<>();
            temp.put("REP", 0d);
            temp.put("DEM", 0d);
            results.put(centroid.getColor(),temp);
        }
        for (Point p : pointList)
        {
                Map<String,Double> votingPattern = p.getVotingPattern();
                Map<String, Double> districtResult = results.get(p.getColor());
                districtResult.put("REP", districtResult.get("REP") + votingPattern.get("REP"));
                districtResult.put("DEM", districtResult.get("DEM") + votingPattern.get("DEM"));
        }
        int reps = 0;
        int dems = 0;

        Iterator it = results.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            Map<String, Double> result = (Map) pair.getValue();
            if (result.get("REP") > result.get("DEM"))
            {
                reps++;
            }
            else
            {
                dems++;
            }
            it.remove(); // avoids a ConcurrentModificationException
        }
        System.out.println(reps + " " + dems);
    }

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
            c.setPopulation(0);
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

        if (currentCentroidList.size() > 0) {

            shade(0, 0, DRAW_WIDTH / 2);
            shade(DRAW_WIDTH / 2, 0, DRAW_WIDTH / 2);
            shade(0, DRAW_WIDTH / 2, DRAW_WIDTH / 2);
            shade(DRAW_WIDTH / 2, DRAW_WIDTH / 2, DRAW_WIDTH / 2);
        }

        for (Point point : pointList) {
            gc.setFill(point.getColor());
            gc.setFill(Color.BLACK);
//            gc.fillOval(point.getX() - 1, point.getY() - 1, 2, 2);
        }
        for (Centroid centroid : currentCentroidList) {
            Color c = centroid.getColor();
            gc.setFill(Color.rgb((int)(c.getRed() * 255),(int)(c.getGreen() * 255),(int)(c.getBlue() * 255),.5));
//            gc.fillOval(centroid.getX() - 20, centroid.getY() - 20, 40, 40);
        }
    }

    private void shade(int x, int y, int sideLength) {
        Color startPaint = getClosestPaint(x, y);
        if (startPaint == getClosestPaint(x, y + sideLength - 1) &&
                startPaint == getClosestPaint(x + sideLength - 1, y) &&
                startPaint == getClosestPaint(x + sideLength - 1, y + sideLength - 1) &&
                startPaint == getClosestPaint(x + (sideLength-1)/2, y + (sideLength-1)/2)) {

            Color tint = startPaint;
            tint = Color.rgb((int) (tint.getRed() * 255), (int) (tint.getGreen() * 255),
                    (int) tint.getBlue() * 255, .5);

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

        for (Centroid c : currentCentroidList) {
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

            if (points.size() != 0) {
                c.setX(meanX);
                c.setY(meanY);
            }
        }

    }
    private void printPopulations()
    {
        LinkedList<Integer> populations = new LinkedList<>();
        int min = Integer.MAX_VALUE;
        int max = 0;
        for (Centroid c : centroidList)
        {
            populations.add(c.getPopulation());
            min = min < c.getPopulation() ? min : c.getPopulation();
            max = max > c.getPopulation() ? max : c.getPopulation();
        }
        System.out.println(populations);
        System.out.println("stdev: " + calculateSD(populations));
        System.out.println("min: " + min + " max: " + max);
        System.out.println("best min: " + highestMin + " best max: " + lowestMax);
        if (min > highestMin && max < lowestMax)
        {
            System.out.println("UPDATING BEST CENTROIDS");
            highestMin = min;
            lowestMax = max;
            bestCentroids.clear();
            for (Centroid c : centroidList)
            {
                Centroid temp = new Centroid(c.getX(), c.getY(), c.getColor());
                temp.setPopulation(c.getPopulation());
                temp.setWeight(c.getWeight());
                bestCentroids.add(temp);
            }
        }
    }
    private void updateWeights()
    {
        for (Centroid c : centroidList)
        {
//                if (Math.abs(c.getPopulation() - AVERAGE_POPULATION) < 50_000)
//                    continue;
                if (c.getPopulation() > AVERAGE_POPULATION)
                    c.setWeight(c.getWeight()+(.005 * (Math.abs(c.getPopulation()-AVERAGE_POPULATION)/AVERAGE_POPULATION)));
                if (c.getPopulation() < AVERAGE_POPULATION) {
                    c.setWeight(c.getWeight()-(.005 * (Math.abs(c.getPopulation()-AVERAGE_POPULATION)/AVERAGE_POPULATION)));
                }

            System.out.print(c.getWeight() + ":" + (Math.abs(c.getPopulation()-AVERAGE_POPULATION)/AVERAGE_POPULATION) + " ");
        }
    }
    public static double calculateSD(List<Integer> numbers)
    {
        double sum = 0.0, standardDeviation = 0.0;
        int length = numbers.size();
        for(double num : numbers) {
            sum += num;
        }
        double mean = sum/length;
        for(double num: numbers) {
            standardDeviation += Math.pow(num - mean, 2);
        }
        return Math.sqrt(standardDeviation/length);
    }
}