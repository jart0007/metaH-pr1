package com.jart0007;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Configurador {
    ArrayList<String> archivos;
    ArrayList<String> algoritmos;
    ArrayList<Long> semillas;
    Integer k_greedy;

    // parametros de la busqueda tabu; si faltan en el config.txt se usan los valores del guion
    Integer iteraciones = 10000;
    Integer porcentajeEstancamiento = 5;
    Integer tenenciaTabu = 10;
    Integer porcentajeOscilacion = 50;

    public Configurador(String ruta) {
        archivos = new ArrayList<>();
        algoritmos = new ArrayList<>();
        semillas = new ArrayList<>();


        //Copiamos de Stackoverflow o cualquier sitio de internet el tipico algoritmo de leer archivos en java

        String linea;
        FileReader f = null;
        try{
            f = new FileReader(ruta);
            BufferedReader b = new BufferedReader(f);

            while((linea=b.readLine())!=null){
                String[] split = linea.split("=");

                switch(split[0]){
                    case "Archivos":
                        // archivos.add(split[1]); <-- para crear un vector usando de separacion el parametro de la funcion split
                        String[] v = split[1].split(" ");
                        for(int i=0; i<v.length; i++){
                            archivos.add(v[i]);
                        }
                        break;
                    case "Semillas":
                        String[] vsemillas = split[1].split(" ");
                        for(int i=0; i<vsemillas.length; i++){
                            semillas.add(Long.parseLong(vsemillas[i]));

                        }
                        break;
                    case "Algoritmos":
                        String[] valgoritmos = split[1].split(" ");
                        for(int i=0; i<valgoritmos.length; i++){
                            algoritmos.add(valgoritmos[i]);
                        }
                        break;
                    case "k_greedy":
                        k_greedy=Integer.parseInt( split[1] );
                        break;
                    //nuevos parametros de la busqueda tabu, leidos igual que el parametro del greedy aleatorio
                    case "Iteraciones":
                        iteraciones=Integer.parseInt( split[1] );
                        break;
                    case "PorcentajeEstancamiento":
                        porcentajeEstancamiento=Integer.parseInt( split[1] );
                        break;
                    case "TenenciaTabu":
                        tenenciaTabu=Integer.parseInt( split[1] );
                        break;
                    case "PorcentajeOscilacion":
                        porcentajeOscilacion=Integer.parseInt( split[1] );
                        break;
                }

            }
        }catch (IOException e){
            System.out.println(e);
        }
    }

    public ArrayList<String> getArchivos() {
        return archivos;
    }

    public ArrayList<String> getAlgoritmos() {
        return algoritmos;
    }

    public ArrayList<Long> getSemillas() {
        return semillas;
    }

    public Integer getParametroextra() {
        return k_greedy;
    }

    // getters de los nuevos parametros
    public Integer getIteraciones() {
        return iteraciones;
    }

    public Integer getPorcentajeEstancamiento() {
        return porcentajeEstancamiento;
    }

    public Integer getTenenciaTabu() {
        return tenenciaTabu;
    }

    public Integer getPorcentajeOscilacion() {
        return porcentajeOscilacion;
    }
}
