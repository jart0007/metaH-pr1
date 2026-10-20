package com.jart0007;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import static java.lang.Math.*;

///<> Algoritmo Greedy (GRE)
public class AlgGRE_Clase01_Grupo03 {
    ///<> datos del problema (mismos nombres que en LeerArchivo para no tocar los metodos copiados)
    private LeerArchivo problema;
    private float[][] matriz;
    private int dimension;

    public AlgGRE_Clase01_Grupo03(LeerArchivo problema) {
        this.problema = problema;
        this.matriz = problema.getMatriz();
        this.dimension = problema.getDimension();
    }

    private double funcionEvaluacion(int[] ciudades) {
        return problema.funcionEvaluacion(ciudades);
    }
    /**
     * Greedy
     * * si convierto la matriz de distancias (a.getMatriz()) a un vector de sumatorias
     * * me dice cual es la ciudad mas centrica, ordenar las ciudades conforme
     * *
     * * 1. calcular vector de sumatorias y ordenarlo
     * * 2. funcion de evaluacion [ F(x) = sum(d1,2 + d2,3 +...) + dn,1 ]
     *
     * @return solucion en vector de ciudades visitadas en orden
     */
    int[] greedy(Log log) throws IOException {
        Par<Integer, Double>[] vd = new Par[dimension];
        log.addLine("Inicio del greedy, no usa semilla");
        //inicializar el vector a 0

        for (int i = 0; i < dimension; i++) {
            vd[i] = new Par<>(i, (double) 0);
        }

        //rellenamos el vector de sumatorias

        for (int i = 0; i < dimension; i++) {
            double suma = 0;
            float[] fila = matriz[i];
            for (int j = 0; j < dimension; j++) {
                suma += fila[j]; //<><opt>
            }
            vd[i].setValor(suma);
        }

        //ordenamos el vector
        Arrays.sort(vd, Comparator.comparing(Par::getValor));

        int[] v = new int[dimension];
        for (int i = 0; i < dimension; i++) {
            v[i] = vd[i].getClave();
        }

        log.addLine("Solucion Greedy : Coste = " + funcionEvaluacion(v));
        return v; //Ej. [2,3,4,1]

    }
}