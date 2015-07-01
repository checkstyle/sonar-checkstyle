package org.sonar.itests;

public class IgnoreCheckstyleFalsePositives {
	
  public void ignoreFalsePositive(){
  	//CHECKSTYLE:OFF
  	try {
  	  int i=2;	
  	} catch (Exception e) {
  	}
    //CHECKSTYLE:ON
  }
  
  public void ruleIsViolated(){
    try {
  	  int i=2;	
  	} catch (Exception e) {
  	}
  }
}
