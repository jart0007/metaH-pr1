package com.jart0007;


import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import static java.lang.Math.max;
import static java.lang.Math.min;

///Algoritmo Tabu (TA) - pendiente de implementar
public class AlgTA_Clase01_Grupo03 {
    ///datos del problema (mismos nombres que en LeerArchivo para no tocar los metodos copiados)
    private LeerArchivo problema;
    private float[][] matriz;
    private int dimension;
    private int maxIteraciones;
    private int estancamiento;
    private int tenenciaTabu;
    private int porcentajeOscilacion;

    private static final int UMBRAL_VECINOS = 500; //<><opt> a partir de esta dimension se usa lista de candidatos (a280, ch130, pr144 siguen exactas)
    private boolean usarVecinos; //<><opt>
    private int[][] vecinos; //<><opt> vecinos[ciudad] = ciudades mas cercanas (se crea la primera vez que se usa)
    private int[] pos; //<><opt> pos[ciudad] = indice de esa ciudad en el vector solucion

    //La parte de la diagional superior es la memoria a corto plazo y la de abajo memoria a largo plazo :D
    private short  memoria[][];

    public AlgTA_Clase01_Grupo03(LeerArchivo problema, Configurador conf) {
        this.problema = problema;
        this.matriz = problema.getMatriz();
        this.dimension = problema.getDimension();
        //<><opt> la memoria short[n][n] ya no se reserva aqui (685MB en d18512 aunque no se use la tabu); se crea en busquedaTabu
        this.maxIteraciones = conf.getIteraciones();
        this.estancamiento = (int) (conf.getPorcentajeEstancamiento() / 100.0 * this.maxIteraciones);
        this.tenenciaTabu = conf.getTenenciaTabu();
        this.porcentajeOscilacion = conf.getPorcentajeOscilacion();
        this.usarVecinos = dimension > UMBRAL_VECINOS; //<><opt>
        this.pos = new int[dimension]; //<><opt>

        //control de iteraciones+tenencia. Si fuese mayor a el maximo valor del tipo de dato "SHORT" no cabría
        if(this.maxIteraciones + tenenciaTabu > Short.MAX_VALUE){
            throw new IllegalArgumentException("Iteraciones + TenenciaTabu no puede superar" + Short.MAX_VALUE);
        }
    }

