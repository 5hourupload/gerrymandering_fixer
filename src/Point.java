import javafx.scene.paint.Color;

public class Point
{
    private double x, y;

    private Color color = Color.BLACK;
    private int population;
    public Point(double x, double y, int population)
    {
        this.x = x;
        this.y = y;
        this.population = population;
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public int getPopulation() {
        return population;
    }
}