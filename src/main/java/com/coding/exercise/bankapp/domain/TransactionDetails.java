package com.coding.exercise.bankapp.domain;

import lombok.*;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TransactionDetails {

	private Long accountNumber;

	private Date txDateTime;

	private String txType;

	public void setTransactionType(String txType) {
		this.txType = txType;
	}

	public void setAmount(Double txAmount) {
		this.txAmount = txAmount;
	}

	private Double txAmount;
}
