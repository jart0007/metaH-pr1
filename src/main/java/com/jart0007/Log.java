package com.jart0007;

//> codigo sacado de https://discoduroderoer.es/clase-log-en-java/
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat; //para poner la fecha
import java.util.ArrayList;
import java.util.Date;

public class Log {

    private BufferedWriter buffered;
    private String ruta;

    public Log(String ruta) throws IOException {
        this.ruta = ruta;
        this.open(true);
    }

    public Log(String ruta, boolean reset) throws IOException {
        this.ruta = ruta;
        this.open(!reset);
    }

    private void open(boolean append) throws IOException {
        this.buffered = new BufferedWriter(new FileWriter(this.ruta, append));
    }

    public void addLine(String line) throws IOException {

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss");
        String formatoFecha = sdf.format(new Date());
        this.open(true);
        this.buffered.write("[" + formatoFecha + "] " + line + "\n");
        this.close();
    }

    // --- MÉTODOS OPTIMIZADOS PARA FLUJO CONTINUO DE ESCRITURA ---

    /**
     * Abre el archivo una sola vez al principio del algoritmo.
     */
    public void openWriter() throws IOException {
        this.buffered = new BufferedWriter(new FileWriter(this.ruta, true));
    }

    /**
     * Escribe en el flujo en memoria sin abrir ni cerrar el archivo.
     */
    public void writeLine(String line) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String formatoFecha = sdf.format(new Date());
        this.buffered.write("[" + formatoFecha + "] " + line);
        this.buffered.newLine();
    }

    /**
     * Cierra el archivo y vuelca todo el contenido al disco al terminar el algoritmo.
     */
    public void closeWriter() throws IOException {
        if (this.buffered != null) {
            this.buffered.flush();
            this.buffered.close();
        }
    }

    public String[] getLines() throws FileNotFoundException, IOException {
        // Corregidas las entidades HTML (&lt; y &gt;) por los símbolos < y >
        ArrayList<String> linesFile = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(this.ruta));

        String line;
        while ((line = br.readLine()) != null) {
            linesFile.add(line);
        }

        br.close();

        // Conversión limpia a array
        return linesFile.toArray(new String[0]);
    }

    /**
     * Usa getLines() para obtener el contenido del log y escribirlo en un archivo de destino.
     *
     * @param rutaDestino Ruta del archivo donde se copiarán las líneas.
     * @throws IOException Si ocurre un error de lectura o escritura.
     */
    public void exportToFile(String rutaDestino) throws IOException {
        String[] lineas = this.getLines();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaDestino))) {
            for (String linea : lineas) {
                bw.write(linea);
                bw.newLine();
            }
        }
    }

    public void resetLog() throws IOException{
        this.open(false);
        this.close();
    }

    private void close() throws IOException{
        this.buffered.close();
    }

}