/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Test;

import Utilities.PasswordUtil;

/**
 *
 * @author LENOVO
 */
public class PasswordTest {
    public static void main(String[] args) {
        String plainPassword = "Admin@123"; // replace with your desired password
        String hashedPassword = PasswordUtil.hashPassword(plainPassword);
        System.out.println("Hashed password: " + hashedPassword);
//        Uploader
        String plainPassword1 = "Tamanna@123"; // replace with your desired password
        String hashedPassword1 = PasswordUtil.hashPassword(plainPassword1);
        System.out.println("Hashed password: " + hashedPassword1);
    }
}
