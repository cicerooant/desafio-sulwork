package br.com.desafio.eventos.participante;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfValidator implements ConstraintValidator<CpfValido, String> {

    @Override
    public boolean isValid(String cpf, ConstraintValidatorContext context) {
        if (cpf == null || !cpf.matches("\\d{11}")) {
            return false;
        }

        if (cpf.chars().distinct().count() == 1) {
            return false;
        }

        int primeiroDigito = calcularDigito(cpf, 9);
        int segundoDigito = calcularDigito(cpf, 10);
        return primeiroDigito == Character.digit(cpf.charAt(9), 10)
                && segundoDigito == Character.digit(cpf.charAt(10), 10);
    }

    private int calcularDigito(String cpf, int quantidadeDigitos) {
        int soma = 0;
        for (int indice = 0; indice < quantidadeDigitos; indice++) {
            int peso = quantidadeDigitos + 1 - indice;
            soma += Character.digit(cpf.charAt(indice), 10) * peso;
        }

        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
