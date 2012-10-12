package net.medcommons.modules.itk;

/**
 * Parameters for image transform passed to ITK subroutine.
 * @author sdoyle
 *
 */
public class ImageTransformDimensions {
	int window;
	int level;
	int outputMaxWidth;
	int outputMaxHeight;
	boolean inputSubregionSpecified = false;
	double inputRegionBottomRightX;
	double inputRegionBottomRightY;
	double inputRegionTopLeftX;
	double inputRegionTopLeftY;
	
	
	
	
	public int getWindow(){
		return(this.window);
	}
	public void setWindow(int window){
		this.window = window;
	}
	public int getLevel(){
		return(this.level);
	}
	public void setLevel(int level){
		this.level = level;
	}
	
	public int getOutputMaxWidth(){
		return(outputMaxWidth);
	}
	public void setOutputMaxWidth(int outputMaxWidth){
		this.outputMaxWidth = outputMaxWidth;
	}
	
	
	public int getOutputMaxHeight(){
		return(this.outputMaxHeight);
	}
	public void setOutputMaxHeight(int outputMaxHeight){
		this.outputMaxHeight = outputMaxHeight;
	}

	public boolean getInputSubregionSpecified(){
		return(inputSubregionSpecified);
	}
	public void setInputSubregionSpecified(boolean inputSubregionSpecified ){
		this.inputSubregionSpecified = inputSubregionSpecified;
	}
	
	
	public double getInputRegionBottomRightX(){
		return(inputRegionBottomRightX);
	}
	public void setInputRegionBottomRightX(double inputRegionBottomRightX){
		this.inputRegionBottomRightX = inputRegionBottomRightX;
	}
	
	public double getInputRegionBottomRightY(){
		return(inputRegionBottomRightY);
	}
	public void setInputRegionBottomRightY(double inputRegionBottomRightY){
		this.inputRegionBottomRightY = inputRegionBottomRightY;
	}
	
	public double getInputRegionTopLeftX(){
		return(inputRegionTopLeftX);
	}
	public void setInputRegionTopLeftX(double inputRegionTopLeftX){
		this.inputRegionTopLeftX = inputRegionTopLeftX;
	}
	public double getInputRegionTopLeftY(){
		return(inputRegionTopLeftY);
	}
	public void setInputRegionTopLeftY(double inputRegionTopLeftY){
		this.inputRegionTopLeftY = inputRegionTopLeftY;
	}
	
}
