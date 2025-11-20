//package com.nrd.messagerie;
//
//
//
//import java.util.Base64;
//import javax.crypto.KeyGenerator;
//import javax.crypto.SecretKey;
//
//public class GenerateKey {
//    public static void main(String[] args) throws Exception {
//        KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA512");
//        keyGen.init(512);
//        SecretKey secretKey = keyGen.generateKey();
//        String base64Key = Base64.getEncoder().encodeToString(secretKey.getEncoded());
//        System.out.println("Clé JWT (copiez-la dans application.yml):");
//        System.out.println(base64Key);
//    }
//}