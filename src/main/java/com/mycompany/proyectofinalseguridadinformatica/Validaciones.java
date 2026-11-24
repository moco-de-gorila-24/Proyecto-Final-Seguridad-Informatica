/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyectofinalseguridadinformatica;

import java.util.regex.Pattern;

/**
 *
 * @author Jorge
 */
public class Validaciones {
    
    // Usuario entre 4 y 20 caracteres
    private static final String REGEX_USUARIO = "^[a-zA-Z0-9_]{4,20}$";
    
    private static final String REGEX_CORREO = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    // Contrasena:
    // Minimo 8 caracteres, una mayuscula, una minuscula, un numero y un caracter especial
    private static final String REGEX_CONTRASENA = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!])[A-Za-z\\d@#$%^&+=!]{8,}$";

    public static boolean validarUsuario(String usuario) {
        return usuario != null && Pattern.matches(REGEX_USUARIO, usuario);
    }

    public static boolean validarCorreo(String correo) {
        return correo != null && Pattern.matches(REGEX_CORREO, correo);
    }

    public static boolean validarContrasena(String contrasena) {
        return contrasena != null && Pattern.matches(REGEX_CONTRASENA, contrasena);
    }
}
