package com.jart0007;

import javax.lang.model.type.NullType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import static java.lang.Math.*;

///<> Algoritmo Greedy Aleatorio (GRA)
public class AlgGRA_Clase01_Grupo03 {
    ///<> datos del problema (mismos nombres que en LeerArchivo para no tocar los metodos copiados)
    private LeerArchivo problema;
    private float[][] matriz;
    private int dimension;

    public AlgGRA_Clase01_Grupo03(LeerArchivo problema) {
        this.problema = problema;
        this.matriz = problema.getMatriz();
        this.dimension = problema.getDimension();
    }
    private double funcionEvaluacion(int[] ciudades) {
        return problema.funcionEvaluacion(ciudades);
    }

    private int[] orden; //orden por centralidad, para tenerlo guardado en vez de usar un parametro despues


    private int[] ordenCentralidad() {
        if (orden == null) {
            Par<Integer, Double>[] vd = new Par[dimension];
            for (int i = 0; i < dimension; i++) {
                double suma = 0;
                float[] fila = matriz[i];
                for (int j = 0; j < dimension; j++) suma += fila[j];
                vd[i] = new Par<>(i, suma);
            }
            Arrays.sort(vd, Comparator.comparing(Par::getValor));
            orden = new int[dimension];
            for (int i = 0; i < dimension; i++) orden[i] = vd[i].getClave();
        }
        return orden;
    }

    int[] greedyAleatorio(int k, long semilla) {

        //primero inicializamos el generador de numeros aleatorios con la semilla, al inicio del todo
        Random generador = new Random(semilla);

        //el principio es igual a el Greedy
        ArrayList<Integer> v = new ArrayList<>(dimension);
        for (int c : ordenCentralidad()) v.add(c); //<><opt> el orden no depende de la semilla: se calcula una sola vez, nos equivocamos y lo pusimos abajo en una primera version

        //ahora es cuando debemos escoger uno aleatorio entre la posicion 0 y la k-1 (por como funcionan los indices)


        int[] sol = new int[dimension];
        for (int i = 0; i < dimension; i++) {
            int limiteActual = min(k, v.size());
            int pos = generador.nextInt(limiteActual);

            sol[i] = v.get(pos);


            int ultimo = v.getLast();
            v.set(pos, ultimo);
            v.removeLast();
        }

        return sol;
    }

    int[] greedyAleatorio(int k, long semilla,Log log) throws IOException {

        //primero inicializamos el generador de numeros aleatorios con la semilla, al inicio del todo
        Random generador = new Random(semilla);

        log.addLine("Inicio Greedy Aleatorio | Semilla: " + semilla + " | k: " + k);
        //el principio es igual a el Greedy
        ArrayList<Integer> v = new ArrayList<>(dimension); //<><opt>
        for (int c : ordenCentralidad()) v.add(c); //<><opt> el orden por centralidad no depende de la semilla: se calcula una sola vez

        //ahora es cuando debemos escoger uno aleatorio entre la posicion 0 y la k-1 (por como funcionan los indices)


        int[] sol = new int[dimension];
        for (int i = 0; i < dimension; i++) {
            int limiteActual = min(k, v.size());
            int pos = generador.nextInt(limiteActual);

            sol[i] = v.get(pos);


            int ultimo = v.getLast();
            v.set(pos, ultimo);
            v.removeLast();
        }
        log.addLine("Solucion Greedy Aleatorio: Coste = " + funcionEvaluacion(sol));
        return sol;
    }
}