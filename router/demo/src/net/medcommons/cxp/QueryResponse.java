package net.medcommons.cxp;

public class QueryResponse {
	private Parameter[] parameters = null;
	public void setParameters(Parameter[] parameters){
		this.parameters = parameters;
	}
	public Parameter[] getParameters(){
		return(this.parameters);
	}
	
	
}
