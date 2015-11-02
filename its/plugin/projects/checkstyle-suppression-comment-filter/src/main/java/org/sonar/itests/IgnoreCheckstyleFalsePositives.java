package org.sonar.itests;

public class IgnoreCheckstyleFalsePositives {
	
  public void ignoreFalsePositive(){
  	//CHECKSTYLE:OFF
		;
    //CHECKSTYLE:ON
  }
  
  public void ruleIsViolated(){
    ;
  }
}
