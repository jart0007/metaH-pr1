package com.jart0007;

public class Par<K, V> {
    private final K clave;
    private  V valor;

    // Constructor
    public Par(K clave, V valor) {
        this.clave = clave;
        this.valor = valor;
    }

    // Métodos Getters
    public K getClave() { return clave; }
    public V getValor() { return valor; }

    //  Métodos Setters
    public void setValor(V valor) { this.valor = valor; }
}
