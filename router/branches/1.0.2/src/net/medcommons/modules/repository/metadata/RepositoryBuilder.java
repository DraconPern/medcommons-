package net.medcommons.modules.repository.metadata;



import org.jdom.input.SAXBuilder;
;

	public class RepositoryBuilder extends SAXBuilder {

	    public RepositoryBuilder() {
	        super();
	        this.setFactory(new RepositoryElementFactory());
	    }

	    public RepositoryBuilder(boolean arg0) {
	        super(arg0);
	        this.setFactory(new RepositoryElementFactory());
	    }

	    public RepositoryBuilder(String arg0) {
	        super(arg0);
	        this.setFactory(new RepositoryElementFactory());
	    }

	    public RepositoryBuilder(String arg0, boolean arg1) {
	        super(arg0, arg1);
	        this.setFactory(new RepositoryElementFactory()); 
	    }

	}
