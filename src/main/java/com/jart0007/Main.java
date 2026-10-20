/**
 * @author Juan Antonio Rosell Torres
 * @date 14/06/2026
 *
 * [nota]: En este codigo, en varios ficheros, hay diferentes tipos de comentarios del estilo: "//" o "//<>"" o "//<><opt>. Son una guia personal para nosotros, no tienen ninguna intencion
 */
package com.jart0007;

import java.io.IOException;
import java.util.Arrays;

/*
* Metaheurísticas grupo 3
*
* Todo argumentos -Xmx -Xns para desbordamiento
*/
public class Main {



    static void main(String[] args) throws IOException {
        //creamos el configurador con la ruta que le pasamos por argumento
        Configurador conf = new Configurador(args[0]);



            //1º ahora la idea es aplicar todos los algoritmos a todos los archivos con todas las semillas
        for (int i = 0; i < conf.archivos.size(); i++) {
            LeerArchivo a = new LeerArchivo(conf.archivos.get(i));

            ///<> un objeto por algoritmo, todos sobre la misma instancia leida
            AlgGRE_Clase01_Grupo03 gre = new AlgGRE_Clase01_Grupo03(a);
            AlgGRA_Clase01_Grupo03 gra = new AlgGRA_Clase01_Grupo03(a);
            AlgBL_Clase01_Grupo03 bl = new AlgBL_Clase01_Grupo03(a, conf);
            AlgTA_Clase01_Grupo03 bt = new AlgTA_Clase01_Grupo03(a, conf); //la tabu y local reciben el configurador para leer sus parametros

            System.out.println(" | " + a.getName() + " | ");
            for (int j = 0; j < conf.algoritmos.size(); j++) {

                System.out.println("\t | " + conf.algoritmos.get(j) +" | ");

                switch (conf.algoritmos.get(j)) {
                    case "Greedy" :
                    {
                        String nombreLog = "log_Greedy_" + a.getName() + "_sin semilla.txt";
                        Log log = new Log(nombreLog,true);

                        int[] v = gre.greedy(log);

                        System.out.println("\t\t no importa la semilla --> " + Arrays.toString(Arrays.copyOfRange(v, 0, 6)));
                        System.out.printf("\t\t Funcion de evaluacion = %,.2f%n", a.funcionEvaluacion(v));                        break;
                    }
                    case "GreedyAleatorio" :
                    {
                        for (int k=0; k < conf.semillas.size(); k++) {
                            String nombreLog = "log_GreedyAleatorio_" + a.getName() + "_" + conf.semillas.get(k) + ".txt";
                            Log log = new Log(nombreLog,true);

                            int[] v = gra.greedyAleatorio(conf.k_greedy,conf.semillas.get(k),log);

                            System.out.println("\t\t semilla: " + conf.semillas.get(k) + " --> " + Arrays.toString(Arrays.copyOfRange(v, 0, 6)));
                            System.out.printf("\t\t Funcion de evaluacion = %,.2f%n", a.funcionEvaluacion(v));                        }
                        break;
                    }
                    case "BusquedaLocal":
                    {
                        for (int k=0; k < conf.semillas.size(); k++) {
                            String nombreLog = "log_BusquedaLocal_" + a.getName() + "_" + conf.semillas.get(k) + ".txt";
                            Log log = new Log(nombreLog,true);

                            log.addLine("BL con semilla: " + conf.semillas.get(k));

                            //primero necesitamos pasarle una solución inical, la creamos a partir de una semilla y el GreedyAleatorio
                            int[] ini = gra.greedyAleatorio(conf.k_greedy,conf.semillas.get(k));
                            log.addLine("Solucion inicial (Greedy Aleatorio): Coste= " + a.funcionEvaluacion(ini));

                            //ahora si podemos iniciar el algoritmo de Busqueda local
                            int[] v= bl.busquedaLocal(ini,log);
                            log.addLine("Solución Final (Búsqueda Local): Coste = " + a.funcionEvaluacion(v));

                            System.out.println("\t\t semilla: " + conf.semillas.get(k) + " --> " + Arrays.toString(Arrays.copyOfRange(v, 0, 6)));
                            System.out.printf("\t\t Funcion de evaluacion = %,.2f%n", a.funcionEvaluacion(v));                        }
                        break;
                    }
                    case "BusquedaTabu":
                    {
                        for (int k=0; k < conf.semillas.size(); k++) {
                            String nombreLog = "log_BusquedaTabu_" + a.getName() + "_" + conf.semillas.get(k) + ".txt";
                            Log log = new Log(nombreLog,true);

                            log.addLine("BT con semilla: " + conf.semillas.get(k));
                            //primero necesitamos pasarle una solución inical, la creamos a partir de una semilla y el GreedyAleatorio
                            int[] ini = gra.greedyAleatorio(conf.k_greedy,conf.semillas.get(k));
                            log.addLine("Solucion inicial (Greedy Aleatorio): Coste= " + a.funcionEvaluacion(ini));

                            //ahora si podemos iniciar el algoritmo de BusquedaTabu
                            int[] v= bt.busquedaTabu(ini, conf.semillas.get(k),log);
                            log.addLine("Solución Final (Búsqueda Tabú): Coste = " + a.funcionEvaluacion(v));


                            System.out.println("\t\t semilla: " + conf.semillas.get(k) + " --> " + Arrays.toString(Arrays.copyOfRange(v, 0, 6)));
                            System.out.printf("\t\t Funcion de evaluacion = %,.2f%n", a.funcionEvaluacion(v));                        }
                        break;
                    }
                }
            }
        }
    }
}
