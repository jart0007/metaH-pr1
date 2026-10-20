package com.jart0007;

import java.io.IOException;

///<> Busqueda Local del primer mejor con DLB (BL)
public class AlgBL_Clase01_Grupo03 {
    ///<> datos del problema
    private LeerArchivo problema;
    private float[][] matriz;
    private int dimension;
    private int maxIteraciones;
    private Log log;

    private static final int UMBRAL_VECINOS = 2500; //<><opt> a partir de esta dimension se usa lista de candidatos (a280, ch130, pr144 siguen exactas)
    private boolean usarVecinos; //<><opt>
    private int[][] vecinos; //<><opt> vecinos[ciudad] = ciudades mas cercanas ordenadas por distancia (se crea la primera vez que se usa)
    private int[] pos; //<><opt> pos[ciudad] = indice de esa ciudad dentro del vector solucion

    public AlgBL_Clase01_Grupo03(LeerArchivo problema, Configurador conf) {
        this.problema = problema;
        this.matriz = problema.getMatriz();
        this.dimension = problema.getDimension();
        this.maxIteraciones = conf.getIteraciones();
        this.usarVecinos = dimension > UMBRAL_VECINOS; //<><opt>
        this.pos = new int[dimension]; //<><opt>
    }

    ///<> la funcion de evaluacion vive en LeerArchivo; este metodo evita tocar las llamadas copiadas
    private double funcionEvaluacion(int[] ciudades) {
        return problema.funcionEvaluacion(ciudades);
    }

    /**
     * k vecinos mas cercanos de cada ciudad. Coste O(n^2) pero con acceso secuencial a cada fila, se hace una sola vez
     */
    private int[][] calcularVecinos(int k) { //<><opt>
        int[][] res = new int[dimension][k]; //<><opt>
        double[] dist = new double[k]; //<><opt>
        for (int c = 0; c < dimension; c++) { //<><opt>
            int n = 0; //<><opt>
            float[] fila = matriz[c]; //<><opt>
            for (int o = 0; o < dimension; o++) { //<><opt>
                if (o == c) continue; //<><opt>
                double d = fila[o]; //<><opt>
                if (n == k && d >= dist[k - 1]) continue; //<><opt>
                int p = (n < k) ? n++ : k - 1; //<><opt>
                while (p > 0 && dist[p - 1] > d) { dist[p] = dist[p - 1]; res[c][p] = res[c][p - 1]; p--; } //<><opt>
                dist[p] = d; res[c][p] = o; //<><opt>
            } //<><opt>
        } //<><opt>
        return res; //<><opt>
    } //<><opt>

    /* ------------------------Busqueda local del primero el mejor, con entorno generado por DLB (no mirar bit de ciudad no prometedora)  -----------------------------*/



    /**
     * funcion para aplicar movimientos prometedores dentro de DLB()
     * @param i indice del vector a cambiar con j
     * @param j indice del vector a cambiar con i
     * @param v vector solucion
     */
    private void aplicarMovimiento(int i, int j, int[] v){
        int aux = v[i];
        v[i] = v[j];
        v[j] = aux;
        pos[v[i]] = i; //<><opt>
        pos[v[j]] = j; //<><opt>
    }

    /**
     * método  que usa DLB() para ver si un cambio (2-opt) mejora la solución
     *
     * @param i            indice de la primera ciudad
     * @param j            indice de la segunda ciudad
     * @param nuevaFuncion (out) parametro de salida con el valor de la nuevaFuncion por si hace falta
     * @return devuelve True si la solucion mejora con ese cambio
     */
    private boolean solucionMejor(int i, int j, int ant_i, int post_i, double d_ant_i, double d_i_post,
                                  double F_SolInicial, int[] v, double[] nuevaFuncion) {

        int post_j = (j + 1 == dimension) ? 0 : j + 1;
        int ant_j  = (j - 1 < 0) ? dimension - 1 : j - 1;

        double arcosAntes;
        double arcosDespues;

        // CASO 1: Ciudades adyacentes (j está justo después de i)
        if (post_i == j) {
            arcosAntes   = d_ant_i + matriz[v[j]][v[post_j]];
            arcosDespues = matriz[v[ant_i]][v[j]] + matriz[v[i]][v[post_j]];

        }
        // CASO 2: Ciudades adyacentes (i está justo después de j)
        else if (post_j == i) {
            arcosAntes   = matriz[v[ant_j]][v[j]] + d_i_post;
            arcosDespues = matriz[v[ant_j]][v[i]] + matriz[v[j]][v[post_i]];

        }
        // CASO 3: CASO NORMAL SWAP (Ciudades no adyacentes, 4 arcos)
        else {
            double d_ant_j  = matriz[v[ant_j]][v[j]];
            double d_j_post = matriz[v[j]][v[post_j]];

            arcosAntes   = d_ant_i + d_i_post + d_ant_j + d_j_post;
            arcosDespues = matriz[v[ant_i]][v[j]] + matriz[v[j]][v[post_i]] + matriz[v[ant_j]][v[i]] + matriz[v[i]][v[post_j]];
        }

        if (arcosDespues < arcosAntes) {
            nuevaFuncion[0] = F_SolInicial - arcosAntes + arcosDespues;
            return true;
        }

        return false;
    }