    private double funcionEvaluacion(int[] ciudades) {
        return problema.funcionEvaluacion(ciudades);
    }
    private void aplicarMovimiento(int i, int j, int[] v) {
        int aux = v[i];
        v[i] = v[j];
        v[j] = aux;
        pos[v[i]] = i; //<><opt>
        pos[v[j]] = j; //<><opt>
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

    /* ------------------------ Busqueda tabú ----------------------------- */

    /*
    * Número de iteraciones = 10.000
    * Estancamiento = 5% del número de iteraciones
    * Tenencia tabú = 10
    * Oscilación estratégica = 50%
    * */

    /**
     * método  que usaba el DLB() para ver si un cambio (2-opt) mejora la solución, ahora en la busqueda tabu
     * se usa para calcular la diferencia entre dos soluciones (para en la exploracion ver cual es mejor)
     *
     * @param i            indice de la primera ciudad
     * @param j            indice de la segunda ciudad
     * @return devuelve True si la solucion mejora con ese cambio
     */
    private double calculaDiferencia(int i, int j, int ant_i, int post_i, double d_ant_i, double d_i_post, int[] v) {

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
        // CASO 3: caso normal (Ciudades no adyacentes, 4 arcos)
        else {
            double d_ant_j  = matriz[v[ant_j]][v[j]];
            double d_j_post = matriz[v[j]][v[post_j]];

            arcosAntes   = d_ant_i + d_i_post + d_ant_j + d_j_post;
            arcosDespues = matriz[v[ant_i]][v[j]] + matriz[v[j]][v[post_i]] + matriz[v[ant_j]][v[i]] + matriz[v[i]][v[post_j]];
        }

        //en la busqueda tabu solo nos interesa saber la diferencia
        return arcosDespues - arcosAntes;
    }

    /**
     * Explora el entorno y devuelve el mejor movimiento
     * @return devuelve la mejor diferencia y el (i,j)
     */
    double[] entorno(int[] sol, int iteraccion_actual) {
        double mejorDiferencia = Double.POSITIVE_INFINITY;
        int mejorI = -1;
        int mejorJ = -1;


        int limiteI = usarVecinos ? dimension : dimension - 1; //<><opt> con vecinos tambien se explora la ultima posicion como 'i'
        for (int i = 0; i < limiteI; i++) { //<><opt>
            int ant_i  = (i - 1 < 0) ? dimension - 1 : i - 1;
            int post_i = (i + 1 == dimension) ? 0 : i + 1;

            // Precalculamos las dos distancias fijas de 'i' para evitar reconsultar la matriz en el bucle 'j'
            double d_ant_i  = matriz[sol[ant_i]][sol[i]];
            double d_i_post = matriz[sol[i]][sol[post_i]];

            int numJ = usarVecinos ? vecinos[sol[i]].length : dimension - i - 1; //<><opt> k candidatos en vez de n-i-1
            for (int jj = 0; jj < numJ; jj++) { //<><opt>
                int j = usarVecinos ? pos[vecinos[sol[i]][jj]] : i + 1 + jj; //<><opt> j = posicion actual de un vecino cercano
                int c1 = sol[i];
                int c2 = sol[j];

                int menor = min(c1,c2);
                int mayor = max(c1,c2);

                boolean esTabu = memoria[menor][mayor] >= iteraccion_actual;

                if(!esTabu){
                    double diferencia = calculaDiferencia(i,j,ant_i,post_i,d_ant_i,d_i_post,sol); //buscamos la ciudad con mejor diferencia
                    if(diferencia < mejorDiferencia){
                        mejorDiferencia = diferencia;
                        mejorI = i;
                        mejorJ = j;
                    }
                }
            }
        }
        return new double[]{mejorDiferencia,mejorI,mejorJ};
    }


    // --------------- GESTION DE MEMORIA A LARGO Y CORTO PLAZO ----------------------

    /**
     * Vuelve a cero la MCP (triángulo superior) tras la Oscilación Estratégica.
     */
    private void vaciarMCP() {
        for (int i = 0; i < dimension; i++) {
            for (int j = i + 1; j < dimension; j++) {
                memoria[i][j] = 0;
            }
        }
    }

    /**
     * Incrementa la frecuencia de los arcos de la solución actual en la MLP (triángulo inferior).
     */
    private void actualizarMLP(int[] sol) {
        for (int i = 0; i < dimension; i++) {
            int c1 = sol[i];
            int c2 = sol[(i + 1) % dimension];

            int minC = min(c1, c2);
            int maxC = max(c1, c2);

            // Triángulo inferior: la fila es el índice mayor y la columna el menor
            memoria[maxC][minC]++;
        }
    }

    //Devuelve una solución q hace a partir de la info q tenemos. Memoria a largo plazo
    private int[] generarSolucionMLP(boolean intensificar, Random generador) {
        //<><opt> eliminada la matrizModificada (n*n doubles = 2.7GB en d18512); el coste modificado se calcula al vuelo en el greedy

        boolean[] visitado = new boolean[dimension];
        int[] nuevaSol = new int[dimension];

        int inicio = generador.nextInt(dimension);
        nuevaSol[0] = inicio;
        visitado[inicio] = true;

        //construccion "greedy" con la matriz modificada
        for (int paso = 1; paso < dimension; paso++) {
            int actual = nuevaSol[paso - 1];
            int mejorSiguiente = -1;
            double mejorCoste = Double.MAX_VALUE;

            for (int cand = 0; cand < dimension; cand++) {
                if (visitado[cand]) continue; //<><opt>
                int freq = memoria[max(actual, cand)][min(actual, cand)]; //<><opt> frecuencia del arco (triangulo inferior)
                //Intensificar: dividir abarata los arcos muy usados. Diversificar: multiplicar los encarece
                double costeMod = intensificar ? matriz[actual][cand] / (1.0 + freq) : matriz[actual][cand] * (1.0 + freq); //<><opt>
                if (costeMod < mejorCoste) { //<><opt>
                    mejorCoste = costeMod; //<><opt>
                    mejorSiguiente = cand;
                }
            }
            nuevaSol[paso] = mejorSiguiente;
            visitado[mejorSiguiente] = true;
        }
        return nuevaSol;
    }

    /**
     *
     */
    int[] busquedaTabu(int[] sol,long semilla,Log log) throws IOException {
        log.openWriter();
        try {
            //ponemos la memoria a "0" para las siguientes iteracciones
            if (memoria == null) memoria = new short[dimension][dimension]; //<><opt> reserva perezosa, solo si se ejecuta la tabu
            else for (int i = 0; i < memoria.length; i++) Arrays.fill(memoria[i], (short) 0); //<><opt>
            if (usarVecinos && vecinos == null) vecinos = calcularVecinos(dimension > 5000 ? 10 : 20); //<><opt> se calcula una sola vez por instancia

            Random rand = new Random(semilla);

            //las 3 soluciones que tenemos que ir siguiendo:

            int[] solActual = sol.clone(); //cosas de java
            for (int p = 0; p < dimension; p++) pos[solActual[p]] = p; //<><opt> sincronizamos pos con la solucion inicial
            double costeActual = funcionEvaluacion(solActual);

            int[] mejorSolMomento = solActual.clone();
            double costeMejorMomento = costeActual;

            int[] mejorSolGlobal = solActual.clone();
            double costeMejorGlobal = costeActual;

            //inicializamos los contadores de iteraciones y estancamiento
            int iteraciones = 1;
            int estancamiento = 0;

            log.writeLine("Inicio Búsqueda Tabú | Coste Inicial = " + costeActual);

            while (iteraciones < this.maxIteraciones) {
                double[] mejorMovimiento = entorno(solActual, iteraciones);
                double mejorDiferencia = mejorMovimiento[0];
                int mejorI = (int) mejorMovimiento[1];
                int mejorJ = (int) mejorMovimiento[2];

                if (mejorI == -1 || mejorJ == -1){
                    log.writeLine("AVISO: No se han encontrado movimientos válidos en la iteración " + iteraciones + ". Forzando salida.");
                    break; //<< no se han encontrado movimientos válidos, forzar salida
                }

                //1. tenemos que extraer las ciudades antes, para poder registrarlo en la MCP
                int c1 = solActual[mejorI];
                int c2 = solActual[mejorJ];
                int menor = min(c1, c2);
                int mayor = max(c1, c2);

                //2. aplicamos el movimiento
                aplicarMovimiento(mejorI, mejorJ, solActual);
                costeActual += mejorDiferencia; //Si el movimiento mejora la ruta la diferencia es negativa y disminuye el costo actual
                //Sumas la diferencia al coste de la solucion actual

                //registro para cada iteración(tambien contamos lo malo):
                if (mejorDiferencia < 0) {
                    log.writeLine("Iteración " + iteraciones + " [MEJORA]: Coste = " + costeActual + " (Delta: " + mejorDiferencia + ") || intercambio ("+c1+","+c2+")");
                } else {
                    log.writeLine("Iteración " + iteraciones + " [EMPEORAMIENTO]: Coste = " + costeActual + " (Delta: +" + mejorDiferencia + ") || intercambio ("+c1+","+c2+")");
                }


                //3. guardarlo en la MCP
                memoria[menor][mayor] = (short) (iteraciones + this.tenenciaTabu); //Cuando sea: (memoria[menor][mayor] > iteraciones) lo saco de la MCP

                //4. actualizamos los arcos en la MLP
                actualizarMLP(solActual);

                //vemos si tenemos que actualizar el mejor del momento antes de continuar
                if (costeActual < costeMejorMomento) {
                    mejorSolMomento = solActual.clone();
                    costeMejorMomento = costeActual;
                    estancamiento = 0;

                    //vemos si tenemos que actualizar el mejor global
                    if (costeActual < costeMejorGlobal) {
                        mejorSolGlobal = solActual.clone();
                        costeMejorGlobal = costeActual;
                        log.writeLine("Iteración " + iteraciones + " [NUEVO RÉCORD GLOBAL]: Coste = " + costeMejorGlobal);
                    }

                } else {
                    estancamiento++; //le sumo "+1" al estancamiento si no mejora, si llega al límite tengo que generar una nueva solucion a
                    //partir de la MLP (oscilación estrategica).
                }

                //Oscilación estratégica. Generalmente solo se activa cuando no mejora MCP
                if (estancamiento >= this.estancamiento) {
                    boolean intensificar = rand.nextDouble() < (porcentajeOscilacion / 100.0); //Aleatoriamente decidimos si queremos intensificar o diversificar
                    String tipo = intensificar ? "INTENSIFICACION" : "DIVERSIFICACION";

                    solActual = generarSolucionMLP(intensificar, rand);
                    for (int p = 0; p < dimension; p++) pos[solActual[p]] = p; //<><opt> la solucion cambia entera: recalculamos pos
                    costeActual = funcionEvaluacion(solActual);

                    log.writeLine("Iteración " + iteraciones + " [OSCILACIÓN - " + tipo + "]: Reinicio con nuevo Coste = " + costeActual);

                    mejorSolMomento = solActual.clone();
                    costeMejorMomento = costeActual;

                    vaciarMCP();
                    estancamiento = 0;
                }

                iteraciones++;
            }
            log.writeLine("Fin Búsqueda Tabú | Mejor Coste Global Conseguido = " + costeMejorGlobal);
            return mejorSolGlobal;
        }catch(Exception e) {
                log.writeLine("ERROR EN BÚSQUEDA TABÚ: " + e.getMessage());
        }finally {
            log.closeWriter();
        }
        return new int[0];
    }
}