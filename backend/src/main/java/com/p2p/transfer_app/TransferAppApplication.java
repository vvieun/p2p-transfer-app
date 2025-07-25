package com.p2p.transfer_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class TransferAppApplication 
{

	public static void main(String[] args) 
	{
		SpringApplication.run(TransferAppApplication.class, args);
	}

}
