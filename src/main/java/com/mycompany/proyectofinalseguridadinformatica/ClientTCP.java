/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyectofinalseguridadinformatica;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author Jorge
 */
public class ClientTCP {

    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);

        System.out.print("Direccion del servidor: ");
        String direccion = teclado.nextLine();
        System.out.print("Puerto: ");
        int puerto = Integer.parseInt(teclado.nextLine());

        try (Socket socket = new Socket(direccion, puerto)) {
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
            String respuesta = entrada.readLine();

            if (respuesta.startsWith("RECHAZADO")) {
                System.out.println("El servidor rechazo la conexion.");
                System.out.println(respuesta.substring(respuesta.indexOf("|") + 1));

                return;
            }

            if (respuesta.startsWith("ACEPTADO")) {
                System.out.println(respuesta.substring(respuesta.indexOf("|") + 1));
            }

            String mensaje;

            while (true) {
                System.out.println();
                System.out.println("Escribe un mensaje o SALIR para desconectarte:");

                mensaje = teclado.nextLine();
                salida.println(mensaje);
                respuesta = entrada.readLine();

                if (respuesta == null) {
                    break;
                }

                System.out.println(respuesta);

                if (mensaje.equalsIgnoreCase("SALIR")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("No se pudo conectar al servidor.");
            System.out.println("Verifica que el servidor este iniciado.");
        }
    }
}
