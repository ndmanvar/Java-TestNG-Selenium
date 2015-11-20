package com.yourcompany.utils;

import org.testng.ITestResult;
import org.testng.util.RetryAnalyzerCount;

/**
 * Created by mehmetgerceker on 11/12/15.
 */
public class RetryRule extends RetryAnalyzerCount {
    public RetryRule() {
        //We probably want to get this from CLI or some config file.
        super.setCount(1);
    }

    @Override
    public boolean retryMethod(ITestResult arg0){
        System.out.println("Test Name: " + arg0.getName()
                + " Test status: " + arg0.getStatus()
                + "Retries left: " + super.getCount());
        return true;
    }
}
