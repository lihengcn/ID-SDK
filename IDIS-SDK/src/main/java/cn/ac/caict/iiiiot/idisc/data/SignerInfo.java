package cn.ac.caict.iiiiot.idisc.data;

import java.security.PrivateKey;

public class SignerInfo {
	public ValueReference signer;
	public PrivateKey prvKey;
	public SignerInfo(ValueReference signer,PrivateKey prvKey){
		this.signer = signer;
		this.prvKey = prvKey;
	}
}
