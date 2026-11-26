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
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
    private static final Set<String> usuariosConectados = ConcurrentHashMap.newKeySet();

    private static final Usuario[] usuarios = {
        new Usuario("Angel", "12345"),
        new Usuario("Jorge", "abc123"),
        new Usuario("Luis", "password"),
        new Usuario("4", "password"),
        new Usuario("5", "password"),
        new Usuario("6", "password"),
        new Usuario("7", "password"),
        new Usuario("8", "password"),
        new Usuario("9", "password"),
        new Usuario("10", "password"),
        new Usuario("11", "password")
    };

    public static void main(String[] args) {
        System.out.println("Iniciando servidor de votaciones...");

        try (ServerSocket servidor = new ServerSocket(PUERTO)) {
            System.out.println("Servidor escuchando en el puerto " + PUERTO);

            while (true) {
                Socket cliente = servidor.accept();

                if (limite.tryAcquire()) {
                    
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
            System.out.println("Error al iniciar el servidor:");
        }
    }

    private static class ManejadorCliente implements Runnable {
        private final Socket cliente;

        public ManejadorCliente(Socket cliente) {
            this.cliente = cliente;
        }

        @Override
        public void run() {

            boolean autenticado = false;
            String nombreUsuarioActivo = null;

            try (
                BufferedReader entrada = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
                PrintWriter salida = new PrintWriter(cliente.getOutputStream(), true)
            ) {

                // Solicitar credenciales
                salida.println("AUTENTICACION");

                String nombre = entrada.readLine();
                String contrasenia = entrada.readLine();

                // Validar credenciales
                if (nombre == null || contrasenia == null || !autenticar(nombre, contrasenia)) {
                    salida.println("RECHAZADO|Nombre o contraseña incorrectos.");
                    return;
                }

                // Normalizar el nombre para evitar conexiones duplicadas por mayusculas
                String nombreNormalizado = nombre.trim().toLowerCase(Locale.ROOT);

                // Intentar registrar al usuario activo
                if (!usuariosConectados.add(nombreNormalizado)) {
                    salida.println("RECHAZADO|Este usuario ya tiene una sesion activa.");
                    System.out.println("Intento de conexion duplicada: " + nombre);

                    return;
                }

                // El usuario ya quedo registrado
                nombreUsuarioActivo = nombreNormalizado;
                autenticado = true;
                int cantidad = conexionesActivas.incrementAndGet();

                System.out.println("Cliente conectado: " + nombre + " | Conexiones: " + cantidad);
                salida.println("ACEPTADO|Bienvenido " + nombre + ". Conexiones: " + cantidad + "/" + MAX_CONEXIONES);
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
                
                // Liberar usuario solo si fue autenticado
                if (autenticado) {
                    usuariosConectados.remove(nombreUsuarioActivo);
                    int cantidad = conexionesActivas.decrementAndGet();
                    System.out.println("Cliente desconectado. Conexiones activas: " + cantidad);
                }

                limite.release();
            }
        }
    }
    private static boolean autenticar(String nombre, String contrasenia) {
        for (Usuario usuario : usuarios) {
            if (usuario.getNombre().equals(nombre) && usuario.getContrasenia().equals(contrasenia)) {
                return true;
            }
        }
        return false;
    }
}