    /**
     * DLB
     * Recorrer vector binario junto al vector solución para omitir ciudades no prometedoras
     */
    int[] DLB(int[] sol, int[] bin, int indice, double f_inicial, Log log,int[] contadorMovimientos) throws IOException {
        int max = sol.length;
        int i = indice;

        double[] posibleNuevaFuncion = new double[1];

        for (int pasosI = 0; pasosI < max; pasosI++) {
            boolean mejora = false;

            if (bin[i] != 1) {
                int ant_i  = (i - 1 < 0) ? max - 1 : i - 1;
                int post_i = (i + 1 == max) ? 0 : i + 1;

                // Precalculamos las dos distancias fijas de 'i' para evitar reconsultar la matriz en el bucle 'j'
                double d_ant_i  = matriz[sol[ant_i]][sol[i]];
                double d_i_post = matriz[sol[i]][sol[post_i]];

                int j = (i + 1) % max;
                int numJ = usarVecinos ? vecinos[sol[i]].length : max - 1; //<><opt> solo k candidatos en vez de n-1

                for (int pasosJ = 0; pasosJ < numJ; pasosJ++) { //<><opt>
                    if (usarVecinos) j = pos[vecinos[sol[i]][pasosJ]]; //<><opt> j = posicion actual de un vecino cercano

                    mejora = solucionMejor(i, j, ant_i, post_i, d_ant_i, d_i_post, f_inicial, sol, posibleNuevaFuncion);

                    if (mejora) {
                        int ciudadA = sol[i];
                        int ciudadB = sol[j];

                        // 2. Aplicamos el intercambio
                        aplicarMovimiento(i, j, sol);
                        f_inicial = posibleNuevaFuncion[0];

                        // 3. Escribimos en el log de forma rápida
                        log.writeLine("\t\tMovimiento " + contadorMovimientos[0] + ": cambio: (" + ciudadA + ", " + ciudadB + ") | Nuevo Coste = " + f_inicial);
                        contadorMovimientos[0]++;

                        bin[i] = 0;
                        bin[j] = 0;
                        break; // Sale del bucle j y pasa a la siguiente ciudad del DLB
                    }
                    if (!usarVecinos) j = (j + 1) % max; //<><opt>
                }
            }

            if (!mejora) {
                bin[i] = 1;
            }

            i = (i + 1) % max;
        }

        return bin;
    }

    private boolean quedanCeros(int[] bin){
        for (int bit : bin){
            if (bit == 0) return true; //aun quedan ceros, debemos de seguir iterando
        }
        return false;
    }

    int[] busquedaLocal(int[] sol,Log log) throws IOException {
        log.openWriter();
        try {
            if (usarVecinos && vecinos == null) vecinos = calcularVecinos(dimension > 5000 ? 10 : 20); //<><opt> se calcula una sola vez por instancia
            for (int p = 0; p < dimension; p++) pos[sol[p]] = p; //<><opt> sincronizamos pos con la solucion inicial

            double costeActual = funcionEvaluacion(sol);
            int[] mascaraDBL = new int[dimension];
            int iteraciones = 0;
            int primerIndice = 0;

            int[] contadorMovimientos = new int[]{1};

            log.writeLine("Inicio de búsqueda local | coste inicial = " + costeActual);

            while (iteraciones < this.maxIteraciones && quedanCeros(mascaraDBL)) {
                double costeAnterior = costeActual;
                mascaraDBL = DLB(sol, mascaraDBL, primerIndice, costeActual, log, contadorMovimientos);
                costeActual = funcionEvaluacion(sol);

                if (costeActual < costeAnterior) {
                    log.writeLine("Iteracion: " + iteraciones + "| Mejora |--> nuevo coste = " + costeActual);
                }
                iteraciones++;
            }

            System.out.println("\t\tnumero de interacciones: " + iteraciones);
            log.writeLine("Fin de búsqueda local, iteracion: " + iteraciones + "| Coste: " + costeActual);

            return sol;
        }finally {
            log.closeWriter();
        }

    }
}
