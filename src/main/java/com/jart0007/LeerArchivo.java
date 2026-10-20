package com.jart0007;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static java.lang.Math.*;

public class LeerArchivo {
    private String name;
    private String comment;
    private String type;
    private int dimension;
    private String edge_type;
    private float[][] matriz;

    private float calcularDistancia(double x1, double x2, double y1, double y2) {
        return (float)sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    public double funcionEvaluacion(int[] ciudades) {
        double distancia = 0;
        for (int i = 0; i < dimension - 1; i++) {
            distancia += matriz[ciudades[i]][ciudades[i + 1]];
        }

        distancia += matriz[ciudades[dimension - 1]][ciudades[0]];
        return distancia;

    }

    public LeerArchivo(String ruta) {
        String linea;
        FileReader f = null;

        try {
            f = new FileReader(ruta);
            BufferedReader b = new BufferedReader(f);

            name = b.readLine().split(":")[1].trim();
            comment = b.readLine().split(":")[1].trim();
            type = b.readLine().split(":")[1].trim();
            dimension = Integer.parseInt(b.readLine().split(":")[1].trim());
            edge_type = b.readLine().split(":")[1].trim();

            b.readLine();

            matriz = new float[dimension][dimension];
            double[] coordX = new double[dimension];
            double[] coordY = new double[dimension];

            for (int i = 0; i < dimension; i++) {
                String[] lineav = b.readLine().trim().split("\\s+");

                coordX[i] = Double.parseDouble(lineav[1]);
                coordY[i] = Double.parseDouble(lineav[2]);
            }

            for (int i = 0; i < dimension; i++) {
                for (int j = 0; j < dimension; j++) {
                    matriz[i][j] = calcularDistancia(coordX[i], coordX[j], coordY[i], coordY[j]);

                }
            }

        } catch (IOException e) {
            System.out.println(e);
        }
    }


    public void mostrarMatriz() {
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                System.out.printf("%8.2f", matriz[i][j]);
            }
            System.out.println(" ");
        }
    }



    //------------------------------ GETTERS
    public String getEdge_type() {
        return edge_type;
    }

    public float[][] getMatriz() {
        return matriz;
    }

    public int getDimension() {
        return dimension;
    }

    public String getType() {
        return type;
    }

    public String getComment() {
        return comment;
    }

    public String getName() {
        return name;
    }
}
