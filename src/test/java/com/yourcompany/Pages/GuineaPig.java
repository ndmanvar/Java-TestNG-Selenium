package com.yourcompany.Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class GuineaPig {
	private final WebDriver driver;
	
	By emailInputFieldLocator = By.id("fbemail");
	
	public GuineaPig(WebDriver driver) {
		this.driver = driver;
	}
	
	public void fillOutEmailInput(String inputText) {
		driver.findElement(emailInputFieldLocator).sendKeys(inputText);
	}
	
	public String getEmailInput() {
		return driver.findElement(emailInputFieldLocator).getAttribute("value");
	}

}