package com.blackduckdemo.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	static double[][] createRealImaginary(final Complex[] dataC) {
        final double[][] dataRI = new double[2][dataC.length];
        final double[] dataR = dataRI[0];
        final double[] dataI = dataRI[1];
        for (int i = 0; i < dataC.length; i++) {
            final Complex c = dataC[i];
            dataR[i] = c.getReal();
            dataI[i] = c.getImaginary();
        }
        return dataRI;
    }

}
