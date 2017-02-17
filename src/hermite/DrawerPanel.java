package hermite;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;
import java.util.ArrayList;
import javax.swing.JPanel;

/**
 * @author mariusz
 */
public class DrawerPanel extends JPanel {
    List<Vector2> points = new ArrayList<>(); // Lista punktów kontrolnych
    List<Vector2> tangents = new ArrayList<>(); // Lista tangentów
    List<Vector2> spline = new ArrayList<>(); // Lista punktów spline'a
    boolean hermite = true; // Czy jest w trybie Hermite'a czy Catmull-Roma
    int counter = 2;
    // Etap wyzn. punktów -> 2: Pkt. kontr.; 1: Tangent
    int numberOfPoints = 20; // Ilość punktów w jednym odcinku spline'a

    public DrawerPanel() {
        super();
    }

    public void clear() { //Wyczyść panel
        points.clear();
        tangents.clear();
        spline.clear();
        counter = 2;
        repaint();
    }

    public void calculateCatmull() { // Oblicz krzywą Catmulla-Roma
        spline.clear();
        Vector2 p0, p1, m0, m1;
        for (int j = 0; j < points.size() - 1; j++) {
// determine control points of segment
            p0 = points.get(j); // Pierwszy pkt. kontrolny
            p1 = points.get(j + 1); // Drugi pkt. kontrolny
            if (j > 0) {
// Pierwszy tangent - połowa wektora [ tng[n+1] ; tng[n-1] ]
                m0 = new Vector2(0.5f * (points.get(j + 1).x - points.get(j - 1).x),
                        0.5f * (points.get(j + 1).y - points.get(j - 1).y));
            } else {
// Pierwszy tangent
                m0 = new Vector2((points.get(j + 1).x - points.get(j).x),
                        (points.get(j + 1).y - points.get(j).y));
            }
            if (j < points.size() - 2) {
// Drugi tangent - połowa wektora [ tng[n+2] ; tng[n] ]
                m1 = new Vector2(0.5f * (points.get(j + 2).x - points.get(j).x),
                        0.5f * (points.get(j + 2).y - points.get(j).y));
            } else {
// Drugi tangent
                m1 = new Vector2((points.get(j + 1).x - points.get(j).x),
                        (points.get(j + 1).y - points.get(j).y));
            }
            calculateCurve(p0, p1, m0, m1, j); // Utwórz krzywą
        }
    }

    public void calculateHermite() {
        spline.clear();
        Vector2 p0, p1, m0, m1;
        for (int j = 0; j < points.size() - 1; j++) {
// determine control points of segment
            p0 = points.get(j);
            p1 = points.get(j + 1);
            m0 = new Vector2(tangents.get(j).x - points.get(j).x, tangents.get(j).y - points.get(j).y);
            m1 = new Vector2(tangents.get(j + 1).x - points.get(j + 1).x, tangents.get(j + 1).y - points.get(j + 1).y);
            calculateCurve(p0, p1, m0, m1, j); // Utwórz krzywą
        }
    }

    public void calculateCurve(Vector2 p0, Vector2 p1, Vector2 m0, Vector2 m1, int j) {
        Vector2 position;
        float t;
        float pointStep = 1.0f / numberOfPoints;
        if (j == points.size() - 2) {
            pointStep = 1.0f / (numberOfPoints - 1.0f);
// ostatni punkt ostatniego odcinka jest równy ostatniemu pkt. kontrolnemu
        }
        for (int i = 0; i < numberOfPoints; i++) {
            t = i * pointStep;
            position = new Vector2( // Obliczenia
                    (2.0f * t * t * t - 3.0f * t * t + 1.0f) * p0.x + (t * t * t - 2.0f * t * t + t) * m0.x
                            + (-2.0f * t * t * t + 3.0f * t * t) * p1.x
                            + (t * t * t - t * t) * m1.x,
                    (2.0f * t * t * t - 3.0f * t * t + 1.0f) * p0.y
                            + (t * t * t - 2.0f * t * t + t) * m0.y
                            + (-2.0f * t * t * t + 3.0f * t * t) * p1.y
                            + (t * t * t - t * t) * m1.y
            );
            spline.add(position); // dodaj punkt do spline'a
        }
    }

    public void add(Vector2 v) {
        if (hermite) { // Tryb hermite'a
            if (counter == 2) { // Dodaj punkt kontorlny
                points.add(v);
            } else if (counter == 1) { // Dodaj tangent
                tangents.add(v);
            }
            counter--; // Kolejny etap
            if (counter == 0) { // Dodano punkt kontrolny i obliczanie spline'u
                calculateHermite();
                counter = 2; // Pierwszy etap
            }
        } else { // Tryb catmulla-roma
            points.add(v); // Dodaj punkt kontrolny
            if (points.size() >= 2) { // Jeśli jest min. 2 pkt. kontr. oblicz spline
                calculateCatmull();
            }
        }
        repaint(); // odmaluj komponent
    }

    public void setHermite() { // Tryb hermite'a
        hermite = true;
    }

    public void setCatmull() { // Tryb catmulla-roma
        hermite = false;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.blue); // Rysuj spline
        for (int i = 0; i < spline.size() - 1; i++) {
            g.drawLine((int) spline.get(i).x, (int) spline.get(i).y,
                    (int) spline.get(i + 1).x, (int) spline.get(i + 1).y);
        }
        g.setColor(Color.black); // Rysuj punkty kontrolne
        for (Vector2 v : points)
            g.drawOval((int) v.x - 4, (int) v.y - 4, 8, 8);
        g.setColor(Color.orange); // Rysuj tangenty
        for (Vector2 v : tangents)
            g.drawOval((int) v.x - 4, (int) v.y - 4, 8, 8);
        g.setColor(Color.green); // Rysuj linie między tangentami i pkt. kontrolnymi
        for (int i = 0; i < tangents.size(); i++) {
            g.drawLine((int) points.get(i).x, (int) points.get(i).y,
                    (int) tangents.get(i).x, (int) tangents.get(i).y);
        }
    }
}