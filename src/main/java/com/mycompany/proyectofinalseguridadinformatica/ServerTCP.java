/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyectofinalseguridadinformatica;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Jorge
 */
public class ServerTCP {
    private static final int PUERTO = 5000;
    private static final int MAX_CONEXIONES = 10;
    private static final Semaphore limite = new Semaphore(MAX_CONEXIONES);
    private static final AtomicInteger conexionesActivas = new AtomicInteger(0);

    public static void main(String[] args) {
        System.out.println("Iniciando servidor de votaciones...");

        try (ServerSocket servidor = new ServerSocket(PUERTO)) {
            System.out.println("Servidor escuchando en el puerto " + PUERTO);

            while (true) {
                Socket cliente = servidor.accept();

                if (limite.tryAcquire()) {
                    int cantidad = conexionesActivas.incrementAndGet();
                    System.out.println("Cliente conectado: " + cliente.getInetAddress() + " | Conexiones: " + cantidad);
                    Thread hilo = new Thread(new ManejadorCliente(cliente));
                    hilo.start();
                } else {
                    try (PrintWriter salida = new PrintWriter(cliente.getOutputStream(), true)) {
                        salida.println("RECHAZADO|Servidor lleno. Maximo 10 conexiones.");
                    }

                    cliente.close();
                    System.out.println("Conexion rechazada. Limite alcanzado.");
                }
            }
        } catch (IOException e) {
            System.out.println("Error al iniciar el servidor.");
        }
    }

    private static class ManejadorCliente implements Runnable {
        private final Socket cliente;

        public ManejadorCliente(Socket cliente) {
            this.cliente = cliente;
        }

        @Override
        public void run() {
            try (
                BufferedReader entrada = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
                PrintWriter salida = new PrintWriter(cliente.getOutputStream(), true)
            ) {
                int cantidad = conexionesActivas.get();
                salida.println("ACEPTADO|Bienvenido al servidor. Conexiones: " + cantidad + "/" + MAX_CONEXIONES);
                String mensaje;
                while ((mensaje = entrada.readLine()) != null) {
                    if (mensaje.equalsIgnoreCase("SALIR")) {
                        salida.println("DESCONEXION_OK|Te has desconectado.");
                        break;
                    } else {
                        salida.println("MENSAJE|Servidor recibio: " + mensaje);
                    }
                }
            } catch (IOException e) {
                System.out.println("Se perdio la conexion con un cliente.");
                
            } finally {
                try {
                    cliente.close();
                } catch (IOException e) {

                }

                conexionesActivas.decrementAndGet();
                limite.release();
                System.out.println("Cliente desconectado. Conexiones activas: " + conexionesActivas.get());
            }
        }
    }
}
