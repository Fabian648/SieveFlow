package de.muenchen.aigner;
import de.muenchen.aigner.domain.model.PrimeBlock;

import java.math.BigInteger;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        PrimeBlock test = new PrimeBlock(new BigInteger("1"));
        System.out.println(test.getString());
    }
}