import javafx.scene.paint.Color;

public class Centroid {

    private double x, y;
    private Color color;
    private int population = 0;
    private double weight = 1;
    public Centroid(double x, double y, Color color)
    {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public void setX(double x)
    {
        this.x = x;
    }

    public void setY(double y)
    {
        this.y = y;
    }

    public Color getColor() {
        return color;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    public int getPopulation() {
        return population;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

}
